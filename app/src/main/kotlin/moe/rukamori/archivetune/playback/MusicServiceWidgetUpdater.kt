/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.playback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.media3.common.Player
import androidx.palette.graphics.Palette
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.rukamori.archivetune.R
import moe.rukamori.archivetune.extensions.SilentHandler
import moe.rukamori.archivetune.utils.reportException
import kotlinx.coroutines.flow.firstOrNull
import moe.rukamori.archivetune.db.entities.LyricsEntity
import moe.rukamori.archivetune.lyrics.LyricsEntry
import moe.rukamori.archivetune.lyrics.LyricsUtils
import moe.rukamori.archivetune.models.MediaMetadata
import moe.rukamori.archivetune.widget.AppleLiquidCompactWidget
import moe.rukamori.archivetune.widget.AppleLiquidDuoWidget
import moe.rukamori.archivetune.widget.AppleLiquidHeroWidget
import moe.rukamori.archivetune.widget.AppleLiquidIslandWidget
import moe.rukamori.archivetune.widget.AppleLiquidLyricsWidget
import moe.rukamori.archivetune.widget.AppleLiquidMiniWidget
import moe.rukamori.archivetune.widget.AppleLiquidMonoWidget
import moe.rukamori.archivetune.widget.AppleLiquidNowWidget
import moe.rukamori.archivetune.widget.AppleLiquidOrbWidget
import moe.rukamori.archivetune.widget.AppleLiquidPanoWidget
import moe.rukamori.archivetune.widget.AppleLiquidPillWidget
import moe.rukamori.archivetune.widget.AppleLiquidPosterWidget
import moe.rukamori.archivetune.widget.AppleLiquidShelfWidget
import moe.rukamori.archivetune.widget.AppleLiquidSplitWidget
import moe.rukamori.archivetune.widget.AppleLockscreenWidget
import moe.rukamori.archivetune.widget.LoadWidgetInsightsUseCase
import moe.rukamori.archivetune.widget.MusicWidgetKeys
import moe.rukamori.archivetune.widget.PlaybackSpotlightWidget
import moe.rukamori.archivetune.widget.WidgetInsightsSnapshot
import moe.rukamori.archivetune.widget.ZenTanzakuWidget
import moe.rukamori.archivetune.widget.toMutableWidgetPreferences
import moe.rukamori.archivetune.widget.toWidgetPreferenceValue
import java.io.File

internal class MusicServiceWidgetUpdater(
    private val service: MusicService,
    private val player: Player,
    private val scope: CoroutineScope,
    private val loadWidgetInsights: LoadWidgetInsightsUseCase,
) {
    private val widgetManager = GlanceAppWidgetManager(service)
    private val audioManager = service.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val powerManager = service.getSystemService(Context.POWER_SERVICE) as? PowerManager
    private var stateJob: Job? = null
    private var progressJob: Job? = null
    private var isReceiverRegistered = false
    private var cachedSongId: String? = null
    private var cachedLyrics: List<LyricsEntry> = emptyList()

    private suspend fun ensureLyrics(mediaMetadata: MediaMetadata?) {
        if (mediaMetadata == null) {
            cachedSongId = null
            cachedLyrics = emptyList()
            return
        }
        if (mediaMetadata.id == cachedSongId) return
        cachedSongId = mediaMetadata.id
        cachedLyrics = withContext(Dispatchers.IO) {
            try {
                val cached = service.database.lyrics(mediaMetadata.id).firstOrNull()?.lyrics
                val raw = if (cached != null && cached != LyricsEntity.LYRICS_NOT_FOUND) {
                    cached
                } else {
                    service.lyricsHelper.getLyrics(mediaMetadata)
                }
                if (raw != null && raw != LyricsEntity.LYRICS_NOT_FOUND) {
                    LyricsUtils.parseLyrics(raw)
                } else {
                    emptyList()
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    private fun getLyricsForPosition(positionMs: Long): LyricsState {
        val list = cachedLyrics
        if (list.isEmpty()) return LyricsState(null, null, null, null, false)
        val activeIdx = list.indexOfLast { it.time <= positionMs + 400L }
        val active = if (activeIdx >= 0) list[activeIdx].text else if (list.isNotEmpty()) list.first().text else null
        val prev = if (activeIdx > 0) list[activeIdx - 1].text else null
        val next = if (activeIdx >= 0 && activeIdx + 1 < list.size) list[activeIdx + 1].text else null
        val next2 = if (activeIdx >= 0 && activeIdx + 2 < list.size) list[activeIdx + 2].text else null
        return LyricsState(active, prev, next, next2, true)
    }

    private data class LyricsState(
        val active: String?,
        val prev: String?,
        val next: String?,
        val next2: String?,
        val hasLyrics: Boolean,
    )

    private val broadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?,
            ) {
                when (intent?.action) {
                    "android.media.VOLUME_CHANGED_ACTION" -> {
                        scope.launch(SilentHandler) {
                            updateVolumeState()
                        }
                    }
                    Intent.ACTION_SCREEN_ON -> {
                        if (player.isPlaying) {
                            update()
                            updateProgressTracking()
                        }
                    }
                }
            }
        }

    init {
        try {
            val filter =
                IntentFilter().apply {
                    addAction("android.media.VOLUME_CHANGED_ACTION")
                    addAction(Intent.ACTION_SCREEN_ON)
                }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                service.registerReceiver(broadcastReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                service.registerReceiver(broadcastReceiver, filter)
            }
            isReceiverRegistered = true
        } catch (_: Exception) {
            isReceiverRegistered = false
        }
    }

    fun destroy() {
        stateJob?.cancel()
        progressJob?.cancel()
        if (isReceiverRegistered) {
            try {
                service.unregisterReceiver(broadcastReceiver)
            } catch (_: Exception) {
            }
            isReceiverRegistered = false
        }
    }

    fun update() {
        stateJob?.cancel()
        stateJob =
            scope.launch(SilentHandler) {
                pushState()
            }
    }

    fun updateProgressTracking() {
        progressJob?.cancel()
        if (player.isPlaying && player.duration > 0) {
            progressJob =
                scope.launch(SilentHandler) {
                    val installedTargets = findInstalledTargets(progressWidgets)
                    if (installedTargets.isEmpty()) return@launch

                    while (isActive && player.isPlaying) {
                        delay(WIDGET_PROGRESS_UPDATE_INTERVAL_MILLIS)
                        val isInteractive = powerManager?.isInteractive ?: true
                        if (isInteractive && player.isPlaying) {
                            updateProgress(
                                installedTargets = installedTargets,
                                progress = player.playbackProgress(),
                                positionMs = player.currentPosition.coerceAtLeast(0L),
                                durationMs = player.duration.coerceAtLeast(0L),
                            )
                        }
                    }
                }
        }
    }

    private fun getVolumeProgress(): Float {
        val am = audioManager ?: return 0.5f
        val cur = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        return (cur.toFloat() / max.toFloat()).coerceIn(0f, 1f)
    }

    private suspend fun updateVolumeState() {
        val installedTargets = findInstalledTargets(playbackWidgets)
        if (installedTargets.isEmpty()) return
        val volProgress = getVolumeProgress()

        installedTargets.forEach { installedTarget ->
            installedTarget.ids.forEach { id ->
                updateAppWidgetState(service, PreferencesGlanceStateDefinition, id) { prefs ->
                    prefs.toMutableWidgetPreferences().apply {
                        this[MusicWidgetKeys.VOLUME_PROGRESS] = volProgress
                    }
                }
                installedTarget.target.widget.update(service, id)
            }
        }
    }

    private suspend fun pushState() {
        val installedTargets = findInstalledTargets(playbackWidgets)
        if (installedTargets.isEmpty()) return

        val mediaItem = player.currentMediaItem
        val meta = mediaItem?.mediaMetadata
        val artFile = meta?.artworkUri?.let { cacheAlbumArt(it) }
        val dominantColor = artFile?.let { extractDominantColor(it) }
        val currentMeta = service.currentMediaMetadata.value
        ensureLyrics(currentMeta)
        val lyricsState = getLyricsForPosition(player.currentPosition)
        val snapshot =
            WidgetSnapshot(
                title = meta?.title?.toString() ?: service.getString(R.string.no_track_playing),
                artist = meta?.artist?.toString().orEmpty(),
                isPlaying = player.isPlaying,
                isAvailable = mediaItem != null,
                playbackPosition = player.playbackProgress(),
                positionMs = player.currentPosition.coerceAtLeast(0L),
                durationMs = player.duration.coerceAtLeast(0L),
                volumeProgress = getVolumeProgress(),
                artPath = artFile?.absolutePath,
                dominantColor = dominantColor,
                insights = WidgetInsightsSnapshot.Empty,
                activeLyric = lyricsState.active,
                prevLyric = lyricsState.prev,
                nextLyric = lyricsState.next,
                nextLyric2 = lyricsState.next2,
                hasLyrics = lyricsState.hasLyrics,
            )

        installedTargets.forEach { target ->
            updateWidget(target, snapshot)
        }
    }

    private suspend fun updateProgress(
        installedTargets: List<InstalledWidgetTarget>,
        progress: Float,
        positionMs: Long,
        durationMs: Long,
    ) {
        val lyricsState = getLyricsForPosition(positionMs)
        installedTargets.forEach { installedTarget ->
            installedTarget.ids.forEach { id ->
                updateAppWidgetState(service, PreferencesGlanceStateDefinition, id) { prefs ->
                    prefs.toMutableWidgetPreferences().apply {
                        this[MusicWidgetKeys.PLAYBACK_POSITION] = progress
                        this[MusicWidgetKeys.POSITION_MS] = positionMs
                        this[MusicWidgetKeys.DURATION_MS] = durationMs
                        if (lyricsState.hasLyrics) {
                            this[MusicWidgetKeys.HAS_LYRICS] = true
                            lyricsState.active?.let { this[MusicWidgetKeys.LYRIC_ACTIVE] = it } ?: remove(MusicWidgetKeys.LYRIC_ACTIVE)
                            lyricsState.prev?.let { this[MusicWidgetKeys.LYRIC_PREV] = it } ?: remove(MusicWidgetKeys.LYRIC_PREV)
                            lyricsState.next?.let { this[MusicWidgetKeys.LYRIC_NEXT] = it } ?: remove(MusicWidgetKeys.LYRIC_NEXT)
                            lyricsState.next2?.let { this[MusicWidgetKeys.LYRIC_NEXT2] = it } ?: remove(MusicWidgetKeys.LYRIC_NEXT2)
                        } else {
                            this[MusicWidgetKeys.HAS_LYRICS] = false
                        }
                    }
                }
                installedTarget.target.widget.update(service, id)
            }
        }
    }

    private suspend fun updateWidget(
        installedTarget: InstalledWidgetTarget,
        snapshot: WidgetSnapshot,
    ) {
        val targetSnapshot =
            if (installedTarget.target.requiresInsights) {
                snapshot.copy(insights = loadInsightsSnapshot())
            } else {
                snapshot
            }

        installedTarget.ids.forEach { id ->
            updateAppWidgetState(service, PreferencesGlanceStateDefinition, id) { prefs ->
                prefs.toMutableWidgetPreferences().apply {
                    writeSnapshot(targetSnapshot)
                }
            }
            installedTarget.target.widget.update(service, id)
        }
    }

    private suspend fun findInstalledTargets(targets: List<WidgetTarget>): List<InstalledWidgetTarget> =
        targets.mapNotNull { target ->
            widgetManager
                .getGlanceIds(target.widgetClass)
                .takeIf { ids -> ids.isNotEmpty() }
                ?.let { ids -> InstalledWidgetTarget(target, ids) }
        }

    private fun MutablePreferences.writeSnapshot(snapshot: WidgetSnapshot) {
        this[MusicWidgetKeys.TRACK_TITLE] = snapshot.title
        this[MusicWidgetKeys.TRACK_ARTIST] = snapshot.artist
        this[MusicWidgetKeys.IS_PLAYING] = snapshot.isPlaying
        this[MusicWidgetKeys.IS_AVAILABLE] = snapshot.isAvailable
        this[MusicWidgetKeys.PLAYBACK_POSITION] = snapshot.playbackPosition
        this[MusicWidgetKeys.POSITION_MS] = snapshot.positionMs
        this[MusicWidgetKeys.DURATION_MS] = snapshot.durationMs
        this[MusicWidgetKeys.VOLUME_PROGRESS] = snapshot.volumeProgress

        val artPath = snapshot.artPath
        if (artPath != null) {
            this[MusicWidgetKeys.ART_PATH] = artPath
        } else {
            remove(MusicWidgetKeys.ART_PATH)
        }

        val dominantColor = snapshot.dominantColor
        if (dominantColor != null) {
            this[MusicWidgetKeys.DOMINANT_COLOR] = dominantColor
        } else {
            remove(MusicWidgetKeys.DOMINANT_COLOR)
        }

        if (snapshot.hasLyrics) {
            this[MusicWidgetKeys.HAS_LYRICS] = true
            snapshot.activeLyric?.let { this[MusicWidgetKeys.LYRIC_ACTIVE] = it } ?: remove(MusicWidgetKeys.LYRIC_ACTIVE)
            snapshot.prevLyric?.let { this[MusicWidgetKeys.LYRIC_PREV] = it } ?: remove(MusicWidgetKeys.LYRIC_PREV)
            snapshot.nextLyric?.let { this[MusicWidgetKeys.LYRIC_NEXT] = it } ?: remove(MusicWidgetKeys.LYRIC_NEXT)
            snapshot.nextLyric2?.let { this[MusicWidgetKeys.LYRIC_NEXT2] = it } ?: remove(MusicWidgetKeys.LYRIC_NEXT2)
        } else {
            this[MusicWidgetKeys.HAS_LYRICS] = false
            remove(MusicWidgetKeys.LYRIC_ACTIVE)
            remove(MusicWidgetKeys.LYRIC_PREV)
            remove(MusicWidgetKeys.LYRIC_NEXT)
            remove(MusicWidgetKeys.LYRIC_NEXT2)
        }

        writeInsights(snapshot.insights)
    }

    private fun MutablePreferences.writeInsights(insights: WidgetInsightsSnapshot) {
        if (insights.listeningTime.isNotBlank()) {
            this[MusicWidgetKeys.LISTENING_TIME] = insights.listeningTime
        } else {
            remove(MusicWidgetKeys.LISTENING_TIME)
        }
        if (insights.totalPlays.isNotBlank()) {
            this[MusicWidgetKeys.TOTAL_PLAYS] = insights.totalPlays
        } else {
            remove(MusicWidgetKeys.TOTAL_PLAYS)
        }
        writeList(MusicWidgetKeys.RECENT_SONGS, insights.recentSongs)
        writeList(MusicWidgetKeys.GENRES, insights.genres)
        writeList(MusicWidgetKeys.RECOMMENDATIONS, insights.recommendations)

        val topSongSummary = insights.topSongSummary
        if (!topSongSummary.isNullOrBlank()) {
            this[MusicWidgetKeys.TOP_SONG_SUMMARY] = topSongSummary
        } else {
            remove(MusicWidgetKeys.TOP_SONG_SUMMARY)
        }
    }

    private fun MutablePreferences.writeList(
        key: Preferences.Key<String>,
        values: List<String>,
    ) {
        if (values.isEmpty()) {
            remove(key)
        } else {
            this[key] = values.toWidgetPreferenceValue()
        }
    }

    private suspend fun loadInsightsSnapshot(): WidgetInsightsSnapshot =
        try {
            loadWidgetInsights()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            reportException(error)
            WidgetInsightsSnapshot.Empty
        }

    private suspend fun cacheAlbumArt(uri: Uri): File? =
        withContext(Dispatchers.IO) {
            val dest = File(service.cacheDir, "widget_art_${Integer.toHexString(uri.toString().hashCode())}.jpg")
            if (dest.isFile && dest.length() > 0L) return@withContext dest

            if (uri.scheme == "content" || uri.scheme == "file") {
                return@withContext try {
                    service.contentResolver.openInputStream(uri)?.use { src ->
                        dest.outputStream().use { dst -> src.copyTo(dst) }
                    }
                    if (dest.exists() && dest.length() > 0) dest else null
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    null
                }
            }

            if (uri.scheme == "https" || uri.scheme == "http") {
                return@withContext try {
                    val loader = service.applicationContext.imageLoader
                    val request =
                        ImageRequest
                            .Builder(service.applicationContext)
                            .data(uri.toString())
                            .size(512, 512)
                            .allowHardware(false)
                            .build()
                    val result = loader.execute(request)
                    if (result is SuccessResult) {
                        val bitmap = result.image.toBitmap()
                        dest.outputStream().use { out ->
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 88, out)
                        }
                        if (dest.exists() && dest.length() > 0) dest else null
                    } else {
                        null
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    null
                }
            }

            null
        }

    private suspend fun extractDominantColor(file: File): Int? =
        withContext(Dispatchers.Default) {
            try {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return@withContext null
                val palette = Palette.from(bitmap).generate()
                palette.getDarkVibrantColor(
                    palette.getDominantColor(android.graphics.Color.DKGRAY),
                )
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                null
            }
        }

    private fun Player.playbackProgress(): Float =
        if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

    private data class WidgetSnapshot(
        val title: String,
        val artist: String,
        val isPlaying: Boolean,
        val isAvailable: Boolean,
        val playbackPosition: Float,
        val positionMs: Long,
        val durationMs: Long,
        val volumeProgress: Float,
        val artPath: String?,
        val dominantColor: Int?,
        val insights: WidgetInsightsSnapshot,
        val activeLyric: String? = null,
        val prevLyric: String? = null,
        val nextLyric: String? = null,
        val nextLyric2: String? = null,
        val hasLyrics: Boolean = false,
    )

    private data class WidgetTarget(
        val widgetClass: Class<out GlanceAppWidget>,
        val widget: GlanceAppWidget,
        val requiresInsights: Boolean = false,
    )

    private data class InstalledWidgetTarget(
        val target: WidgetTarget,
        val ids: List<GlanceId>,
    )

    private companion object {
        const val WIDGET_PROGRESS_UPDATE_INTERVAL_MILLIS = 1_000L

        val playbackWidgets =
            listOf(
                WidgetTarget(AppleLockscreenWidget::class.java, AppleLockscreenWidget()),
                WidgetTarget(AppleLiquidCompactWidget::class.java, AppleLiquidCompactWidget()),
                WidgetTarget(AppleLiquidPillWidget::class.java, AppleLiquidPillWidget()),
                WidgetTarget(AppleLiquidHeroWidget::class.java, AppleLiquidHeroWidget()),
                WidgetTarget(AppleLiquidMiniWidget::class.java, AppleLiquidMiniWidget()),
                WidgetTarget(AppleLiquidSplitWidget::class.java, AppleLiquidSplitWidget()),
                WidgetTarget(AppleLiquidPosterWidget::class.java, AppleLiquidPosterWidget()),
                WidgetTarget(AppleLiquidIslandWidget::class.java, AppleLiquidIslandWidget()),
                WidgetTarget(AppleLiquidOrbWidget::class.java, AppleLiquidOrbWidget()),
                WidgetTarget(AppleLiquidPanoWidget::class.java, AppleLiquidPanoWidget()),
                WidgetTarget(AppleLiquidMonoWidget::class.java, AppleLiquidMonoWidget()),
                WidgetTarget(AppleLiquidDuoWidget::class.java, AppleLiquidDuoWidget()),
                WidgetTarget(AppleLiquidShelfWidget::class.java, AppleLiquidShelfWidget()),
                WidgetTarget(AppleLiquidNowWidget::class.java, AppleLiquidNowWidget()),
                WidgetTarget(AppleLiquidLyricsWidget::class.java, AppleLiquidLyricsWidget()),
                WidgetTarget(PlaybackSpotlightWidget::class.java, PlaybackSpotlightWidget()),
                WidgetTarget(ZenTanzakuWidget::class.java, ZenTanzakuWidget()),
            )

        val progressWidgets =
            listOf(
                WidgetTarget(AppleLockscreenWidget::class.java, AppleLockscreenWidget()),
                WidgetTarget(AppleLiquidCompactWidget::class.java, AppleLiquidCompactWidget()),
                WidgetTarget(AppleLiquidPillWidget::class.java, AppleLiquidPillWidget()),
                WidgetTarget(AppleLiquidHeroWidget::class.java, AppleLiquidHeroWidget()),
                WidgetTarget(AppleLiquidMiniWidget::class.java, AppleLiquidMiniWidget()),
                WidgetTarget(AppleLiquidSplitWidget::class.java, AppleLiquidSplitWidget()),
                WidgetTarget(AppleLiquidPosterWidget::class.java, AppleLiquidPosterWidget()),
                WidgetTarget(AppleLiquidIslandWidget::class.java, AppleLiquidIslandWidget()),
                WidgetTarget(AppleLiquidOrbWidget::class.java, AppleLiquidOrbWidget()),
                WidgetTarget(AppleLiquidPanoWidget::class.java, AppleLiquidPanoWidget()),
                WidgetTarget(AppleLiquidMonoWidget::class.java, AppleLiquidMonoWidget()),
                WidgetTarget(AppleLiquidDuoWidget::class.java, AppleLiquidDuoWidget()),
                WidgetTarget(AppleLiquidShelfWidget::class.java, AppleLiquidShelfWidget()),
                WidgetTarget(AppleLiquidNowWidget::class.java, AppleLiquidNowWidget()),
                WidgetTarget(AppleLiquidLyricsWidget::class.java, AppleLiquidLyricsWidget()),
                WidgetTarget(PlaybackSpotlightWidget::class.java, PlaybackSpotlightWidget()),
                WidgetTarget(ZenTanzakuWidget::class.java, ZenTanzakuWidget()),
            )
    }
}

/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.ui.screens.lockscreen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.launch
import moe.rukamori.archivetune.R
import moe.rukamori.archivetune.lyrics.LyricsEntry
import moe.rukamori.archivetune.lyrics.LyricsUtils
import moe.rukamori.archivetune.models.MediaMetadata
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun LockscreenLyricsPlayerContent(
    mediaMetadata: MediaMetadata,
    isPlaying: Boolean,
    position: Long,
    duration: Long,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    lyrics: List<LyricsEntry>,
    hasLyrics: Boolean,
    onPlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 1. Dominant Artwork Color Extraction
    var dominantColor by remember { mutableStateOf<Color?>(null) }
    LaunchedEffect(mediaMetadata.thumbnailUrl) {
        val url = mediaMetadata.thumbnailUrl ?: return@LaunchedEffect
        val request =
            ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()
        val result = context.imageLoader.execute(request)
        val bitmap = result.image?.toBitmap() ?: return@LaunchedEffect
        Palette.from(bitmap).generate { palette ->
            dominantColor = palette?.dominantSwatch?.rgb?.let { Color(it) }
                ?: palette?.vibrantSwatch?.rgb?.let { Color(it) }
        }
    }

    // 2. Audio Volume Management
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val maxVolume = remember(audioManager) {
        audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.coerceAtLeast(1) ?: 15
    }
    var currentVolumeProgress by remember {
        mutableFloatStateOf(
            ((audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: (maxVolume / 2)).toFloat() / maxVolume.toFloat())
                .coerceIn(0f, 1f),
        )
    }

    DisposableEffect(context, audioManager, maxVolume) {
        val receiver =
            object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context?,
                    intent: Intent?,
                ) {
                    val cur = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: return
                    currentVolumeProgress = (cur.toFloat() / maxVolume.toFloat()).coerceIn(0f, 1f)
                }
            }
        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {
            }
        }
    }

    // 3. Playback Position & Scrubber Calculations
    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableFloatStateOf(0f) }

    val currentPositionMs = if (isSeeking) (seekPosition * duration).toLong() else position
    val safeDuration = duration.coerceAtLeast(1L)
    val progressFraction = (currentPositionMs.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)

    val elapsedSeconds = (currentPositionMs / 1000L).coerceAtLeast(0L)
    val remainingSeconds = ((safeDuration - currentPositionMs) / 1000L).coerceAtLeast(0L)
    val elapsedStr = "${elapsedSeconds / 60}:${(elapsedSeconds % 60).toString().padStart(2, '0')}"
    val remainingStr = "-${remainingSeconds / 60}:${(remainingSeconds % 60).toString().padStart(2, '0')}"

    // 4. Swipe-to-Dismiss / Drag Dismissal
    val dismissOffsetY = remember { Animatable(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }

    // Pulsing Ambient Backglow
    val infiniteTransition = rememberInfiniteTransition(label = "ambientGlow")
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.40f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "auraAlpha",
    )

    // Active Lyric Index Tracking & Auto-Scroll
    val currentLineIndex = remember(lyrics, currentPositionMs) {
        if (lyrics.isEmpty()) -1 else LyricsUtils.findCurrentLineIndex(lyrics, currentPositionMs, leadMs = 300L)
    }
    val lazyListState = rememberLazyListState()

    LaunchedEffect(currentLineIndex) {
        if (currentLineIndex >= 0 && !lazyListState.isScrollInProgress) {
            val target = (currentLineIndex - 1).coerceAtLeast(0)
            lazyListState.animateScrollToItem(
                index = target,
                scrollOffset = 0,
            )
        }
    }

    // Root Container: Translucent Scrim with Clearance for System Clock
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color(0x35000000))
                .statusBarsPadding()
                .navigationBarsPadding()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        contentAlignment = Alignment.Center,
    ) {
        // Floating 4x4 Liquid Glass Lyrics Deck
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .offset { IntOffset(0, dismissOffsetY.value.roundToInt()) }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { _, dragAmount ->
                                totalDragY += dragAmount
                                scope.launch {
                                    dismissOffsetY.snapTo(totalDragY)
                                }
                            },
                            onDragEnd = {
                                if (abs(totalDragY) > 120f) {
                                    scope.launch {
                                        dismissOffsetY.animateTo(
                                            targetValue = if (totalDragY < 0) -1000f else 1000f,
                                            animationSpec = tween(200, easing = FastOutSlowInEasing),
                                        )
                                        onDismiss()
                                    }
                                } else {
                                    scope.launch {
                                        dismissOffsetY.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(220, easing = FastOutSlowInEasing),
                                        )
                                        totalDragY = 0f
                                    }
                                }
                            },
                            onDragCancel = {
                                scope.launch {
                                    dismissOffsetY.animateTo(0f)
                                    totalDragY = 0f
                                }
                            },
                        )
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
        ) {
            // Dynamic Liquid Ambient Aura behind card
            if (dominantColor != null) {
                val aura = dominantColor!!
                Box(
                    modifier =
                        Modifier
                            .size(360.dp)
                            .align(Alignment.Center)
                            .graphicsLayer {
                                alpha = auraAlpha
                            }
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            aura.copy(alpha = 0.55f),
                                            aura.copy(alpha = 0.15f),
                                            Color.Transparent,
                                        ),
                                    ),
                                )
                            },
                )
            }

            // Apple Liquid Glass 4x4 Card Frame
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .shadow(28.dp, RoundedCornerShape(30.dp))
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color(0x60FFFFFF)) // Layer 1: Outer specular glass rim
                        .padding(1.2.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(29.dp))
                            .background(Color(0x30000000)) // Layer 2: 3D refraction shadow
                            .padding(0.8.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(28.dp))
                                .background(Color(0x6012131D)), // Layer 3: Smoked crystal acrylic base
                    ) {
                        // Layer 4: Dynamic Artwork Dye
                        if (dominantColor != null) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .matchParentSize()
                                        .background(dominantColor!!.copy(alpha = 0.28f)),
                            )
                        }

                        // Layer 5: Glass Content & Controls
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                        ) {
                            // ─────────────────────────────────────────────────────────────
                            // TOP: SYNCHRONIZED LYRICS STAGE
                            // ─────────────────────────────────────────────────────────────
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(22.dp))
                                        .background(Color(0x28FFFFFF))
                                        .padding(12.dp),
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Stage Header: Artwork Thumbnail, Badge & AirPlay Pill
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        // Mini Artwork Thumbnail
                                        Box(
                                            modifier =
                                                Modifier
                                                    .size(28.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0x40FFFFFF))
                                                    .padding(0.8.dp),
                                        ) {
                                            AsyncImage(
                                                model = mediaMetadata.thumbnailUrl,
                                                contentDescription = mediaMetadata.title,
                                                contentScale = ContentScale.Crop,
                                                modifier =
                                                    Modifier
                                                        .fillMaxSize()
                                                        .clip(RoundedCornerShape(7.5.dp)),
                                            )
                                        }

                                        Spacer(Modifier.width(8.dp))

                                        // Synced Lyrics Badge
                                        Box(
                                            modifier =
                                                Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0x55000000))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                        ) {
                                            Text(
                                                text = if (hasLyrics) "SYNCED LYRICS" else "LIVE PLAYER",
                                                color = Color.White.copy(alpha = 0.75f),
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }

                                        Spacer(Modifier.weight(1f))

                                        // AirPlay Capsule
                                        Row(
                                            modifier =
                                                Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color(0x55000000))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_apple_airplay),
                                                contentDescription = "AirPlay",
                                                tint = Color(0xFF38A3FF),
                                                modifier = Modifier.size(11.dp),
                                            )
                                            Spacer(Modifier.width(3.5.dp))
                                            Text(
                                                text = "AirPlay",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    // Lyrics Waterfall Stage
                                    if (hasLyrics && lyrics.isNotEmpty()) {
                                        LazyColumn(
                                            state = lazyListState,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(min = 180.dp, max = 220.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            itemsIndexed(lyrics) { index, item ->
                                                val isActive = index == currentLineIndex
                                                val distance = abs(index - currentLineIndex)

                                                if (isActive) {
                                                    // Active Line: Illuminated Hero Focus Capsule
                                                    Row(
                                                        modifier =
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .background(Color(0x35FFFFFF))
                                                                .clickable { onSeek(item.time) }
                                                                .padding(horizontal = 10.dp, vertical = 7.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                    ) {
                                                        Box(
                                                            modifier =
                                                                Modifier
                                                                    .width(3.5.dp)
                                                                    .height(20.dp)
                                                                    .clip(RoundedCornerShape(2.dp))
                                                                    .background(Color.White),
                                                        )
                                                        Spacer(Modifier.width(8.dp))
                                                        Text(
                                                            text = item.text,
                                                            color = Color.White,
                                                            fontSize = 17.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.weight(1f),
                                                        )
                                                    }
                                                } else {
                                                    val alpha =
                                                        when (distance) {
                                                            1 -> 0.52f
                                                            2 -> 0.30f
                                                            else -> 0.18f
                                                        }
                                                    val fontSize =
                                                        when (distance) {
                                                            1 -> 13.5.sp
                                                            2 -> 11.5.sp
                                                            else -> 10.5.sp
                                                        }
                                                    val weight = if (distance == 1) FontWeight.Medium else FontWeight.Normal

                                                    Text(
                                                        text = item.text,
                                                        color = Color.White.copy(alpha = alpha),
                                                        fontSize = fontSize,
                                                        fontWeight = weight,
                                                        modifier =
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .clickable { onSeek(item.time) }
                                                                .padding(horizontal = 12.dp, vertical = 3.dp),
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // Elegant Placeholder when instrumental or loading
                                        Column(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(min = 160.dp, max = 200.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                        ) {
                                            Text(
                                                text = "♪",
                                                color = Color.White.copy(alpha = 0.85f),
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = mediaMetadata.title.ifBlank { context.getString(R.string.app_name) },
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                textAlign = TextAlign.Center,
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = mediaMetadata.artists.joinToString { it.name }.ifBlank { context.getString(R.string.lyrics) },
                                                color = Color.White.copy(alpha = 0.65f),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Normal,
                                                maxLines = 1,
                                                textAlign = TextAlign.Center,
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            // ─────────────────────────────────────────────────────────────
                            // BOTTOM: CRYSTALLINE GLASS CONTROL SHELF
                            // ─────────────────────────────────────────────────────────────
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(22.dp))
                                        .background(Color(0x35FFFFFF))
                                        .padding(12.dp),
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Title & Artist
                                    Text(
                                        text = mediaMetadata.title.ifBlank { context.getString(R.string.app_name) },
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        modifier = Modifier.basicMarquee(),
                                    )
                                    Text(
                                        text = mediaMetadata.artists.joinToString { it.name }.ifBlank { context.getString(R.string.no_track_playing) },
                                        color = Color.White.copy(alpha = 0.75f),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Normal,
                                        maxLines = 1,
                                        modifier = Modifier.basicMarquee(),
                                    )

                                    Spacer(Modifier.height(6.dp))

                                    // Interactive Scrubber
                                    Slider(
                                        value = if (isSeeking) seekPosition else progressFraction,
                                        onValueChange = {
                                            isSeeking = true
                                            seekPosition = it
                                        },
                                        onValueChangeFinished = {
                                            isSeeking = false
                                            onSeek((seekPosition * safeDuration).toLong())
                                        },
                                        colors =
                                            SliderDefaults.colors(
                                                thumbColor = Color.White,
                                                activeTrackColor = Color.White,
                                                inactiveTrackColor = Color.White.copy(alpha = 0.22f),
                                            ),
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height(18.dp),
                                    )

                                    // Timestamps
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            text = elapsedStr,
                                            color = Color.White.copy(alpha = 0.65f),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Normal,
                                        )
                                        Text(
                                            text = remainingStr,
                                            color = Color.White.copy(alpha = 0.65f),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Normal,
                                        )
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    // Full 5-element Control Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        // Volume Down
                                        Box(
                                            modifier =
                                                Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0)
                                                        val cur = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: return@clickable
                                                        currentVolumeProgress = (cur.toFloat() / maxVolume.toFloat()).coerceIn(0f, 1f)
                                                    },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_apple_volume_min),
                                                contentDescription = "Volume Down",
                                                tint = Color.White.copy(alpha = 0.70f),
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }

                                        Spacer(Modifier.weight(1f))

                                        // Skip Previous
                                        Box(
                                            modifier =
                                                Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .clickable(enabled = canSkipPrevious, onClick = onSkipPrevious),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_apple_backward),
                                                contentDescription = "Previous",
                                                tint = if (canSkipPrevious) Color.White else Color.White.copy(alpha = 0.35f),
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }

                                        Spacer(Modifier.width(14.dp))

                                        // Play / Pause Liquid Lens
                                        Box(
                                            modifier =
                                                Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0x60FFFFFF))
                                                    .padding(1.dp)
                                                    .clickable(onClick = onPlayPause),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Box(
                                                modifier =
                                                    Modifier
                                                        .fillMaxSize()
                                                        .clip(CircleShape)
                                                        .background(dominantColor?.copy(alpha = 0.40f) ?: Color(0x35FFFFFF)),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    painter =
                                                        painterResource(
                                                            if (isPlaying) R.drawable.ic_apple_pause else R.drawable.ic_apple_play,
                                                        ),
                                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(22.dp),
                                                )
                                            }
                                        }

                                        Spacer(Modifier.width(14.dp))

                                        // Skip Next
                                        Box(
                                            modifier =
                                                Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .clickable(enabled = canSkipNext, onClick = onSkipNext),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_apple_forward),
                                                contentDescription = "Next",
                                                tint = if (canSkipNext) Color.White else Color.White.copy(alpha = 0.35f),
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }

                                        Spacer(Modifier.weight(1f))

                                        // Volume Up
                                        Box(
                                            modifier =
                                                Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0)
                                                        val cur = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: return@clickable
                                                        currentVolumeProgress = (cur.toFloat() / maxVolume.toFloat()).coerceIn(0f, 1f)
                                                    },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_apple_volume_max),
                                                contentDescription = "Volume Up",
                                                tint = Color.White.copy(alpha = 0.70f),
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

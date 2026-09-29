/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import moe.rukamori.archivetune.R

class AppleLiquidLyricsWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            AppleLiquidLyricsContent(context)
        }
    }
}

@Composable
private fun AppleLiquidLyricsContent(context: Context) {
    val prefs = currentState<Preferences>()
    val state = prefs.toWidgetPlaybackState(context)

    val activeLyric = prefs[MusicWidgetKeys.LYRIC_ACTIVE]?.takeIf { it.isNotBlank() }
    val prevLyric = prefs[MusicWidgetKeys.LYRIC_PREV]?.takeIf { it.isNotBlank() }
    val prevLyric2 = prefs[MusicWidgetKeys.LYRIC_PREV2]?.takeIf { it.isNotBlank() }
    val nextLyric = prefs[MusicWidgetKeys.LYRIC_NEXT]?.takeIf { it.isNotBlank() }
    val nextLyric2 = prefs[MusicWidgetKeys.LYRIC_NEXT2]?.takeIf { it.isNotBlank() }
    val hasLyrics = prefs[MusicWidgetKeys.HAS_LYRICS] ?: (activeLyric != null)

    val dominant = state.dominantColor?.let { Color(it) }

    val liquidAuraColor = remember(dominant) { dominant?.copy(alpha = 0.16f) ?: Color.Transparent }
    val frostedPillBg = Color(0x30FFFFFF) // Translucent glass capsules
    val liquidLensBg = remember(dominant) {
        dominant?.let {
            val r = (it.red * 0.40f + 1f * 0.60f).coerceIn(0f, 1f)
            val g = (it.green * 0.40f + 1f * 0.60f).coerceIn(0f, 1f)
            val b = (it.blue * 0.40f + 1f * 0.60f).coerceIn(0f, 1f)
            Color(red = r, green = g, blue = b, alpha = 0.50f)
        } ?: Color(0x40FFFFFF)
    }

    val textPrimary = ColorProvider(Color.White)
    val textSecondary = ColorProvider(Color(0xD0FFFFFF))
    val textTertiary = ColorProvider(Color(0x90FFFFFF))
    val textMuted = ColorProvider(Color(0x60FFFFFF))
    val scrubberTrack = ColorProvider(Color(0x28FFFFFF))
    val scrubberFill = ColorProvider(Color.White)

    val elapsedSec = if (state.durationMs > 0L) (state.positionMs / 1000L).toInt() else 0
    val totalSec = if (state.durationMs > 0L) (state.durationMs / 1000L).toInt() else 0
    val remainingSec = (totalSec - elapsedSec).coerceAtLeast(0)
    val elapsedStr = if (state.isAvailable && state.durationMs > 0L) "${elapsedSec / 60}:${(elapsedSec % 60).toString().padStart(2, '0')}" else "0:00"
    val remainingStr = if (state.isAvailable && totalSec > 0) "-${remainingSec / 60}:${(remainingSec % 60).toString().padStart(2, '0')}" else "-0:00"

    // Apple Dock Crystal Glass Background (Multi-layer XML with Specular Rim & Gradient)
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_apple_dock_glass_bg))
            .cornerRadius(32.dp)
            .clickable(openArchiveTuneAction(context)),
    ) {
        // Dynamic Liquid Artwork Dye Layer
        if (state.dominantColor != null) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(liquidAuraColor)
                    .cornerRadius(30.dp),
            ) {}
        }

                    // Layer 6: Unified Single-Piece Glass Deck (Seamless on Crystal Glass)
                    Column(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .padding(13.dp),
                    ) {
                        // ─────────────────────────────────────────────────────────────
                        // TOP: SYNCHRONIZED LYRICS STAGE (Seamless on Crystal Glass)
                        // ─────────────────────────────────────────────────────────────
                        Column(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .defaultWeight(),
                        ) {
                            // Stage Header: Artwork Thumbnail, Synced Badge, & AirPlay Pill
                            Row(
                                modifier = GlanceModifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Mini Squircle Artwork Thumbnail
                                Box(
                                    modifier = GlanceModifier
                                        .size(24.dp)
                                        .background(Color(0x60FFFFFF))
                                        .cornerRadius(7.dp)
                                        .padding(0.8.dp),
                                ) {
                                    WidgetAlbumArt(
                                        artPath = prefs[MusicWidgetKeys.ART_PATH],
                                        modifier = GlanceModifier
                                            .fillMaxSize()
                                            .cornerRadius(6.2.dp),
                                        contentDescription = state.title,
                                        targetSize = 24.dp,
                                    )
                                }

                                Spacer(GlanceModifier.width(8.dp))

                                // Synced Lyrics Badge (Frosted Translucent Capsule)
                                Box(
                                    modifier = GlanceModifier
                                        .background(frostedPillBg)
                                        .cornerRadius(8.dp)
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                ) {
                                    Text(
                                        text = if (hasLyrics) "SYNCED LYRICS" else "LIVE PLAYER",
                                        style = TextStyle(
                                            color = textPrimary,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                    )
                                }

                                Spacer(GlanceModifier.defaultWeight())

                                // AirPlay Capsule (Frosted Translucent Capsule)
                                Row(
                                    modifier = GlanceModifier
                                        .background(frostedPillBg)
                                        .cornerRadius(10.dp)
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_apple_airplay),
                                        contentDescription = "AirPlay",
                                        colorFilter = ColorFilter.tint(ColorProvider(Color(0xFF38A3FF))),
                                        modifier = GlanceModifier.size(10.dp),
                                    )
                                    Spacer(GlanceModifier.width(3.dp))
                                    Text(
                                        text = "AirPlay",
                                        style = TextStyle(
                                            color = textPrimary,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                    )
                                }
                            }

                            Spacer(GlanceModifier.height(8.dp))

                            // Centered Lyrics Flow (Directly on Glass)
                            Column(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .defaultWeight(),
                                verticalAlignment = Alignment.Vertical.CenterVertically,
                            ) {
                                if (hasLyrics && activeLyric != null) {
                                    // 2nd Previous Lyric Line (deep context, 30% alpha)
                                    if (prevLyric2 != null) {
                                        Text(
                                            text = prevLyric2,
                                            maxLines = 1,
                                            style = TextStyle(
                                                color = ColorProvider(Color(0x55FFFFFF)),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Normal,
                                            ),
                                            modifier = GlanceModifier.padding(horizontal = 6.dp),
                                        )
                                        Spacer(GlanceModifier.height(3.dp))
                                    }

                                    // Previous Lyric Line (context, 52% alpha)
                                    if (prevLyric != null) {
                                        Text(
                                            text = prevLyric,
                                            maxLines = 1,
                                            style = TextStyle(
                                                color = ColorProvider(Color(0x8AFFFFFF)),
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Medium,
                                            ),
                                            modifier = GlanceModifier.padding(horizontal = 6.dp),
                                        )
                                        Spacer(GlanceModifier.height(5.dp))
                                    }

                                    // Active Synchronized Lyric Line (Hero Focus Capsule with Indicator Bar)
                                    Row(
                                        modifier = GlanceModifier
                                            .fillMaxWidth()
                                            .background(Color(0x38FFFFFF))
                                            .cornerRadius(12.dp)
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            modifier = GlanceModifier
                                                .width(3.dp)
                                                .height(18.dp)
                                                .background(Color.White)
                                                .cornerRadius(1.5.dp),
                                        ) {}
                                        Spacer(GlanceModifier.width(8.dp))
                                        Text(
                                            text = activeLyric,
                                            maxLines = 3,
                                            style = TextStyle(
                                                color = textPrimary,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            modifier = GlanceModifier.defaultWeight(),
                                        )
                                    }

                                    // Next Lyric Line (anticipation, 55% alpha)
                                    if (nextLyric != null) {
                                        Spacer(GlanceModifier.height(5.dp))
                                        Text(
                                            text = nextLyric,
                                            maxLines = 1,
                                            style = TextStyle(
                                                color = ColorProvider(Color(0x8EFFFFFF)),
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Medium,
                                            ),
                                            modifier = GlanceModifier.padding(horizontal = 6.dp),
                                        )
                                    }

                                    // Upcoming 2nd Next Line (deep perspective, 30% alpha)
                                    if (nextLyric2 != null) {
                                        Spacer(GlanceModifier.height(3.dp))
                                        Text(
                                            text = nextLyric2,
                                            maxLines = 1,
                                            style = TextStyle(
                                                color = ColorProvider(Color(0x55FFFFFF)),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Normal,
                                            ),
                                            modifier = GlanceModifier.padding(horizontal = 6.dp),
                                        )
                                    }
                                } else {
                                    // Elegant Placeholder when instrumental or lyrics loading
                                    Column(
                                        modifier = GlanceModifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                                    ) {
                                        Text(
                                            text = if (state.isAvailable) "♪" else "ArchiveTune",
                                            style = TextStyle(
                                                color = textSecondary,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                        )
                                        Spacer(GlanceModifier.height(4.dp))
                                        Text(
                                            text = if (state.isAvailable) {
                                                state.title
                                            } else {
                                                context.getString(R.string.no_track_playing)
                                            },
                                            maxLines = 1,
                                            style = TextStyle(
                                                color = textPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                        )
                                        Spacer(GlanceModifier.height(2.dp))
                                        Text(
                                            text = if (state.isAvailable) {
                                                if (state.artist.isNotBlank()) state.artist else context.getString(R.string.lyrics)
                                            } else {
                                                context.getString(R.string.play)
                                            },
                                            maxLines = 1,
                                            style = TextStyle(
                                                color = textTertiary,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Normal,
                                            ),
                                        )
                                    }
                                }
                            }
                        }

                        // Subtle Hairline Frosted Separator
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0x20FFFFFF)),
                        ) {}

                        Spacer(GlanceModifier.height(10.dp))

                        // ─────────────────────────────────────────────────────────────
                        // BOTTOM: CONTROLS SHELF (Seamless on Crystal Glass)
                        // ─────────────────────────────────────────────────────────────
                        Column(modifier = GlanceModifier.fillMaxWidth()) {
                            // Title & Artist
                            Text(
                                text = if (state.isAvailable) state.title else context.getString(R.string.app_name),
                                maxLines = 1,
                                style = TextStyle(
                                    color = textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                            Text(
                                text = if (state.isAvailable) state.artist else context.getString(R.string.no_track_playing),
                                maxLines = 1,
                                style = TextStyle(
                                    color = textSecondary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Normal,
                                ),
                            )

                            Spacer(GlanceModifier.height(8.dp))

                            // Scrubber
                            LinearProgressIndicator(
                                progress = if (state.isAvailable) state.playbackPosition else 0f,
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .height(3.5.dp)
                                    .cornerRadius(2.dp),
                                color = scrubberFill,
                                backgroundColor = scrubberTrack,
                            )

                            Spacer(GlanceModifier.height(2.dp))

                            Row(modifier = GlanceModifier.fillMaxWidth()) {
                                Text(
                                    text = elapsedStr,
                                    style = TextStyle(color = textTertiary, fontSize = 8.5.sp),
                                )
                                Spacer(GlanceModifier.defaultWeight())
                                Text(
                                    text = remainingStr,
                                    style = TextStyle(color = textTertiary, fontSize = 8.5.sp),
                                )
                            }

                            Spacer(GlanceModifier.height(8.dp))

                            // Full 5-element Control Row
                            Row(
                                modifier = GlanceModifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = GlanceModifier.size(32.dp).clickable(volumeDownAction()),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_apple_volume_min),
                                        contentDescription = "Volume Down",
                                        colorFilter = ColorFilter.tint(textSecondary),
                                        modifier = GlanceModifier.size(16.dp),
                                    )
                                }

                                Spacer(GlanceModifier.defaultWeight())

                                Box(
                                    modifier = GlanceModifier.size(36.dp).clickable(skipPreviousAction()),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_apple_backward),
                                        contentDescription = "Previous",
                                        colorFilter = ColorFilter.tint(textPrimary),
                                        modifier = GlanceModifier.size(20.dp),
                                    )
                                }

                                Spacer(GlanceModifier.width(12.dp))

                                Box(
                                    modifier = GlanceModifier
                                        .size(46.dp)
                                        .background(Color(0x80FFFFFF))
                                        .cornerRadius(23.dp)
                                        .padding(1.dp)
                                        .clickable(playPauseAction()),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Box(
                                        modifier = GlanceModifier
                                            .fillMaxSize()
                                            .background(liquidLensBg)
                                            .cornerRadius(22.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Image(
                                            provider = ImageProvider(
                                                if (state.isPlaying) R.drawable.ic_apple_pause else R.drawable.ic_apple_play,
                                            ),
                                            contentDescription = if (state.isPlaying) "Pause" else "Play",
                                            colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                            modifier = GlanceModifier.size(22.dp),
                                        )
                                    }
                                }

                                Spacer(GlanceModifier.width(12.dp))

                                Box(
                                    modifier = GlanceModifier.size(36.dp).clickable(skipNextAction()),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_apple_forward),
                                        contentDescription = "Next",
                                        colorFilter = ColorFilter.tint(textPrimary),
                                        modifier = GlanceModifier.size(20.dp),
                                    )
                                }

                                Spacer(GlanceModifier.defaultWeight())

                                Box(
                                    modifier = GlanceModifier.size(32.dp).clickable(volumeUpAction()),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_apple_volume_max),
                                        contentDescription = "Volume Up",
                                        colorFilter = ColorFilter.tint(textSecondary),
                                        modifier = GlanceModifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
    }
}


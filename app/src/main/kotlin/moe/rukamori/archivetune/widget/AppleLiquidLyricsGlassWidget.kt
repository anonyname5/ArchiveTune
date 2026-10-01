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

/**
 * AppleLiquidLyricsGlassWidget
 * True Apple Liquid Glass with wet specular curved glare, fluid color caustics,
 * clean seamless floating typography, and crystalline dock controls.
 */
class AppleLiquidLyricsGlassWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            AppleLiquidLyricsGlassContent(context)
        }
    }
}

@Composable
private fun AppleLiquidLyricsGlassContent(context: Context) {
    val prefs = currentState<Preferences>()
    val state = prefs.toWidgetPlaybackState(context)

    val activeLyric = prefs[MusicWidgetKeys.LYRIC_ACTIVE]?.takeIf { it.isNotBlank() }
    val prevLyric = prefs[MusicWidgetKeys.LYRIC_PREV]?.takeIf { it.isNotBlank() }
    val prevLyric2 = prefs[MusicWidgetKeys.LYRIC_PREV2]?.takeIf { it.isNotBlank() }
    val nextLyric = prefs[MusicWidgetKeys.LYRIC_NEXT]?.takeIf { it.isNotBlank() }
    val nextLyric2 = prefs[MusicWidgetKeys.LYRIC_NEXT2]?.takeIf { it.isNotBlank() }
    val hasLyrics = prefs[MusicWidgetKeys.HAS_LYRICS] ?: (activeLyric != null)

    val dominant = state.dominantColor?.let { Color(it) }

    // Dynamic Liquid Dye Color: Infuses fluid album color into the glass sheet
    val liquidDyeColor = remember(dominant) {
        dominant?.copy(alpha = 0.35f) ?: Color.Transparent
    }

    // Lens button liquid accent
    val playLensBg = remember(dominant) {
        dominant?.let {
            val r = (it.red * 0.5f + 1f * 0.5f).coerceIn(0f, 1f)
            val g = (it.green * 0.5f + 1f * 0.5f).coerceIn(0f, 1f)
            val b = (it.blue * 0.5f + 1f * 0.5f).coerceIn(0f, 1f)
            Color(red = r, green = g, blue = b, alpha = 0.55f)
        } ?: Color(0x55FFFFFF)
    }

    val textPrimary = ColorProvider(Color.White)
    val textSecondary = ColorProvider(Color(0xE0FFFFFF))
    val textTertiary = ColorProvider(Color(0x95FFFFFF))
    val textMuted = ColorProvider(Color(0x50FFFFFF))
    val scrubberTrack = ColorProvider(Color(0x28FFFFFF))
    val scrubberFill = ColorProvider(Color.White)

    val elapsedSec = if (state.durationMs > 0L) (state.positionMs / 1000L).toInt() else 0
    val totalSec = if (state.durationMs > 0L) (state.durationMs / 1000L).toInt() else 0
    val remainingSec = (totalSec - elapsedSec).coerceAtLeast(0)
    val elapsedStr = if (state.isAvailable && state.durationMs > 0L) "${elapsedSec / 60}:${(elapsedSec % 60).toString().padStart(2, '0')}" else "0:00"
    val remainingStr = if (state.isAvailable && totalSec > 0) "-${remainingSec / 60}:${(remainingSec % 60).toString().padStart(2, '0')}" else "-0:00"

    // MAIN LIQUID GLASS SHEET (Drawable with specular top glare & optical perimeter rim)
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_apple_liquid_glass_main_bg))
            .cornerRadius(30.dp)
            .clickable(openArchiveTuneAction(context)),
    ) {
        // Fluid album art dye layer (bleeds through the liquid glass)
        if (state.dominantColor != null) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(liquidDyeColor)
                    .cornerRadius(28.dp),
            ) {}
        }

        // Internal Content Layout
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(14.dp),
        ) {
            // ─────────────────────────────────────────────────────────────
            // TOP BAR: Floating Glass Header (Artwork + Track info + Glass Pill)
            // ─────────────────────────────────────────────────────────────
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Glossy Artwork Squircle
                Box(
                    modifier = GlanceModifier
                        .size(42.dp)
                        .background(Color(0x70FFFFFF))
                        .cornerRadius(12.dp)
                        .padding(1.dp),
                ) {
                    WidgetAlbumArt(
                        artPath = prefs[MusicWidgetKeys.ART_PATH],
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .cornerRadius(11.dp),
                        contentDescription = state.title,
                        targetSize = 42.dp,
                    )
                }

                Spacer(GlanceModifier.width(10.dp))

                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = if (state.isAvailable) state.title else context.getString(R.string.app_name),
                        maxLines = 1,
                        style = TextStyle(
                            color = textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    Spacer(GlanceModifier.height(1.dp))
                    Text(
                        text = if (state.isAvailable) state.artist else context.getString(R.string.no_track_playing),
                        maxLines = 1,
                        style = TextStyle(
                            color = textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                    )
                }

                Spacer(GlanceModifier.width(8.dp))

                // Sleek Liquid Glass Pill
                Row(
                    modifier = GlanceModifier
                        .background(ImageProvider(R.drawable.widget_apple_liquid_capsule_bg))
                        .cornerRadius(12.dp)
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_apple_airplay),
                        contentDescription = "AirPlay",
                        colorFilter = ColorFilter.tint(ColorProvider(Color(0xFF38A3FF))),
                        modifier = GlanceModifier.size(10.5.dp),
                    )
                    Spacer(GlanceModifier.width(4.dp))
                    Text(
                        text = if (hasLyrics) "LYRICS" else "AUDIO",
                        style = TextStyle(
                            color = textPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }

            Spacer(GlanceModifier.height(10.dp))

            // ─────────────────────────────────────────────────────────────
            // CENTER: FLOATING LIQUID LYRICS STAGE
            // ─────────────────────────────────────────────────────────────
            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.Start,
            ) {
                if (hasLyrics && activeLyric != null) {
                    // Context lyric before (faded optical perspective)
                    if (prevLyric != null) {
                        Text(
                            text = prevLyric,
                            maxLines = 1,
                            style = TextStyle(
                                color = textMuted,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Normal,
                            ),
                            modifier = GlanceModifier.padding(horizontal = 8.dp),
                        )
                        Spacer(GlanceModifier.height(6.dp))
                    }

                    // HERO ACTIVE LYRIC (Liquid Glass Drop Highlight)
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .background(ImageProvider(R.drawable.widget_apple_liquid_hero_lyric_bg))
                            .cornerRadius(14.dp)
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                    ) {
                        Text(
                            text = activeLyric,
                            maxLines = 2,
                            style = TextStyle(
                                color = textPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }

                    // Next Line (Anticipation)
                    if (nextLyric != null) {
                        Spacer(GlanceModifier.height(6.dp))
                        Text(
                            text = nextLyric,
                            maxLines = 1,
                            style = TextStyle(
                                color = textTertiary,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                            modifier = GlanceModifier.padding(horizontal = 8.dp),
                        )
                    }

                    // Subsequent Line (Soft tail)
                    if (nextLyric2 != null) {
                        Spacer(GlanceModifier.height(4.dp))
                        Text(
                            text = nextLyric2,
                            maxLines = 1,
                            style = TextStyle(
                                color = textMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                            ),
                            modifier = GlanceModifier.padding(horizontal = 8.dp),
                        )
                    }
                } else if (hasLyrics && activeLyric == null && nextLyric != null) {
                    // Intro State
                    Column(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                    ) {
                        Text(
                            text = "♪  Intro",
                            style = TextStyle(
                                color = textTertiary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                        Spacer(GlanceModifier.height(6.dp))
                        Text(
                            text = nextLyric,
                            maxLines = 2,
                            style = TextStyle(
                                color = textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                            modifier = GlanceModifier.padding(horizontal = 10.dp),
                        )
                    }
                } else {
                    // Idle / Instrumental
                    Column(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                    ) {
                        Text(
                            text = "♪  " + if (state.isAvailable) "Instrumental" else "ArchiveTune",
                            style = TextStyle(
                                color = textTertiary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                    }
                }
            }

            Spacer(GlanceModifier.height(8.dp))

            // ─────────────────────────────────────────────────────────────
            // BOTTOM: FLOATING LIQUID DOCK CAPSULE (Scrubber & Controls)
            // ─────────────────────────────────────────────────────────────
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(ImageProvider(R.drawable.widget_apple_liquid_capsule_bg))
                    .cornerRadius(22.dp)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Column(modifier = GlanceModifier.fillMaxWidth()) {
                    // Scrubber Bar
                    LinearProgressIndicator(
                        progress = if (state.isAvailable) state.playbackPosition else 0f,
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .cornerRadius(1.5.dp),
                        color = scrubberFill,
                        backgroundColor = scrubberTrack,
                    )

                    Spacer(GlanceModifier.height(2.dp))

                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        Text(
                            text = elapsedStr,
                            style = TextStyle(color = textTertiary, fontSize = 8.sp),
                        )
                        Spacer(GlanceModifier.defaultWeight())
                        Text(
                            text = remainingStr,
                            style = TextStyle(color = textTertiary, fontSize = 8.sp),
                        )
                    }

                    Spacer(GlanceModifier.height(4.dp))

                    // Control buttons
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = GlanceModifier.size(28.dp).clickable(volumeDownAction()),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_apple_volume_min),
                                contentDescription = "Volume Down",
                                colorFilter = ColorFilter.tint(textSecondary),
                                modifier = GlanceModifier.size(13.5.dp),
                            )
                        }

                        Spacer(GlanceModifier.defaultWeight())

                        Box(
                            modifier = GlanceModifier.size(32.dp).clickable(skipPreviousAction()),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_apple_backward),
                                contentDescription = "Previous",
                                colorFilter = ColorFilter.tint(textPrimary),
                            )
                        }

                        Spacer(GlanceModifier.width(12.dp))

                        // Play/Pause Liquid Lens Button
                        Box(
                            modifier = GlanceModifier
                                .size(40.dp)
                                .background(Color(0x80FFFFFF))
                                .cornerRadius(20.dp)
                                .padding(1.dp)
                                .clickable(playPauseAction()),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = GlanceModifier
                                    .fillMaxSize()
                                    .background(playLensBg)
                                    .cornerRadius(19.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    provider = ImageProvider(
                                        if (state.isPlaying) R.drawable.ic_apple_pause else R.drawable.ic_apple_play,
                                    ),
                                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                                    colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                    modifier = GlanceModifier.size(19.dp),
                                )
                            }
                        }

                        Spacer(GlanceModifier.width(12.dp))

                        Box(
                            modifier = GlanceModifier.size(32.dp).clickable(skipNextAction()),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_apple_forward),
                                contentDescription = "Next",
                                colorFilter = ColorFilter.tint(textPrimary),
                            )
                        }

                        Spacer(GlanceModifier.defaultWeight())

                        Box(
                            modifier = GlanceModifier.size(28.dp).clickable(volumeUpAction()),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_apple_volume_max),
                                contentDescription = "Volume Up",
                                colorFilter = ColorFilter.tint(textSecondary),
                                modifier = GlanceModifier.size(13.5.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

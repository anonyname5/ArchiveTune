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

class AppleLiquidIslandWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            AppleLiquidIslandContent(context)
        }
    }
}

@Composable
private fun AppleLiquidIslandContent(context: Context) {
    val prefs = currentState<Preferences>()
    val state = prefs.toWidgetPlaybackState(context)

    val dominant = state.dominantColor?.let { Color(it) }
    val glassBaseDark = Color(0x650D0E15)
    val liquidAuraColor = remember(dominant) { dominant?.copy(alpha = 0.22f) ?: Color.Transparent }
    val liquidLensBg = remember(dominant) {
        dominant?.let {
            val r = (it.red * 0.45f + 1f * 0.55f).coerceIn(0f, 1f)
            val g = (it.green * 0.45f + 1f * 0.55f).coerceIn(0f, 1f)
            val b = (it.blue * 0.45f + 1f * 0.55f).coerceIn(0f, 1f)
            Color(red = r, green = g, blue = b, alpha = 0.40f)
        } ?: Color(0x35FFFFFF)
    }

    val textPrimary = ColorProvider(Color.White)
    val textSecondary = ColorProvider(Color(0xD0FFFFFF))
    val textTertiary = ColorProvider(Color(0x90FFFFFF))
    val scrubberTrack = ColorProvider(Color(0x28FFFFFF))
    val scrubberFill = ColorProvider(Color.White)

    val elapsedSec = if (state.durationMs > 0L) (state.positionMs / 1000L).toInt() else 0
    val totalSec = if (state.durationMs > 0L) (state.durationMs / 1000L).toInt() else 0
    val remainingSec = (totalSec - elapsedSec).coerceAtLeast(0)
    val elapsedStr = if (state.isAvailable && state.durationMs > 0L) "${elapsedSec / 60}:${(elapsedSec % 60).toString().padStart(2, '0')}" else "0:00"
    val remainingStr = if (state.isAvailable && totalSec > 0) "-${remainingSec / 60}:${(remainingSec % 60).toString().padStart(2, '0')}" else "-0:00"

    // Outer Specular Rim
    Box(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .background(Color(0x60FFFFFF))
                .cornerRadius(28.dp)
                .padding(1.2.dp)
                .clickable(openArchiveTuneAction(context)),
    ) {
        // Inner Depth Shadow
        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(Color(0x30000000))
                    .cornerRadius(27.dp)
                    .padding(0.8.dp),
        ) {
            // Smoked Acrylic Island Base
            Box(
                modifier =
                    GlanceModifier
                        .fillMaxSize()
                        .background(glassBaseDark)
                        .cornerRadius(26.dp),
            ) {
                // Dynamic Aura Layer
                if (state.dominantColor != null) {
                    Box(
                        modifier =
                            GlanceModifier
                                .fillMaxSize()
                                .background(liquidAuraColor)
                                .cornerRadius(26.dp),
                    ) {}
                }

                // Main Column (5 direct children)
                Column(
                    modifier =
                        GlanceModifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Child 1: Top Dynamic Island Status Header
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Mini Artwork Thumbnail
                        Box(
                            modifier =
                                GlanceModifier
                                    .size(28.dp)
                                    .background(Color(0x40FFFFFF))
                                    .cornerRadius(8.dp)
                                    .padding(0.8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            WidgetAlbumArt(
                                artPath = prefs[MusicWidgetKeys.ART_PATH],
                                modifier =
                                    GlanceModifier
                                        .fillMaxSize()
                                        .cornerRadius(7.dp),
                                contentDescription = state.title,
                                targetSize = 40.dp,
                            )
                        }

                        Spacer(GlanceModifier.width(8.dp))

                        // Waveform Equalizer Bar Capsule
                        Row(
                            modifier =
                                GlanceModifier
                                    .background(Color(0x28FFFFFF))
                                    .cornerRadius(10.dp)
                                    .padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(modifier = GlanceModifier.width(2.5.dp).height(8.dp).background(Color(0xCCFFFFFF)).cornerRadius(1.dp)) {}
                            Spacer(GlanceModifier.width(2.dp))
                            Box(modifier = GlanceModifier.width(2.5.dp).height(12.dp).background(Color(0xFFFFFFFF)).cornerRadius(1.dp)) {}
                            Spacer(GlanceModifier.width(2.dp))
                            Box(modifier = GlanceModifier.width(2.5.dp).height(6.dp).background(Color(0xAAFFFFFF)).cornerRadius(1.dp)) {}
                            Spacer(GlanceModifier.width(2.dp))
                            Box(modifier = GlanceModifier.width(2.5.dp).height(10.dp).background(Color(0xEEFFFFFF)).cornerRadius(1.dp)) {}
                        }

                        Spacer(GlanceModifier.defaultWeight())

                        // AirPlay Capsule
                        Row(
                            modifier =
                                GlanceModifier
                                    .background(Color(0x35FFFFFF))
                                    .cornerRadius(10.dp)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_apple_airplay),
                                contentDescription = "AirPlay",
                                colorFilter = ColorFilter.tint(ColorProvider(Color(0xFF38A3FF))),
                                modifier = GlanceModifier.size(9.dp),
                            )
                            Spacer(GlanceModifier.width(3.dp))
                            Text(
                                text = "AirPlay",
                                style =
                                    TextStyle(
                                        color = ColorProvider(Color.White),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                            )
                        }
                    }

                    Spacer(GlanceModifier.height(4.dp))

                    // Child 2: Track Metadata
                    Column(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = if (state.isAvailable) state.title else context.getString(R.string.app_name),
                            maxLines = 1,
                            style =
                                TextStyle(
                                    color = textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                        Spacer(GlanceModifier.height(1.dp))
                        Text(
                            text = if (state.isAvailable) state.artist else context.getString(R.string.no_track_playing),
                            maxLines = 1,
                            style =
                                TextStyle(
                                    color = textSecondary,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Normal,
                                ),
                        )
                    }

                    Spacer(GlanceModifier.height(5.dp))

                    // Child 3: Scrubber Deck
                    Column(modifier = GlanceModifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            progress = if (state.isAvailable) state.playbackPosition else 0f,
                            modifier =
                                GlanceModifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .cornerRadius(1.5.dp),
                            color = scrubberFill,
                            backgroundColor = scrubberTrack,
                        )

                        Spacer(GlanceModifier.height(2.5.dp))

                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
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
                    }

                    Spacer(GlanceModifier.height(4.dp))

                    // Child 4: 5-Button Glass Control Deck
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Volume Down
                        Box(
                            modifier = GlanceModifier.size(24.dp).clickable(volumeDownAction()),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_apple_volume_min),
                                contentDescription = "Volume Min",
                                colorFilter = ColorFilter.tint(textTertiary),
                                modifier = GlanceModifier.size(12.dp),
                            )
                        }

                        Spacer(GlanceModifier.width(18.dp))

                        // Previous
                        Box(
                            modifier = GlanceModifier.size(28.dp).clickable(skipPreviousAction()),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_apple_backward),
                                contentDescription = "Previous",
                                colorFilter = ColorFilter.tint(textPrimary),
                                modifier = GlanceModifier.size(17.dp),
                            )
                        }

                        Spacer(GlanceModifier.width(16.dp))

                        // Play/Pause Lens Bubble
                        Box(
                            modifier =
                                GlanceModifier
                                    .size(38.dp)
                                    .background(Color(0x60FFFFFF))
                                    .cornerRadius(19.dp)
                                    .padding(0.8.dp)
                                    .clickable(playPauseAction()),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier =
                                    GlanceModifier
                                        .fillMaxSize()
                                        .background(liquidLensBg)
                                        .cornerRadius(18.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    provider =
                                        ImageProvider(
                                            if (state.isPlaying) R.drawable.ic_apple_pause else R.drawable.ic_apple_play,
                                        ),
                                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                                    colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                    modifier = GlanceModifier.size(18.dp),
                                )
                            }
                        }

                        Spacer(GlanceModifier.width(16.dp))

                        // Next
                        Box(
                            modifier = GlanceModifier.size(28.dp).clickable(skipNextAction()),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_apple_forward),
                                contentDescription = "Next",
                                colorFilter = ColorFilter.tint(textPrimary),
                                modifier = GlanceModifier.size(17.dp),
                            )
                        }

                        Spacer(GlanceModifier.width(18.dp))

                        // Volume Up
                        Box(
                            modifier = GlanceModifier.size(24.dp).clickable(volumeUpAction()),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_apple_volume_max),
                                contentDescription = "Volume Max",
                                colorFilter = ColorFilter.tint(textTertiary),
                                modifier = GlanceModifier.size(13.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

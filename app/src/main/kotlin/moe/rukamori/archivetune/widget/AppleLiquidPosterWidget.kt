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

class AppleLiquidPosterWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            AppleLiquidPosterContent(context)
        }
    }
}

@Composable
private fun AppleLiquidPosterContent(context: Context) {
    val prefs = currentState<Preferences>()
    val state = prefs.toWidgetPlaybackState(context)

    val dominant = state.dominantColor?.let { Color(it) }
    val liquidDockBg = Color(0xB812131D)
    val liquidLensBg = remember(dominant) {
        dominant?.let {
            val r = (it.red * 0.45f + 1f * 0.55f).coerceIn(0f, 1f)
            val g = (it.green * 0.45f + 1f * 0.55f).coerceIn(0f, 1f)
            val b = (it.blue * 0.45f + 1f * 0.55f).coerceIn(0f, 1f)
            Color(red = r, green = g, blue = b, alpha = 0.45f)
        } ?: Color(0x40FFFFFF)
    }

    val textPrimary = ColorProvider(Color.White)
    val textSecondary = ColorProvider(Color(0xD5FFFFFF))
    val textTertiary = ColorProvider(Color(0x95FFFFFF))
    val scrubberTrack = ColorProvider(Color(0x30FFFFFF))
    val scrubberFill = ColorProvider(Color.White)

    // Outer Specular Crystalline Glass Rim
    Box(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .background(Color(0x65FFFFFF))
                .cornerRadius(28.dp)
                .padding(1.2.dp)
                .clickable(openArchiveTuneAction(context)),
    ) {
        // Inner Refraction Shadow
        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(Color(0x30000000))
                    .cornerRadius(27.dp)
                    .padding(0.8.dp),
        ) {
            // Full-Bleed Artwork Base
            Box(
                modifier =
                    GlanceModifier
                        .fillMaxSize()
                        .background(Color(0xFF10111A))
                        .cornerRadius(26.dp),
            ) {
                // Background Cover Artwork
                WidgetAlbumArt(
                    artPath = prefs[MusicWidgetKeys.ART_PATH],
                    modifier =
                        GlanceModifier
                            .fillMaxSize()
                            .cornerRadius(26.dp),
                    contentDescription = state.title,
                    targetSize = 250.dp,
                )

                // Bottom Smoked Vignette Shade
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Box(
                        modifier =
                            GlanceModifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(Color(0x60000000))
                                .cornerRadius(26.dp),
                    ) {}
                }

                // Floating visionOS Glass Dock at bottom
                Column(
                    modifier =
                        GlanceModifier
                            .fillMaxSize()
                            .padding(10.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    // Floating Frosted Crystalline Card Dock
                    Box(
                        modifier =
                            GlanceModifier
                                .fillMaxWidth()
                                .background(Color(0x60FFFFFF))
                                .cornerRadius(22.dp)
                                .padding(1.dp),
                    ) {
                        Box(
                            modifier =
                                GlanceModifier
                                    .fillMaxWidth()
                                    .background(liquidDockBg)
                                    .cornerRadius(21.dp)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                        ) {
                            Column(
                                modifier = GlanceModifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                // Metadata Row: Title + Lossless Tag
                                Row(
                                    modifier = GlanceModifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = GlanceModifier.defaultWeight()) {
                                        Text(
                                            text = if (state.isAvailable) state.title else context.getString(R.string.app_name),
                                            maxLines = 1,
                                            style =
                                                TextStyle(
                                                    color = textPrimary,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                ),
                                        )
                                        Text(
                                            text = if (state.isAvailable) state.artist else context.getString(R.string.no_track_playing),
                                            maxLines = 1,
                                            style =
                                                TextStyle(
                                                    color = textSecondary,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Normal,
                                                ),
                                        )
                                    }

                                    Spacer(GlanceModifier.width(6.dp))

                                    Box(
                                        modifier =
                                            GlanceModifier
                                                .background(Color(0x35FFFFFF))
                                                .cornerRadius(8.dp)
                                                .padding(horizontal = 5.dp, vertical = 2.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "LOSSLESS",
                                            style =
                                                TextStyle(
                                                    color = textTertiary,
                                                    fontSize = 7.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                ),
                                        )
                                    }
                                }

                                Spacer(GlanceModifier.height(5.dp))

                                // Micro Scrubber Line
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

                                Spacer(GlanceModifier.height(6.dp))

                                // Playback Controls Row
                                Row(
                                    modifier = GlanceModifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = GlanceModifier.size(28.dp).clickable(skipPreviousAction()),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Image(
                                            provider = ImageProvider(R.drawable.ic_apple_backward),
                                            contentDescription = "Previous",
                                            colorFilter = ColorFilter.tint(textPrimary),
                                            modifier = GlanceModifier.size(16.dp),
                                        )
                                    }

                                    Spacer(GlanceModifier.width(20.dp))

                                    Box(
                                        modifier =
                                            GlanceModifier
                                                .size(34.dp)
                                                .background(Color(0x60FFFFFF))
                                                .cornerRadius(17.dp)
                                                .padding(0.8.dp)
                                                .clickable(playPauseAction()),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Box(
                                            modifier =
                                                GlanceModifier
                                                    .fillMaxSize()
                                                    .background(liquidLensBg)
                                                    .cornerRadius(16.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Image(
                                                provider =
                                                    ImageProvider(
                                                        if (state.isPlaying) R.drawable.ic_apple_pause else R.drawable.ic_apple_play,
                                                    ),
                                                contentDescription = if (state.isPlaying) "Pause" else "Play",
                                                colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                                modifier = GlanceModifier.size(17.dp),
                                            )
                                        }
                                    }

                                    Spacer(GlanceModifier.width(20.dp))

                                    Box(
                                        modifier = GlanceModifier.size(28.dp).clickable(skipNextAction()),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Image(
                                            provider = ImageProvider(R.drawable.ic_apple_forward),
                                            contentDescription = "Next",
                                            colorFilter = ColorFilter.tint(textPrimary),
                                            modifier = GlanceModifier.size(16.dp),
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

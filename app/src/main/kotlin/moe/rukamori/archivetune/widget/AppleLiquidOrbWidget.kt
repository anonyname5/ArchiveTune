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

class AppleLiquidOrbWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            AppleLiquidOrbContent(context)
        }
    }
}

@Composable
private fun AppleLiquidOrbContent(context: Context) {
    val prefs = currentState<Preferences>()
    val state = prefs.toWidgetPlaybackState(context)

    val dominant = state.dominantColor?.let { Color(it) }
    val glassBaseDark = Color(0x6012131D)
    val liquidAuraColor = remember(dominant) { dominant?.copy(alpha = 0.28f) ?: Color.Transparent }
    val liquidLensBg = remember(dominant) {
        dominant?.let {
            val r = (it.red * 0.45f + 1f * 0.55f).coerceIn(0f, 1f)
            val g = (it.green * 0.45f + 1f * 0.55f).coerceIn(0f, 1f)
            val b = (it.blue * 0.45f + 1f * 0.55f).coerceIn(0f, 1f)
            Color(red = r, green = g, blue = b, alpha = 0.42f)
        } ?: Color(0x35FFFFFF)
    }

    val textPrimary = ColorProvider(Color.White)
    val textSecondary = ColorProvider(Color(0xD0FFFFFF))
    val scrubberTrack = ColorProvider(Color(0x28FFFFFF))
    val scrubberFill = ColorProvider(Color.White)

    // Outer Circular Specular Glass Ring (pebble/orb curvature 36dp)
    Box(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .background(Color(0x65FFFFFF))
                .cornerRadius(36.dp)
                .padding(1.2.dp)
                .clickable(openArchiveTuneAction(context)),
    ) {
        // Inner Glass Depth
        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(Color(0x30000000))
                    .cornerRadius(35.dp)
                    .padding(0.8.dp),
        ) {
            // Smoked Crystal Orb Base
            Box(
                modifier =
                    GlanceModifier
                        .fillMaxSize()
                        .background(glassBaseDark)
                        .cornerRadius(34.dp),
            ) {
                // Dynamic Artwork Dye Aura
                if (state.dominantColor != null) {
                    Box(
                        modifier =
                            GlanceModifier
                                .fillMaxSize()
                                .background(liquidAuraColor)
                                .cornerRadius(34.dp),
                    ) {}
                }

                // Orb Content Column (5 direct children)
                Column(
                    modifier =
                        GlanceModifier
                            .fillMaxSize()
                            .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Circular Artwork with Specular Border
                    Box(
                        modifier =
                            GlanceModifier
                                .size(58.dp)
                                .background(Color(0x50FFFFFF))
                                .cornerRadius(29.dp)
                                .padding(1.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        WidgetAlbumArt(
                            artPath = prefs[MusicWidgetKeys.ART_PATH],
                            modifier =
                                GlanceModifier
                                    .fillMaxSize()
                                    .cornerRadius(28.dp),
                            contentDescription = state.title,
                            targetSize = 80.dp,
                        )
                    }

                    Spacer(GlanceModifier.height(4.dp))

                    // Title & Artist
                    Text(
                        text = if (state.isAvailable) state.title else context.getString(R.string.app_name),
                        maxLines = 1,
                        style =
                            TextStyle(
                                color = textPrimary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                    )
                    Text(
                        text = if (state.isAvailable) state.artist else context.getString(R.string.no_track_playing),
                        maxLines = 1,
                        style =
                            TextStyle(
                                color = textSecondary,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Normal,
                            ),
                    )

                    Spacer(GlanceModifier.height(5.dp))

                    // Controls Dock with Play/Pause Lens
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = GlanceModifier.size(26.dp).clickable(skipPreviousAction()),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_apple_backward),
                                contentDescription = "Previous",
                                colorFilter = ColorFilter.tint(textSecondary),
                                modifier = GlanceModifier.size(15.dp),
                            )
                        }

                        Spacer(GlanceModifier.width(10.dp))

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
                                    modifier = GlanceModifier.size(16.dp),
                                )
                            }
                        }

                        Spacer(GlanceModifier.width(10.dp))

                        Box(
                            modifier = GlanceModifier.size(26.dp).clickable(skipNextAction()),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_apple_forward),
                                contentDescription = "Next",
                                colorFilter = ColorFilter.tint(textSecondary),
                                modifier = GlanceModifier.size(15.dp),
                            )
                        }
                    }

                    Spacer(GlanceModifier.height(4.dp))

                    // Micro Scrubber Indicator
                    LinearProgressIndicator(
                        progress = if (state.isAvailable) state.playbackPosition else 0f,
                        modifier =
                            GlanceModifier
                                .fillMaxWidth()
                                .height(2.5.dp)
                                .cornerRadius(1.5.dp),
                        color = scrubberFill,
                        backgroundColor = scrubberTrack,
                    )
                }
            }
        }
    }
}

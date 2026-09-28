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

class AppleLiquidCompactWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            AppleLiquidCompactContent(context)
        }
    }
}

@Composable
private fun AppleLiquidCompactContent(context: Context) {
    val prefs = currentState<Preferences>()
    val state = prefs.toWidgetPlaybackState(context)

    val dominant = state.dominantColor?.let { Color(it) }
    val glassBaseDark = Color(0x5813141D)
    val liquidAuraColor = remember(dominant) { dominant?.copy(alpha = 0.22f) ?: Color.Transparent }
    val liquidLensBg = remember(dominant) {
        dominant?.let {
            val r = (it.red * 0.45f + 1f * 0.55f).coerceIn(0f, 1f)
            val g = (it.green * 0.45f + 1f * 0.55f).coerceIn(0f, 1f)
            val b = (it.blue * 0.45f + 1f * 0.55f).coerceIn(0f, 1f)
            Color(red = r, green = g, blue = b, alpha = 0.38f)
        } ?: Color(0x35FFFFFF)
    }

    val textPrimary = ColorProvider(Color.White)
    val textSecondary = ColorProvider(Color(0xD0FFFFFF))
    val textTertiary = ColorProvider(Color(0x95FFFFFF))
    val scrubberTrack = ColorProvider(Color(0x28FFFFFF))
    val scrubberFill = ColorProvider(Color.White)

    // Layer 1: Outer Crystalline Specular Glass Rim
    Box(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .background(Color(0x65FFFFFF))
                .cornerRadius(26.dp)
                .padding(1.2.dp)
                .clickable(openArchiveTuneAction(context)),
    ) {
        // Layer 2: Inner Refraction Depth Shadow
        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(Color(0x30000000))
                    .cornerRadius(25.dp)
                    .padding(0.8.dp),
        ) {
            // Layer 3: Smoked Crystal Acrylic Base
            Box(
                modifier =
                    GlanceModifier
                        .fillMaxSize()
                        .background(glassBaseDark)
                        .cornerRadius(24.dp),
            ) {
                // Layer 4: Dynamic Liquid Artwork Dye Layer
                if (state.dominantColor != null) {
                    Box(
                        modifier =
                            GlanceModifier
                                .fillMaxSize()
                                .background(liquidAuraColor)
                                .cornerRadius(24.dp),
                    ) {}
                }

                // Layer 5: Content
                Column(
                    modifier =
                        GlanceModifier
                            .fillMaxSize()
                            .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Album Artwork Card
                    Box(
                        modifier =
                            GlanceModifier
                                .size(64.dp)
                                .background(Color(0x40FFFFFF))
                                .cornerRadius(14.dp)
                                .padding(1.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        WidgetAlbumArt(
                            artPath = prefs[MusicWidgetKeys.ART_PATH],
                            modifier =
                                GlanceModifier
                                    .fillMaxSize()
                                    .cornerRadius(13.dp),
                            contentDescription = state.title,
                        )
                    }

                    Spacer(GlanceModifier.height(5.dp))

                    // Title & Artist
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

                    Spacer(GlanceModifier.height(4.dp))

                    // Mini Progress Scrubber
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
                        // Previous
                        Box(
                            modifier =
                                GlanceModifier
                                    .size(28.dp)
                                    .clickable(skipPreviousAction()),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_apple_backward),
                                contentDescription = "Previous",
                                colorFilter = ColorFilter.tint(textSecondary),
                                modifier = GlanceModifier.size(16.dp),
                            )
                        }

                        Spacer(GlanceModifier.width(12.dp))

                        // Play/Pause Lens Bubble
                        Box(
                            modifier =
                                GlanceModifier
                                    .size(36.dp)
                                    .background(Color(0x60FFFFFF))
                                    .cornerRadius(18.dp)
                                    .padding(0.8.dp)
                                    .clickable(playPauseAction()),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier =
                                    GlanceModifier
                                        .fillMaxSize()
                                        .background(liquidLensBg)
                                        .cornerRadius(17.dp),
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

                        Spacer(GlanceModifier.width(12.dp))

                        // Next
                        Box(
                            modifier =
                                GlanceModifier
                                    .size(28.dp)
                                    .clickable(skipNextAction()),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_apple_forward),
                                contentDescription = "Next",
                                colorFilter = ColorFilter.tint(textSecondary),
                                modifier = GlanceModifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

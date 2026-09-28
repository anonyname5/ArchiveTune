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

class AppleLiquidPillWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            AppleLiquidPillContent(context)
        }
    }
}

@Composable
private fun AppleLiquidPillContent(context: Context) {
    val prefs = currentState<Preferences>()
    val state = prefs.toWidgetPlaybackState(context)

    val dominant = state.dominantColor?.let { Color(it) }
    val glassBaseDark = Color(0x6010111A)
    val liquidAuraColor = remember(dominant) { dominant?.copy(alpha = 0.25f) ?: Color.Transparent }
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

    val elapsedSec = if (state.durationMs > 0L) (state.positionMs / 1000L).toInt() else 0
    val elapsedStr = if (state.isAvailable && state.durationMs > 0L) "${elapsedSec / 60}:${(elapsedSec % 60).toString().padStart(2, '0')}" else "0:00"

    // Outer Crystalline Pill Rim
    Box(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .background(Color(0x60FFFFFF))
                .cornerRadius(28.dp)
                .padding(1.2.dp)
                .clickable(openArchiveTuneAction(context)),
    ) {
        // Inner Glass Refraction Shadow
        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(Color(0x30000000))
                    .cornerRadius(27.dp)
                    .padding(0.8.dp),
        ) {
            // Smoked Acrylic Pill Base
            Box(
                modifier =
                    GlanceModifier
                        .fillMaxSize()
                        .background(glassBaseDark)
                        .cornerRadius(26.dp),
            ) {
                // Dynamic Aura
                if (state.dominantColor != null) {
                    Box(
                        modifier =
                            GlanceModifier
                                .fillMaxSize()
                                .background(liquidAuraColor)
                                .cornerRadius(26.dp),
                    ) {}
                }

                // Horizontal Capsule Row
                Row(
                    modifier =
                        GlanceModifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Mini Artwork Squircle
                    Box(
                        modifier =
                            GlanceModifier
                                .size(38.dp)
                                .background(Color(0x40FFFFFF))
                                .cornerRadius(12.dp)
                                .padding(1.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        WidgetAlbumArt(
                            artPath = prefs[MusicWidgetKeys.ART_PATH],
                            modifier =
                                GlanceModifier
                                    .fillMaxSize()
                                    .cornerRadius(11.dp),
                            contentDescription = state.title,
                        )
                    }

                    Spacer(GlanceModifier.width(9.dp))

                    // Title + Subtitle
                    Column(
                        modifier =
                            GlanceModifier
                                .defaultWeight(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
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
                        Spacer(GlanceModifier.height(1.dp))
                        Text(
                            text = if (state.isAvailable) "${state.artist} • $elapsedStr" else context.getString(R.string.no_track_playing),
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

                    // Controls: Play/Pause Lens + Next
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Play/Pause Lens Bubble
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

                        Spacer(GlanceModifier.width(6.dp))

                        // Next
                        Box(
                            modifier =
                                GlanceModifier
                                    .size(30.dp)
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

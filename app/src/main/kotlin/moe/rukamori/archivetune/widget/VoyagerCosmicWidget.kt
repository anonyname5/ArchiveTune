/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.widget

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
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
import androidx.glance.layout.fillMaxHeight
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

class VoyagerCosmicWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            VoyagerCosmicContent(context)
        }
    }
}

@Composable
private fun VoyagerCosmicContent(context: Context) {
    val prefs = currentState<Preferences>()
    val state = prefs.toWidgetPlaybackState(context)

    GlanceTheme(
        colors =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                GlanceTheme.colors
            } else {
                ArchiveTuneWidgetColors.providers
            },
    ) {
        val palette = rememberWidgetPalette(state.dominantColor)
        val size = LocalSize.current
        val compact = size.width < 290.dp || size.height < 110.dp

        // Deep Cosmos & Voyager Gold Palette
        val spaceVoidBg = ColorProvider(Color(0xFF090A14))
        val goldenRecord = ColorProvider(Color(0xFFE5B800))
        val goldGroove = ColorProvider(Color(0xFFB38F00))
        val cosmicCyan = ColorProvider(Color(0xFF5CE1E6))
        val textPrimary = ColorProvider(Color(0xFFF0F2F8))
        val textSecondary = ColorProvider(Color(0xFF7E849E))
        val controlBg = ColorProvider(Color(0xFF161826))

        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(spaceVoidBg)
                    .cornerRadius(22.dp)
                    .padding(if (compact) 8.dp else 12.dp)
                    .clickable(openArchiveTuneAction(context)),
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // NASA Voyager Golden Record
                val recordSize = if (compact) 84.dp else 100.dp
                Box(
                    modifier =
                        GlanceModifier
                            .size(recordSize)
                            .background(goldenRecord)
                            .cornerRadius(999.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    // Outer Groove Ring
                    Box(
                        modifier =
                            GlanceModifier
                                .size(recordSize - 12.dp)
                                .background(goldGroove)
                                .cornerRadius(999.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        // Inner Record Core
                        Box(
                            modifier =
                                GlanceModifier
                                    .size(recordSize - 26.dp)
                                    .background(goldenRecord)
                                    .cornerRadius(999.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            // Circular Artwork Center
                            val artSize = recordSize * 0.44f
                            WidgetArtwork(
                                artPath = state.artPath,
                                context = context,
                                contentDescription = context.getString(R.string.album_cover_desc),
                                targetSize = artSize,
                                cornerRadius = 999.dp,
                                palette = palette,
                                modifier = GlanceModifier.size(artSize).cornerRadius(999.dp),
                            )

                            // Center Spindle Pin
                            Box(
                                modifier =
                                    GlanceModifier
                                        .size(8.dp)
                                        .background(spaceVoidBg)
                                        .cornerRadius(999.dp),
                            ) {}
                        }
                    }
                }

                Spacer(GlanceModifier.width(if (compact) 10.dp else 14.dp))

                // Right: NASA Mission Telemetry, Title, Orbit Progress, and Spacecraft Controls
                Column(
                    modifier =
                        GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Deep Space Telemetry Header
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "VOYAGER 1 // GOLDEN RECORD",
                            style =
                                TextStyle(
                                    color = goldenRecord,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                        Spacer(GlanceModifier.defaultWeight())
                        Text(
                            text = if (state.isPlaying) "TX ACTIVE" else "STANDBY",
                            style =
                                TextStyle(
                                    color = cosmicCyan,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                    }

                    Spacer(GlanceModifier.height(3.dp))

                    Text(
                        text = state.title,
                        style =
                            TextStyle(
                                color = textPrimary,
                                fontSize = if (compact) 12.sp else 14.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        maxLines = 1,
                    )

                    Text(
                        text = state.artist,
                        style =
                            TextStyle(
                                color = textSecondary,
                                fontSize = if (compact) 10.sp else 11.sp,
                            ),
                        maxLines = 1,
                    )

                    Spacer(GlanceModifier.height(4.dp))

                    // Interstellar Orbital Progress Bar (Gold to Cosmic Blue)
                    if (state.isAvailable && state.playbackPosition > 0f) {
                        LinearProgressIndicator(
                            progress = state.playbackPosition,
                            color = goldenRecord,
                            backgroundColor = ColorProvider(Color(0xFF202336)),
                            modifier =
                                GlanceModifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .cornerRadius(2.dp),
                        )
                        Spacer(GlanceModifier.height(6.dp))
                    }

                    // Deep Space Mission Controls
                    if (state.isAvailable) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 30.dp else 34.dp),
                                action = skipPreviousAction(),
                                icon = R.drawable.skip_previous,
                                contentDescription = context.getString(R.string.widget_previous),
                                backgroundColor = controlBg,
                                contentColor = textPrimary,
                                cornerRadius = 999.dp,
                                iconSize = 16.dp,
                            )
                            Spacer(GlanceModifier.width(8.dp))
                            // Gold Sun Play/Pause Button
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 34.dp else 38.dp),
                                action = playPauseAction(),
                                icon = if (state.isPlaying) R.drawable.pause else R.drawable.play,
                                contentDescription =
                                    context.getString(
                                        if (state.isPlaying) R.string.widget_pause else R.string.play,
                                    ),
                                backgroundColor = goldenRecord,
                                contentColor = ColorProvider(Color(0xFF090A14)),
                                cornerRadius = 999.dp,
                                iconSize = 20.dp,
                            )
                            Spacer(GlanceModifier.width(8.dp))
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 30.dp else 34.dp),
                                action = skipNextAction(),
                                icon = R.drawable.skip_next,
                                contentDescription = context.getString(R.string.next),
                                backgroundColor = controlBg,
                                contentColor = textPrimary,
                                cornerRadius = 999.dp,
                                iconSize = 16.dp,
                            )
                        }
                    }
                }
            }
        }
    }
}

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

class AnalogVuMeterWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            AnalogVuMeterContent(context)
        }
    }
}

@Composable
private fun AnalogVuMeterContent(context: Context) {
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

        // Studio receiver styling
        val chassisBg = ColorProvider(Color(0xFF18191D))
        val vuBacklight = ColorProvider(Color(0xFFFFEDB3))
        val vuScaleText = ColorProvider(Color(0xFF2C2416))
        val vuNeedleColor = ColorProvider(Color(0xFFBF261B))
        val peakZoneColor = ColorProvider(Color(0xFFD32F2F))
        val textPrimary = ColorProvider(Color(0xFFF0F0F5))
        val textSecondary = ColorProvider(Color(0xFF8E909B))
        val amberAccent = ColorProvider(Color(0xFFFFB300))

        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(chassisBg)
                    .cornerRadius(20.dp)
                    .padding(if (compact) 4.dp else 6.dp)
                    .clickable(openArchiveTuneAction(context)),
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Album Art Bezel
                val artSize = if (compact) 60.dp else 74.dp
                WidgetArtwork(
                    artPath = state.artPath,
                    context = context,
                    contentDescription = context.getString(R.string.album_cover_desc),
                    targetSize = artSize,
                    cornerRadius = 8.dp,
                    palette = palette,
                    modifier = GlanceModifier.size(artSize),
                )

                Spacer(GlanceModifier.width(if (compact) 8.dp else 12.dp))

                // Center: Dual Glowing VU Meters & Details
                Column(
                    modifier =
                        GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    // Title and Artist
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
                                fontSize = 10.sp,
                            ),
                        maxLines = 1,
                    )

                    Spacer(GlanceModifier.height(4.dp))

                    // Dual Analog VU Meter Windows (Left & Right Channels)
                    val vuProgressL = if (state.isPlaying) 0.68f else 0.05f
                    val vuProgressR = if (state.isPlaying) 0.74f else 0.05f

                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Channel Left Meter
                        Box(
                            modifier =
                                GlanceModifier
                                    .defaultWeight()
                                    .background(vuBacklight)
                                    .cornerRadius(6.dp)
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                        ) {
                            Column {
                                Row(
                                    modifier = GlanceModifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "VU L",
                                        style =
                                            TextStyle(
                                                color = vuScaleText,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                    )
                                    Spacer(GlanceModifier.defaultWeight())
                                    Text(
                                        text = "0 dB",
                                        style =
                                            TextStyle(
                                                color = peakZoneColor,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                    )
                                }
                                Spacer(GlanceModifier.height(1.dp))
                                LinearProgressIndicator(
                                    progress = vuProgressL,
                                    color = vuNeedleColor,
                                    backgroundColor = ColorProvider(Color(0x332C2416)),
                                    modifier = GlanceModifier.fillMaxWidth().height(3.dp).cornerRadius(1.dp),
                                )
                            }
                        }

                        Spacer(GlanceModifier.width(6.dp))

                        // Channel Right Meter
                        Box(
                            modifier =
                                GlanceModifier
                                    .defaultWeight()
                                    .background(vuBacklight)
                                    .cornerRadius(6.dp)
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                        ) {
                            Column {
                                Row(
                                    modifier = GlanceModifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "VU R",
                                        style =
                                            TextStyle(
                                                color = vuScaleText,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                    )
                                    Spacer(GlanceModifier.defaultWeight())
                                    Text(
                                        text = "0 dB",
                                        style =
                                            TextStyle(
                                                color = peakZoneColor,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                    )
                                }
                                Spacer(GlanceModifier.height(1.dp))
                                LinearProgressIndicator(
                                    progress = vuProgressR,
                                    color = vuNeedleColor,
                                    backgroundColor = ColorProvider(Color(0x332C2416)),
                                    modifier = GlanceModifier.fillMaxWidth().height(3.dp).cornerRadius(1.dp),
                                )
                            }
                        }
                    }

                    Spacer(GlanceModifier.height(4.dp))

                    // Track Position Progress Bar
                    if (state.isAvailable && state.playbackPosition > 0f) {
                        LinearProgressIndicator(
                            progress = state.playbackPosition,
                            color = amberAccent,
                            backgroundColor = ColorProvider(Color(0xFF2E2E38)),
                            modifier =
                                GlanceModifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .cornerRadius(2.dp),
                        )
                    }
                }

                if (state.isAvailable) {
                    Spacer(GlanceModifier.width(8.dp))

                    // Hi-Fi Receiver Controls
                    Column(
                        verticalAlignment = Alignment.Vertical.CenterVertically,
                    ) {
                        WidgetControlButton(
                            modifier = GlanceModifier.size(if (compact) 32.dp else 38.dp),
                            action = playPauseAction(),
                            icon = if (state.isPlaying) R.drawable.pause else R.drawable.play,
                            contentDescription =
                                context.getString(
                                    if (state.isPlaying) R.string.widget_pause else R.string.play,
                                ),
                            backgroundColor = amberAccent,
                            contentColor = ColorProvider(Color(0xFF141418)),
                            cornerRadius = 8.dp,
                            iconSize = 20.dp,
                        )

                        Spacer(GlanceModifier.height(6.dp))

                        Row {
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 24.dp else 28.dp),
                                action = skipPreviousAction(),
                                icon = R.drawable.skip_previous,
                                contentDescription = context.getString(R.string.widget_previous),
                                backgroundColor = ColorProvider(Color(0xFF2A2B33)),
                                contentColor = textPrimary,
                                cornerRadius = 6.dp,
                                iconSize = 14.dp,
                            )
                            Spacer(GlanceModifier.width(4.dp))
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 24.dp else 28.dp),
                                action = skipNextAction(),
                                icon = R.drawable.skip_next,
                                contentDescription = context.getString(R.string.next),
                                backgroundColor = ColorProvider(Color(0xFF2A2B33)),
                                contentColor = textPrimary,
                                cornerRadius = 6.dp,
                                iconSize = 14.dp,
                            )
                        }
                    }
                }
            }
        }
    }
}

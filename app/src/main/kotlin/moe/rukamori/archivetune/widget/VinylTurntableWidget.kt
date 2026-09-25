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
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
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

class VinylTurntableWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            VinylTurntableContent(context)
        }
    }
}

@Composable
private fun VinylTurntableContent(context: Context) {
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

        // Turntable chassis: warm charcoal wood/matte finish
        val chassisBg = ColorProvider(Color(0xFF1E1D20))
        val goldAccent = ColorProvider(Color(0xFFD4AF37))
        val vinylBlack = ColorProvider(Color(0xFF121214))
        val vinylGroove = ColorProvider(Color(0xFF28282D))

        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(chassisBg)
                    .cornerRadius(24.dp)
                    .padding(if (compact) 8.dp else 12.dp)
                    .clickable(openArchiveTuneAction(context)),
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Turntable Platter & Vinyl Record
                val platterSize = if (compact) 84.dp else 102.dp
                Box(
                    modifier =
                        GlanceModifier
                            .size(platterSize)
                            .background(vinylBlack)
                            .cornerRadius(999.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    // Outer Groove Ring
                    Box(
                        modifier =
                            GlanceModifier
                                .size(platterSize - 12.dp)
                                .background(vinylGroove)
                                .cornerRadius(999.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        // Inner Groove
                        Box(
                            modifier =
                                GlanceModifier
                                    .size(platterSize - 26.dp)
                                    .background(vinylBlack)
                                    .cornerRadius(999.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            // Center Album Art Label
                            val labelSize = platterSize * 0.44f
                            WidgetArtwork(
                                artPath = state.artPath,
                                context = context,
                                contentDescription = context.getString(R.string.album_cover_desc),
                                targetSize = labelSize,
                                cornerRadius = 999.dp,
                                palette = palette,
                                modifier = GlanceModifier.size(labelSize).cornerRadius(999.dp),
                            )

                            // Spindle Hole
                            Box(
                                modifier =
                                    GlanceModifier
                                        .size(10.dp)
                                        .background(ColorProvider(Color(0xFFE2E2E6)))
                                        .cornerRadius(999.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier =
                                        GlanceModifier
                                            .size(4.dp)
                                            .background(vinylBlack)
                                            .cornerRadius(999.dp),
                                ) {}
                            }
                        }
                    }
                }

                Spacer(GlanceModifier.width(if (compact) 10.dp else 14.dp))

                // Right Section: Tonearm status, Track Metadata, and Deck Controls
                Column(
                    modifier =
                        GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Deck Header: Vintage 33⅓ RPM Indicator & Stylus status
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "33⅓ RPM",
                            style =
                                TextStyle(
                                    color = goldAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                        Spacer(GlanceModifier.width(6.dp))
                        Text(
                            text = if (state.isPlaying) "• STYLUS ENGAGED" else "• CUE REST",
                            style =
                                TextStyle(
                                    color = ColorProvider(Color(0xFF9E9EA4)),
                                    fontSize = 9.sp,
                                ),
                        )
                    }

                    Spacer(GlanceModifier.height(3.dp))

                    Text(
                        text = state.title,
                        style =
                            TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = if (compact) 13.sp else 15.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        maxLines = 1,
                    )

                    Text(
                        text = state.artist,
                        style =
                            TextStyle(
                                color = ColorProvider(Color(0xFFB0B0B8)),
                                fontSize = if (compact) 11.sp else 12.sp,
                            ),
                        maxLines = 1,
                    )

                    Spacer(GlanceModifier.height(4.dp))

                    // Tonearm Groove Tracker Progress
                    if (state.isAvailable && state.playbackPosition > 0f) {
                        LinearProgressIndicator(
                            progress = state.playbackPosition,
                            color = goldAccent,
                            backgroundColor = ColorProvider(Color(0xFF333238)),
                            modifier =
                                GlanceModifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .cornerRadius(2.dp),
                        )
                        Spacer(GlanceModifier.height(6.dp))
                    }

                    // Tactile Brushed Metal Buttons
                    if (state.isAvailable) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 32.dp else 36.dp),
                                action = skipPreviousAction(),
                                icon = R.drawable.skip_previous,
                                contentDescription = context.getString(R.string.widget_previous),
                                backgroundColor = ColorProvider(Color(0xFF2C2B30)),
                                contentColor = ColorProvider(Color.White),
                                cornerRadius = 8.dp,
                                iconSize = 18.dp,
                            )
                            Spacer(GlanceModifier.width(8.dp))
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 36.dp else 42.dp),
                                action = playPauseAction(),
                                icon = if (state.isPlaying) R.drawable.pause else R.drawable.play,
                                contentDescription =
                                    context.getString(
                                        if (state.isPlaying) R.string.widget_pause else R.string.play,
                                    ),
                                backgroundColor = goldAccent,
                                contentColor = ColorProvider(Color(0xFF1A1A1E)),
                                cornerRadius = 10.dp,
                                iconSize = 22.dp,
                            )
                            Spacer(GlanceModifier.width(8.dp))
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 32.dp else 36.dp),
                                action = skipNextAction(),
                                icon = R.drawable.skip_next,
                                contentDescription = context.getString(R.string.next),
                                backgroundColor = ColorProvider(Color(0xFF2C2B30)),
                                contentColor = ColorProvider(Color.White),
                                cornerRadius = 8.dp,
                                iconSize = 18.dp,
                            )
                        }
                    }
                }
            }
        }
    }
}

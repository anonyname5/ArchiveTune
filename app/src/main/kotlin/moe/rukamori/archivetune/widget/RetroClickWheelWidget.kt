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

class RetroClickWheelWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            RetroClickWheelContent(context)
        }
    }
}

@Composable
private fun RetroClickWheelContent(context: Context) {
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

        // Metallic iPod classic color palette
        val bodyBg = ColorProvider(Color(0xFF232428))
        val lcdScreenBg = ColorProvider(Color(0xFF141922))
        val wheelBg = ColorProvider(Color(0xFF2E3036))
        val centerBtnBg = ColorProvider(Color(0xFF3E4048))
        val accentBlue = ColorProvider(Color(0xFF4A90E2))
        val textPrimary = ColorProvider(Color(0xFFF0F0F5))
        val textSecondary = ColorProvider(Color(0xFF8E95A5))

        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(bodyBg)
                    .cornerRadius(24.dp)
                    .padding(if (compact) 4.dp else 6.dp)
                    .clickable(openArchiveTuneAction(context)),
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left: Classic LCD Screen
                Box(
                    modifier =
                        GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight()
                            .background(lcdScreenBg)
                            .cornerRadius(14.dp)
                            .padding(8.dp),
                ) {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.Vertical.CenterVertically,
                    ) {
                        // LCD Top Status Bar
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = if (state.isPlaying) "▶ NOW PLAYING" else "❚❚ PAUSED",
                                style =
                                    TextStyle(
                                        color = accentBlue,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                            )
                        }

                        Spacer(GlanceModifier.height(4.dp))

                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val artSize = if (compact) 36.dp else 46.dp
                            WidgetArtwork(
                                artPath = state.artPath,
                                context = context,
                                contentDescription = context.getString(R.string.album_cover_desc),
                                targetSize = artSize,
                                cornerRadius = 6.dp,
                                palette = palette,
                                modifier = GlanceModifier.size(artSize),
                            )

                            Spacer(GlanceModifier.width(8.dp))

                            Column(modifier = GlanceModifier.defaultWeight()) {
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
                            }
                        }

                        Spacer(GlanceModifier.height(4.dp))

                        // Blue LCD Scrub Progress Bar
                        LinearProgressIndicator(
                            progress = if (state.isAvailable) state.playbackPosition else 0f,
                            color = accentBlue,
                            backgroundColor = ColorProvider(Color(0xFF222B38)),
                            modifier =
                                GlanceModifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .cornerRadius(2.dp),
                        )
                    }
                }

                Spacer(GlanceModifier.width(if (compact) 10.dp else 14.dp))

                // Right: Iconic Circular Click Wheel
                val wheelSize = if (compact) 82.dp else 100.dp
                Box(
                    modifier =
                        GlanceModifier
                            .size(wheelSize)
                            .background(wheelBg)
                            .cornerRadius(999.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    // Top: MENU label
                    Box(
                        modifier =
                            GlanceModifier
                                .fillMaxSize()
                                .padding(top = 4.dp),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Text(
                            text = "MENU",
                            style =
                                TextStyle(
                                    color = textSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                    }

                    // Left & Right: Skip buttons
                    Row(
                        modifier =
                            GlanceModifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        WidgetControlButton(
                            modifier = GlanceModifier.size(24.dp),
                            action = skipPreviousAction(),
                            icon = R.drawable.skip_previous,
                            contentDescription = context.getString(R.string.widget_previous),
                            backgroundColor = ColorProvider(Color.Transparent),
                            contentColor = textSecondary,
                            cornerRadius = 12.dp,
                            iconSize = 16.dp,
                        )

                        Spacer(GlanceModifier.defaultWeight())

                        WidgetControlButton(
                            modifier = GlanceModifier.size(24.dp),
                            action = skipNextAction(),
                            icon = R.drawable.skip_next,
                            contentDescription = context.getString(R.string.next),
                            backgroundColor = ColorProvider(Color.Transparent),
                            contentColor = textSecondary,
                            cornerRadius = 12.dp,
                            iconSize = 16.dp,
                        )
                    }

                    // Bottom: Play/Pause action indicator
                    Box(
                        modifier =
                            GlanceModifier
                                .fillMaxSize()
                                .padding(bottom = 4.dp),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        WidgetControlButton(
                            modifier = GlanceModifier.size(22.dp),
                            action = playPauseAction(),
                            icon = if (state.isPlaying) R.drawable.pause else R.drawable.play,
                            contentDescription =
                                context.getString(
                                    if (state.isPlaying) R.string.widget_pause else R.string.play,
                                ),
                            backgroundColor = ColorProvider(Color.Transparent),
                            contentColor = textSecondary,
                            cornerRadius = 11.dp,
                            iconSize = 14.dp,
                        )
                    }

                    // Center Action Button: Circular push button that toggles play/pause
                    val centerSize = wheelSize * 0.42f
                    WidgetControlButton(
                        modifier = GlanceModifier.size(centerSize),
                        action = playPauseAction(),
                        icon = if (state.isPlaying) R.drawable.pause else R.drawable.play,
                        contentDescription =
                            context.getString(
                                if (state.isPlaying) R.string.widget_pause else R.string.play,
                            ),
                        backgroundColor = centerBtnBg,
                        contentColor = textPrimary,
                        cornerRadius = 999.dp,
                        iconSize = if (compact) 16.dp else 20.dp,
                    )
                }
            }
        }
    }
}

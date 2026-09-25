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

class GameBoyConsoleWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            GameBoyConsoleContent(context)
        }
    }
}

@Composable
private fun GameBoyConsoleContent(context: Context) {
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

        // Authentic Game Boy 1989 palette
        val bodyGray = ColorProvider(Color(0xFFD6D6DA))
        val bezelGray = ColorProvider(Color(0xFF6B6C74))
        val lcdOliveBg = ColorProvider(Color(0xFF909A29))
        val lcdPixelDark = ColorProvider(Color(0xFF283618))
        val lcdPixelMid = ColorProvider(Color(0xFF4F5D2F))
        val batteryRed = ColorProvider(if (state.isPlaying) Color(0xFFFF1744) else Color(0xFF661020))
        val dpadBlack = ColorProvider(Color(0xFF2E2E33))
        val buttonMagenta = ColorProvider(Color(0xFF9E0059))

        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(bodyGray)
                    .cornerRadius(22.dp)
                    .padding(if (compact) 4.dp else 6.dp)
                    .clickable(openArchiveTuneAction(context)),
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left: Olive-Green Dot-Matrix LCD Screen
                Box(
                    modifier =
                        GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight()
                            .background(bezelGray)
                            .cornerRadius(12.dp)
                            .padding(6.dp),
                ) {
                    Box(
                        modifier =
                            GlanceModifier
                                .fillMaxSize()
                                .background(lcdOliveBg)
                                .cornerRadius(6.dp)
                                .padding(6.dp),
                    ) {
                        Column(
                            modifier = GlanceModifier.fillMaxSize(),
                            verticalAlignment = Alignment.Vertical.CenterVertically,
                        ) {
                            // Top Bar: Battery LED & DOT MATRIX STEREO
                            Row(
                                modifier = GlanceModifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Red Battery LED
                                Box(
                                    modifier =
                                        GlanceModifier
                                            .size(6.dp)
                                            .background(batteryRed)
                                            .cornerRadius(999.dp),
                                ) {}
                                Spacer(GlanceModifier.width(4.dp))
                                Text(
                                    text = "DOT MATRIX STEREO",
                                    style =
                                        TextStyle(
                                            color = lcdPixelDark,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                )
                            }

                            Spacer(GlanceModifier.height(3.dp))

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
                                    cornerRadius = 4.dp,
                                    palette = palette,
                                    modifier = GlanceModifier.size(artSize),
                                )

                                Spacer(GlanceModifier.width(8.dp))

                                Column(modifier = GlanceModifier.defaultWeight()) {
                                    Text(
                                        text = state.title,
                                        style =
                                            TextStyle(
                                                color = lcdPixelDark,
                                                fontSize = if (compact) 12.sp else 13.sp,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                        maxLines = 1,
                                    )
                                    Text(
                                        text = state.artist,
                                        style =
                                            TextStyle(
                                                color = lcdPixelMid,
                                                fontSize = 10.sp,
                                            ),
                                        maxLines = 1,
                                    )
                                }
                            }

                            Spacer(GlanceModifier.height(4.dp))

                            // Pixel Block Progress Bar
                            if (state.isAvailable && state.playbackPosition > 0f) {
                                LinearProgressIndicator(
                                    progress = state.playbackPosition,
                                    color = lcdPixelDark,
                                    backgroundColor = lcdPixelMid,
                                    modifier =
                                        GlanceModifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .cornerRadius(1.dp),
                                )
                            }
                        }
                    }
                }

                Spacer(GlanceModifier.width(if (compact) 8.dp else 12.dp))

                // Right: D-Pad & Magenta A/B Action Buttons
                Column(
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    // D-Pad Cross (Skip Previous & Next)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        WidgetControlButton(
                            modifier = GlanceModifier.size(if (compact) 26.dp else 30.dp),
                            action = skipPreviousAction(),
                            icon = R.drawable.skip_previous,
                            contentDescription = context.getString(R.string.widget_previous),
                            backgroundColor = dpadBlack,
                            contentColor = ColorProvider(Color.White),
                            cornerRadius = 4.dp,
                            iconSize = 14.dp,
                        )
                        Spacer(GlanceModifier.width(4.dp))
                        WidgetControlButton(
                            modifier = GlanceModifier.size(if (compact) 26.dp else 30.dp),
                            action = skipNextAction(),
                            icon = R.drawable.skip_next,
                            contentDescription = context.getString(R.string.next),
                            backgroundColor = dpadBlack,
                            contentColor = ColorProvider(Color.White),
                            cornerRadius = 4.dp,
                            iconSize = 14.dp,
                        )
                    }

                    Spacer(GlanceModifier.height(6.dp))

                    // Round Magenta (A) Play/Pause Button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "B",
                            style =
                                TextStyle(
                                    color = ColorProvider(Color(0xFF333338)),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                        Spacer(GlanceModifier.width(4.dp))
                        WidgetControlButton(
                            modifier = GlanceModifier.size(if (compact) 34.dp else 40.dp),
                            action = playPauseAction(),
                            icon = if (state.isPlaying) R.drawable.pause else R.drawable.play,
                            contentDescription =
                                context.getString(
                                    if (state.isPlaying) R.string.widget_pause else R.string.play,
                                ),
                            backgroundColor = buttonMagenta,
                            contentColor = ColorProvider(Color.White),
                            cornerRadius = 999.dp,
                            iconSize = 18.dp,
                        )
                        Spacer(GlanceModifier.width(4.dp))
                        Text(
                            text = "A",
                            style =
                                TextStyle(
                                    color = ColorProvider(Color(0xFF333338)),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                    }
                }
            }
        }
    }
}

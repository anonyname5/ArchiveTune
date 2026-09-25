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

class ConcertTicketWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            ConcertTicketContent(context)
        }
    }
}

@Composable
private fun ConcertTicketContent(context: Context) {
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

        // Concert Ticket Stub Palette
        val ticketPaper = ColorProvider(Color(0xFF242220))
        val ticketBorder = ColorProvider(Color(0xFF383532))
        val perforationLine = ColorProvider(Color(0xFF5A554E))
        val stubBg = ColorProvider(Color(0xFF1E1C1A))
        val goldAccent = ColorProvider(Color(0xFFD4AF37))
        val textPrimary = ColorProvider(Color(0xFFF7F5F0))
        val textSecondary = ColorProvider(Color(0xFFB3ADA4))

        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(ticketPaper)
                    .cornerRadius(18.dp)
                    .padding(if (compact) 6.dp else 10.dp)
                    .clickable(openArchiveTuneAction(context)),
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left Section: Main Ticket Body
                Column(
                    modifier =
                        GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    // Header Bar
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "LIVE TOUR PASS",
                            style =
                                TextStyle(
                                    color = goldAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                        Spacer(GlanceModifier.width(6.dp))
                        Text(
                            text = "• SEC: VIP | ROW: 01",
                            style =
                                TextStyle(
                                    color = textSecondary,
                                    fontSize = 8.sp,
                                ),
                        )
                    }

                    Spacer(GlanceModifier.height(3.dp))

                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val artSize = if (compact) 44.dp else 52.dp
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
                                        fontSize = 11.sp,
                                    ),
                                maxLines = 1,
                            )
                        }
                    }

                    Spacer(GlanceModifier.height(4.dp))

                    // Barcode Simulator Progress Line
                    if (state.isAvailable && state.playbackPosition > 0f) {
                        LinearProgressIndicator(
                            progress = state.playbackPosition,
                            color = goldAccent,
                            backgroundColor = ticketBorder,
                            modifier =
                                GlanceModifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .cornerRadius(2.dp),
                        )
                    }
                }

                Spacer(GlanceModifier.width(6.dp))

                // Perforated Tear Line
                Box(
                    modifier =
                        GlanceModifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(perforationLine),
                ) {}

                Spacer(GlanceModifier.width(6.dp))

                // Right Section: Tear-Off Ticket Stub Controls
                Box(
                    modifier =
                        GlanceModifier
                            .width(if (compact) 72.dp else 84.dp)
                            .fillMaxHeight()
                            .background(stubBg)
                            .cornerRadius(12.dp)
                            .padding(4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                        verticalAlignment = Alignment.Vertical.CenterVertically,
                    ) {
                        Text(
                            text = "ADMIT ONE",
                            style =
                                TextStyle(
                                    color = goldAccent,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )

                        Spacer(GlanceModifier.height(4.dp))

                        WidgetControlButton(
                            modifier = GlanceModifier.size(if (compact) 34.dp else 40.dp),
                            action = playPauseAction(),
                            icon = if (state.isPlaying) R.drawable.pause else R.drawable.play,
                            contentDescription =
                                context.getString(
                                    if (state.isPlaying) R.string.widget_pause else R.string.play,
                                ),
                            backgroundColor = goldAccent,
                            contentColor = ColorProvider(Color(0xFF1E1C1A)),
                            cornerRadius = 8.dp,
                            iconSize = 20.dp,
                        )

                        Spacer(GlanceModifier.height(4.dp))

                        Row {
                            WidgetControlButton(
                                modifier = GlanceModifier.size(24.dp),
                                action = skipPreviousAction(),
                                icon = R.drawable.skip_previous,
                                contentDescription = context.getString(R.string.widget_previous),
                                backgroundColor = ColorProvider(Color.Transparent),
                                contentColor = textSecondary,
                                cornerRadius = 4.dp,
                                iconSize = 14.dp,
                            )
                            Spacer(GlanceModifier.width(4.dp))
                            WidgetControlButton(
                                modifier = GlanceModifier.size(24.dp),
                                action = skipNextAction(),
                                icon = R.drawable.skip_next,
                                contentDescription = context.getString(R.string.next),
                                backgroundColor = ColorProvider(Color.Transparent),
                                contentColor = textSecondary,
                                cornerRadius = 4.dp,
                                iconSize = 14.dp,
                            )
                        }
                    }
                }
            }
        }
    }
}

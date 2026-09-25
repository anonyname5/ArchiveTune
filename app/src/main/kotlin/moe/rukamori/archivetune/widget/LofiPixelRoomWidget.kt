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

class LofiPixelRoomWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            LofiPixelRoomContent(context)
        }
    }
}

@Composable
private fun LofiPixelRoomContent(context: Context) {
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

        // Lo-fi cozy midnight bedroom palette
        val roomBg = ColorProvider(Color(0xFF181524))
        val monitorFrame = ColorProvider(Color(0xFF29233D))
        val lofiLavender = ColorProvider(Color(0xFFCE93D8))
        val lofiYellow = ColorProvider(Color(0xFFFFEE58))
        val lofiTeal = ColorProvider(Color(0xFF80DEEA))
        val textPrimary = ColorProvider(Color(0xFFF3E5F5))
        val textSecondary = ColorProvider(Color(0xFF9E95B3))

        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(roomBg)
                    .cornerRadius(20.dp)
                    .padding(if (compact) 4.dp else 6.dp)
                    .clickable(openArchiveTuneAction(context)),
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Pixel CRT Monitor Framing Album Artwork
                val artSize = if (compact) 72.dp else 88.dp
                Box(
                    modifier =
                        GlanceModifier
                            .size(artSize)
                            .background(monitorFrame)
                            .cornerRadius(12.dp)
                            .padding(4.dp),
                ) {
                    WidgetArtwork(
                        artPath = state.artPath,
                        context = context,
                        contentDescription = context.getString(R.string.album_cover_desc),
                        targetSize = artSize,
                        cornerRadius = 8.dp,
                        palette = palette,
                        modifier = GlanceModifier.fillMaxSize(),
                    )
                }

                Spacer(GlanceModifier.width(if (compact) 10.dp else 14.dp))

                // Right: Lo-fi chill badges, Track Info, and Pixel Arcade Controls
                Column(
                    modifier =
                        GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "★ LO-FI CHILL",
                            style =
                                TextStyle(
                                    color = lofiYellow,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                        Spacer(GlanceModifier.width(6.dp))
                        Image(
                            provider = ImageProvider(R.drawable.graphic_eq),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(lofiLavender),
                            modifier = GlanceModifier.size(12.dp),
                        )
                        Spacer(GlanceModifier.width(4.dp))
                        Text(
                            text = if (state.isPlaying) "PLAYING" else "IDLE",
                            style =
                                TextStyle(
                                    color = textSecondary,
                                    fontSize = 8.sp,
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

                    // Cozy Neon Progress Line
                    if (state.isAvailable && state.playbackPosition > 0f) {
                        LinearProgressIndicator(
                            progress = state.playbackPosition,
                            color = lofiLavender,
                            backgroundColor = ColorProvider(Color(0xFF2C2640)),
                            modifier =
                                GlanceModifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .cornerRadius(2.dp),
                        )
                        Spacer(GlanceModifier.height(6.dp))
                    }

                    // Pixel arcade-style controls
                    if (state.isAvailable) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 30.dp else 34.dp),
                                action = skipPreviousAction(),
                                icon = R.drawable.skip_previous,
                                contentDescription = context.getString(R.string.widget_previous),
                                backgroundColor = monitorFrame,
                                contentColor = textPrimary,
                                cornerRadius = 6.dp,
                                iconSize = 16.dp,
                            )
                            Spacer(GlanceModifier.width(6.dp))
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 34.dp else 38.dp),
                                action = playPauseAction(),
                                icon = if (state.isPlaying) R.drawable.pause else R.drawable.play,
                                contentDescription =
                                    context.getString(
                                        if (state.isPlaying) R.string.widget_pause else R.string.play,
                                    ),
                                backgroundColor = lofiTeal,
                                contentColor = ColorProvider(Color(0xFF00363A)),
                                cornerRadius = 8.dp,
                                iconSize = 20.dp,
                            )
                            Spacer(GlanceModifier.width(6.dp))
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 30.dp else 34.dp),
                                action = skipNextAction(),
                                icon = R.drawable.skip_next,
                                contentDescription = context.getString(R.string.next),
                                backgroundColor = monitorFrame,
                                contentColor = textPrimary,
                                cornerRadius = 6.dp,
                                iconSize = 16.dp,
                            )
                        }
                    }
                }
            }
        }
    }
}

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

class NeonJukeboxWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            NeonJukeboxContent(context)
        }
    }
}

@Composable
private fun NeonJukeboxContent(context: Context) {
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

        // 1950s Chrome & Neon Jukebox palette
        val dinerDarkBg = ColorProvider(Color(0xFF160F1F))
        val neonMagenta = ColorProvider(Color(0xFFE040FB))
        val neonAmber = ColorProvider(Color(0xFFFFB300))
        val chromeSilver = ColorProvider(Color(0xFFD6D9E0))
        val flipCardBg = ColorProvider(Color(0xFFFFF9E6))
        val flipCardText = ColorProvider(Color(0xFF22112A))
        val textSecondary = ColorProvider(Color(0xFFA69CB5))

        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(dinerDarkBg)
                    .cornerRadius(22.dp)
                    .padding(if (compact) 8.dp else 12.dp)
                    .clickable(openArchiveTuneAction(context)),
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Jukebox Chrome Arch with Album Art
                val artSize = if (compact) 72.dp else 88.dp
                Box(
                    modifier =
                        GlanceModifier
                            .size(artSize)
                            .background(chromeSilver)
                            .cornerRadius(16.dp)
                            .padding(3.dp),
                ) {
                    WidgetArtwork(
                        artPath = state.artPath,
                        context = context,
                        contentDescription = context.getString(R.string.album_cover_desc),
                        targetSize = artSize,
                        cornerRadius = 13.dp,
                        palette = palette,
                        modifier = GlanceModifier.fillMaxSize(),
                    )
                }

                Spacer(GlanceModifier.width(if (compact) 10.dp else 14.dp))

                // Right Section: Title Flip Card, Bubble Tube Progress, and Coin Drop Controls
                Column(
                    modifier =
                        GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Header: Selection & Neon Tube status
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "SELECTION: B-12",
                            style =
                                TextStyle(
                                    color = neonAmber,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                        Spacer(GlanceModifier.defaultWeight())
                        Text(
                            text = "✦ BUBBLE TUBES ✦",
                            style =
                                TextStyle(
                                    color = neonMagenta,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                    }

                    Spacer(GlanceModifier.height(3.dp))

                    // Flip Title Card (Vintage cream mechanical strip)
                    Box(
                        modifier =
                            GlanceModifier
                                .fillMaxWidth()
                                .background(flipCardBg)
                                .cornerRadius(6.dp)
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                    ) {
                        Column {
                            Text(
                                text = state.title,
                                style =
                                    TextStyle(
                                        color = flipCardText,
                                        fontSize = if (compact) 12.sp else 13.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                maxLines = 1,
                            )
                            Text(
                                text = state.artist,
                                style =
                                    TextStyle(
                                        color = ColorProvider(Color(0xFF5A4462)),
                                        fontSize = 10.sp,
                                    ),
                                maxLines = 1,
                            )
                        }
                    }

                    Spacer(GlanceModifier.height(4.dp))

                    // Liquid Bubble Neon Progress Bar
                    if (state.isAvailable && state.playbackPosition > 0f) {
                        LinearProgressIndicator(
                            progress = state.playbackPosition,
                            color = neonMagenta,
                            backgroundColor = ColorProvider(Color(0xFF2C1940)),
                            modifier =
                                GlanceModifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .cornerRadius(2.dp),
                        )
                        Spacer(GlanceModifier.height(6.dp))
                    }

                    // Jukebox Playback Controls
                    if (state.isAvailable) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 28.dp else 32.dp),
                                action = skipPreviousAction(),
                                icon = R.drawable.skip_previous,
                                contentDescription = context.getString(R.string.widget_previous),
                                backgroundColor = ColorProvider(Color(0xFF2D2040)),
                                contentColor = chromeSilver,
                                cornerRadius = 6.dp,
                                iconSize = 16.dp,
                            )
                            Spacer(GlanceModifier.width(8.dp))
                            // Glowing Amber Coin Drop Button
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 34.dp else 38.dp),
                                action = playPauseAction(),
                                icon = if (state.isPlaying) R.drawable.pause else R.drawable.play,
                                contentDescription =
                                    context.getString(
                                        if (state.isPlaying) R.string.widget_pause else R.string.play,
                                    ),
                                backgroundColor = neonAmber,
                                contentColor = ColorProvider(Color(0xFF1E1000)),
                                cornerRadius = 999.dp,
                                iconSize = 18.dp,
                            )
                            Spacer(GlanceModifier.width(8.dp))
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 28.dp else 32.dp),
                                action = skipNextAction(),
                                icon = R.drawable.skip_next,
                                contentDescription = context.getString(R.string.next),
                                backgroundColor = ColorProvider(Color(0xFF2D2040)),
                                contentColor = chromeSilver,
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

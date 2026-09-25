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

class SwissBrutalistWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            SwissBrutalistContent(context)
        }
    }
}

@Composable
private fun SwissBrutalistContent(context: Context) {
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

        // Swiss Brutalist stark monochrome palette
        val brutalistBg = ColorProvider(Color(0xFF0F0F12))
        val brutalistWhite = ColorProvider(Color(0xFFFFFFFF))
        val brutalistGray = ColorProvider(Color(0xFF888890))
        val brutalistRed = ColorProvider(Color(0xFFFF2A2A))
        val buttonBg = ColorProvider(Color(0xFF222228))

        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(brutalistBg)
                    .cornerRadius(16.dp)
                    .padding(if (compact) 8.dp else 12.dp)
                    .clickable(openArchiveTuneAction(context)),
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left: Editorial Typography & Brutalist Angular Controls
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
                            text = "№ 04 // SOUND ARCHIVE",
                            style =
                                TextStyle(
                                    color = brutalistRed,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                        Spacer(GlanceModifier.defaultWeight())
                        Text(
                            text = "SWISS GRID",
                            style =
                                TextStyle(
                                    color = brutalistGray,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                    }

                    Spacer(GlanceModifier.height(3.dp))

                    Text(
                        text = state.title.uppercase(),
                        style =
                            TextStyle(
                                color = brutalistWhite,
                                fontSize = if (compact) 13.sp else 15.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        maxLines = 1,
                    )

                    Text(
                        text = state.artist.uppercase(),
                        style =
                            TextStyle(
                                color = brutalistGray,
                                fontSize = if (compact) 10.sp else 11.sp,
                            ),
                        maxLines = 1,
                    )

                    Spacer(GlanceModifier.height(4.dp))

                    if (state.isAvailable && state.playbackPosition > 0f) {
                        LinearProgressIndicator(
                            progress = state.playbackPosition,
                            color = brutalistWhite,
                            backgroundColor = ColorProvider(Color(0xFF282830)),
                            modifier =
                                GlanceModifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .cornerRadius(1.dp),
                        )
                        Spacer(GlanceModifier.height(6.dp))
                    }

                    // Sharp Angular Brutalist Buttons
                    if (state.isAvailable) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 28.dp else 32.dp),
                                action = skipPreviousAction(),
                                icon = R.drawable.skip_previous,
                                contentDescription = context.getString(R.string.widget_previous),
                                backgroundColor = buttonBg,
                                contentColor = brutalistWhite,
                                cornerRadius = 3.dp,
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
                                backgroundColor = brutalistWhite,
                                contentColor = ColorProvider(Color.Black),
                                cornerRadius = 3.dp,
                                iconSize = 18.dp,
                            )
                            Spacer(GlanceModifier.width(6.dp))
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 28.dp else 32.dp),
                                action = skipNextAction(),
                                icon = R.drawable.skip_next,
                                contentDescription = context.getString(R.string.next),
                                backgroundColor = buttonBg,
                                contentColor = brutalistWhite,
                                cornerRadius = 3.dp,
                                iconSize = 16.dp,
                            )
                        }
                    }
                }

                Spacer(GlanceModifier.width(if (compact) 10.dp else 14.dp))

                // Right: Full-Bleed Sharp Album Artwork
                val artSize = if (compact) 76.dp else 92.dp
                Box(
                    modifier =
                        GlanceModifier
                            .size(artSize)
                            .cornerRadius(4.dp),
                ) {
                    WidgetArtwork(
                        artPath = state.artPath,
                        context = context,
                        contentDescription = context.getString(R.string.album_cover_desc),
                        targetSize = artSize,
                        cornerRadius = 4.dp,
                        palette = palette,
                        modifier = GlanceModifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

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

class BraunMinimalistWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            BraunMinimalistContent(context)
        }
    }
}

@Composable
private fun BraunMinimalistContent(context: Context) {
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

        // Dieter Rams / Braun industrial palette
        val braunChassis = ColorProvider(Color(0xFFEBE8E1))
        val textBlack = ColorProvider(Color(0xFF1E1F22))
        val textGray = ColorProvider(Color(0xFF6E7179))
        val braunOrange = ColorProvider(Color(0xFFFF5722))
        val grilleHole = ColorProvider(Color(0xFFD0CDC5))
        val progressTrack = ColorProvider(Color(0xFFD5D2CA))

        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(braunChassis)
                    .cornerRadius(18.dp)
                    .padding(if (compact) 4.dp else 6.dp)
                    .clickable(openArchiveTuneAction(context)),
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left: Braun Speaker Grille Perforation Columns
                Column(
                    modifier =
                        GlanceModifier
                            .width(18.dp)
                            .fillMaxHeight(),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                ) {
                    repeat(6) {
                        Row {
                            Box(
                                modifier =
                                    GlanceModifier
                                        .size(4.dp)
                                        .background(grilleHole)
                                        .cornerRadius(999.dp),
                            ) {}
                            Spacer(GlanceModifier.width(3.dp))
                            Box(
                                modifier =
                                    GlanceModifier
                                        .size(4.dp)
                                        .background(grilleHole)
                                        .cornerRadius(999.dp),
                            ) {}
                        }
                        Spacer(GlanceModifier.height(4.dp))
                    }
                }

                Spacer(GlanceModifier.width(10.dp))

                // Square Artwork Frame
                val artSize = if (compact) 62.dp else 74.dp
                Box(
                    modifier =
                        GlanceModifier
                            .size(artSize)
                            .background(grilleHole)
                            .cornerRadius(6.dp)
                            .padding(2.dp),
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

                Spacer(GlanceModifier.width(if (compact) 8.dp else 12.dp))

                // Middle: Clean Helvetica Typography & Hairline Progress
                Column(
                    modifier =
                        GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "BRAUN • ARCHIVETUNE",
                        style =
                            TextStyle(
                                color = textGray,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                    )

                    Spacer(GlanceModifier.height(2.dp))

                    Text(
                        text = state.title,
                        style =
                            TextStyle(
                                color = textBlack,
                                fontSize = if (compact) 13.sp else 15.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        maxLines = 1,
                    )

                    Text(
                        text = state.artist,
                        style =
                            TextStyle(
                                color = textGray,
                                fontSize = if (compact) 10.sp else 11.sp,
                            ),
                        maxLines = 1,
                    )

                    Spacer(GlanceModifier.height(4.dp))

                    if (state.isAvailable && state.playbackPosition > 0f) {
                        LinearProgressIndicator(
                            progress = state.playbackPosition,
                            color = textBlack,
                            backgroundColor = progressTrack,
                            modifier =
                                GlanceModifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .cornerRadius(1.dp),
                        )
                    }
                }

                // Right: Signature Orange Rotary Dial Power/Play Button
                if (state.isAvailable) {
                    Spacer(GlanceModifier.width(8.dp))
                    Column(
                        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                        verticalAlignment = Alignment.Vertical.CenterVertically,
                    ) {
                        // Signature Circular Orange Play Toggle
                        WidgetControlButton(
                            modifier = GlanceModifier.size(if (compact) 36.dp else 42.dp),
                            action = playPauseAction(),
                            icon = if (state.isPlaying) R.drawable.pause else R.drawable.play,
                            contentDescription =
                                context.getString(
                                    if (state.isPlaying) R.string.widget_pause else R.string.play,
                                ),
                            backgroundColor = braunOrange,
                            contentColor = ColorProvider(Color.White),
                            cornerRadius = 999.dp,
                            iconSize = 20.dp,
                        )

                        Spacer(GlanceModifier.height(6.dp))

                        Row {
                            WidgetControlButton(
                                modifier = GlanceModifier.size(24.dp),
                                action = skipPreviousAction(),
                                icon = R.drawable.skip_previous,
                                contentDescription = context.getString(R.string.widget_previous),
                                backgroundColor = ColorProvider(Color.Transparent),
                                contentColor = textBlack,
                                cornerRadius = 999.dp,
                                iconSize = 14.dp,
                            )
                            Spacer(GlanceModifier.width(4.dp))
                            WidgetControlButton(
                                modifier = GlanceModifier.size(24.dp),
                                action = skipNextAction(),
                                icon = R.drawable.skip_next,
                                contentDescription = context.getString(R.string.next),
                                backgroundColor = ColorProvider(Color.Transparent),
                                contentColor = textBlack,
                                cornerRadius = 999.dp,
                                iconSize = 14.dp,
                            )
                        }
                    }
                }
            }
        }
    }
}

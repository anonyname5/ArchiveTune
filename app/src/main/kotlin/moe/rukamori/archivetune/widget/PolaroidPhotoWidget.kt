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

class PolaroidPhotoWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            PolaroidPhotoContent(context)
        }
    }
}

@Composable
private fun PolaroidPhotoContent(context: Context) {
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

        // Authentic Polaroid film frame colors
        val polaroidWhite = ColorProvider(Color(0xFFF7F5F0))
        val photoBezel = ColorProvider(Color(0xFFE4DFD6))
        val sharpieBlack = ColorProvider(Color(0xFF202024))
        val sharpieGray = ColorProvider(Color(0xFF6B6A72))
        val filmRed = ColorProvider(Color(0xFFD32F2F))

        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(polaroidWhite)
                    .cornerRadius(18.dp)
                    .padding(if (compact) 4.dp else 6.dp)
                    .clickable(openArchiveTuneAction(context)),
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Polaroid Snapshot Photo Frame
                val photoSize = if (compact) 76.dp else 92.dp
                Box(
                    modifier =
                        GlanceModifier
                            .size(photoSize)
                            .background(photoBezel)
                            .cornerRadius(8.dp)
                            .padding(2.dp),
                ) {
                    WidgetArtwork(
                        artPath = state.artPath,
                        context = context,
                        contentDescription = context.getString(R.string.album_cover_desc),
                        targetSize = photoSize,
                        cornerRadius = 6.dp,
                        palette = palette,
                        modifier = GlanceModifier.fillMaxSize(),
                    )
                }

                Spacer(GlanceModifier.width(if (compact) 10.dp else 14.dp))

                // Right: Handwritten Title, Film Stamp & Shutter Controls
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
                            text = "POLAROID 600",
                            style =
                                TextStyle(
                                    color = filmRed,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                        Spacer(GlanceModifier.width(6.dp))
                        Text(
                            text = "• INSTANT PRINT",
                            style =
                                TextStyle(
                                    color = sharpieGray,
                                    fontSize = 8.sp,
                                ),
                        )
                    }

                    Spacer(GlanceModifier.height(3.dp))

                    Text(
                        text = "♪ ${state.title}",
                        style =
                            TextStyle(
                                color = sharpieBlack,
                                fontSize = if (compact) 13.sp else 15.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        maxLines = 1,
                    )

                    Text(
                        text = state.artist,
                        style =
                            TextStyle(
                                color = sharpieGray,
                                fontSize = if (compact) 10.sp else 12.sp,
                            ),
                        maxLines = 1,
                    )

                    Spacer(GlanceModifier.height(4.dp))

                    // Minimalist subtle film progress
                    if (state.isAvailable && state.playbackPosition > 0f) {
                        LinearProgressIndicator(
                            progress = state.playbackPosition,
                            color = filmRed,
                            backgroundColor = photoBezel,
                            modifier =
                                GlanceModifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .cornerRadius(2.dp),
                        )
                        Spacer(GlanceModifier.height(6.dp))
                    }

                    // Shutter-style Controls
                    if (state.isAvailable) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 30.dp else 34.dp),
                                action = skipPreviousAction(),
                                icon = R.drawable.skip_previous,
                                contentDescription = context.getString(R.string.widget_previous),
                                backgroundColor = photoBezel,
                                contentColor = sharpieBlack,
                                cornerRadius = 999.dp,
                                iconSize = 16.dp,
                            )
                            Spacer(GlanceModifier.width(8.dp))
                            // Camera Shutter Button (Red circular accent)
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 36.dp else 40.dp),
                                action = playPauseAction(),
                                icon = if (state.isPlaying) R.drawable.pause else R.drawable.play,
                                contentDescription =
                                    context.getString(
                                        if (state.isPlaying) R.string.widget_pause else R.string.play,
                                    ),
                                backgroundColor = filmRed,
                                contentColor = ColorProvider(Color.White),
                                cornerRadius = 999.dp,
                                iconSize = 20.dp,
                            )
                            Spacer(GlanceModifier.width(8.dp))
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 30.dp else 34.dp),
                                action = skipNextAction(),
                                icon = R.drawable.skip_next,
                                contentDescription = context.getString(R.string.next),
                                backgroundColor = photoBezel,
                                contentColor = sharpieBlack,
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

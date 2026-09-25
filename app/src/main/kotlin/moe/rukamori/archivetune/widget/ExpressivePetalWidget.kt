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

class ExpressivePetalWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            ExpressivePetalContent(context)
        }
    }
}

@Composable
private fun ExpressivePetalContent(context: Context) {
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
        val isSquare = size.width < 180.dp || size.width <= size.height * 1.25f

        // Material 3 Expressive organic palette
        val petalBg = palette.primaryContainer
        val petalInner = palette.surface
        val petalAccent = palette.progress
        val textPrimary = palette.onSurface
        val textSecondary = palette.onSurfaceVariant

        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(petalBg)
                    .cornerRadius(34.dp)
                    .padding(4.dp)
                    .clickable(openArchiveTuneAction(context)),
        ) {
            if (isSquare) {
                // Square 2x2 Flower / Scalloped Petal Layout
                Column(
                    modifier =
                        GlanceModifier
                            .fillMaxSize()
                            .background(petalInner)
                            .cornerRadius(26.dp)
                            .padding(8.dp),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    val artSize = (size.width - 40.dp).coerceIn(48.dp, 72.dp)
                    WidgetArtwork(
                        artPath = state.artPath,
                        context = context,
                        contentDescription = context.getString(R.string.album_cover_desc),
                        targetSize = artSize,
                        cornerRadius = 999.dp,
                        palette = palette,
                        modifier = GlanceModifier.size(artSize).cornerRadius(999.dp),
                    )

                    Spacer(GlanceModifier.height(4.dp))

                    Text(
                        text = state.title,
                        style =
                            TextStyle(
                                color = textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        maxLines = 1,
                    )

                    Spacer(GlanceModifier.height(4.dp))

                    if (state.isAvailable) {
                        WidgetControlButton(
                            modifier = GlanceModifier.size(34.dp),
                            action = playPauseAction(),
                            icon = if (state.isPlaying) R.drawable.pause else R.drawable.play,
                            contentDescription =
                                context.getString(
                                    if (state.isPlaying) R.string.widget_pause else R.string.play,
                                ),
                            backgroundColor = petalAccent,
                            contentColor = palette.surface,
                            cornerRadius = 999.dp,
                            iconSize = 18.dp,
                        )
                    }
                }
            } else {
                // Wide 4x2 Petal Pill Layout
                Row(
                    modifier =
                        GlanceModifier
                            .fillMaxSize()
                            .background(petalInner)
                            .cornerRadius(26.dp)
                            .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val artSize = 68.dp
                    WidgetArtwork(
                        artPath = state.artPath,
                        context = context,
                        contentDescription = context.getString(R.string.album_cover_desc),
                        targetSize = artSize,
                        cornerRadius = 999.dp,
                        palette = palette,
                        modifier = GlanceModifier.size(artSize).cornerRadius(999.dp),
                    )

                    Spacer(GlanceModifier.width(12.dp))

                    Column(
                        modifier =
                            GlanceModifier
                                .defaultWeight()
                                .fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "EXPRESSIVE ✿",
                            style =
                                TextStyle(
                                    color = petalAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )

                        Spacer(GlanceModifier.height(2.dp))

                        Text(
                            text = state.title,
                            style =
                                TextStyle(
                                    color = textPrimary,
                                    fontSize = 14.sp,
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

                        Spacer(GlanceModifier.height(4.dp))

                        if (state.isAvailable && state.playbackPosition > 0f) {
                            LinearProgressIndicator(
                                progress = state.playbackPosition,
                                color = petalAccent,
                                backgroundColor = palette.secondaryContainer,
                                modifier =
                                    GlanceModifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .cornerRadius(2.dp),
                            )
                            Spacer(GlanceModifier.height(6.dp))
                        }

                        if (state.isAvailable) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                WidgetControlButton(
                                    modifier = GlanceModifier.size(30.dp),
                                    action = skipPreviousAction(),
                                    icon = R.drawable.skip_previous,
                                    contentDescription = context.getString(R.string.widget_previous),
                                    backgroundColor = palette.secondaryContainer,
                                    contentColor = textPrimary,
                                    cornerRadius = 999.dp,
                                    iconSize = 16.dp,
                                )
                                Spacer(GlanceModifier.width(8.dp))
                                WidgetControlButton(
                                    modifier = GlanceModifier.size(36.dp),
                                    action = playPauseAction(),
                                    icon = if (state.isPlaying) R.drawable.pause else R.drawable.play,
                                    contentDescription =
                                        context.getString(
                                            if (state.isPlaying) R.string.widget_pause else R.string.play,
                                        ),
                                    backgroundColor = petalAccent,
                                    contentColor = palette.surface,
                                    cornerRadius = 999.dp,
                                    iconSize = 20.dp,
                                )
                                Spacer(GlanceModifier.width(8.dp))
                                WidgetControlButton(
                                    modifier = GlanceModifier.size(30.dp),
                                    action = skipNextAction(),
                                    icon = R.drawable.skip_next,
                                    contentDescription = context.getString(R.string.next),
                                    backgroundColor = palette.secondaryContainer,
                                    contentColor = textPrimary,
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
}

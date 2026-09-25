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

class QueueFlowWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            QueueFlowContent(context)
        }
    }
}

@Composable
private fun QueueFlowContent(context: Context) {
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

        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(palette.surface)
                    .cornerRadius(22.dp)
                    .padding(if (compact) 8.dp else 12.dp)
                    .clickable(openArchiveTuneAction(context)),
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left Half: Current Song & Playback Controls
                Column(
                    modifier =
                        GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val artSize = if (compact) 42.dp else 50.dp
                        WidgetArtwork(
                            artPath = state.artPath,
                            context = context,
                            contentDescription = context.getString(R.string.album_cover_desc),
                            targetSize = artSize,
                            cornerRadius = 8.dp,
                            palette = palette,
                            modifier = GlanceModifier.size(artSize),
                        )

                        Spacer(GlanceModifier.width(8.dp))

                        Column(modifier = GlanceModifier.defaultWeight()) {
                            Text(
                                text = "NOW PLAYING",
                                style =
                                    TextStyle(
                                        color = palette.progress,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                            )
                            Text(
                                text = state.title,
                                style =
                                    TextStyle(
                                        color = palette.onSurface,
                                        fontSize = if (compact) 12.sp else 13.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                maxLines = 1,
                            )
                            Text(
                                text = state.artist,
                                style =
                                    TextStyle(
                                        color = palette.onSurfaceVariant,
                                        fontSize = 10.sp,
                                    ),
                                maxLines = 1,
                            )
                        }
                    }

                    Spacer(GlanceModifier.height(4.dp))

                    if (state.isAvailable && state.playbackPosition > 0f) {
                        LinearProgressIndicator(
                            progress = state.playbackPosition,
                            color = palette.progress,
                            backgroundColor = palette.progressTrack,
                            modifier =
                                GlanceModifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .cornerRadius(2.dp),
                        )
                        Spacer(GlanceModifier.height(6.dp))
                    }

                    if (state.isAvailable) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WidgetControlButton(
                                modifier = GlanceModifier.size(28.dp),
                                action = skipPreviousAction(),
                                icon = R.drawable.skip_previous,
                                contentDescription = context.getString(R.string.widget_previous),
                                backgroundColor = palette.secondaryContainer,
                                contentColor = palette.onSecondaryContainer,
                                cornerRadius = 6.dp,
                                iconSize = 14.dp,
                            )
                            Spacer(GlanceModifier.width(6.dp))
                            WidgetControlButton(
                                modifier = GlanceModifier.size(34.dp),
                                action = playPauseAction(),
                                icon = if (state.isPlaying) R.drawable.pause else R.drawable.play,
                                contentDescription =
                                    context.getString(
                                        if (state.isPlaying) R.string.widget_pause else R.string.play,
                                    ),
                                backgroundColor = palette.primaryContainer,
                                contentColor = palette.onPrimaryContainer,
                                cornerRadius = 8.dp,
                                iconSize = 18.dp,
                            )
                            Spacer(GlanceModifier.width(6.dp))
                            WidgetControlButton(
                                modifier = GlanceModifier.size(28.dp),
                                action = skipNextAction(),
                                icon = R.drawable.skip_next,
                                contentDescription = context.getString(R.string.next),
                                backgroundColor = palette.secondaryContainer,
                                contentColor = palette.onSecondaryContainer,
                                cornerRadius = 6.dp,
                                iconSize = 14.dp,
                            )
                        }
                    }
                }

                Spacer(GlanceModifier.width(10.dp))

                // Divider
                Box(
                    modifier =
                        GlanceModifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(palette.progressTrack),
                ) {}

                Spacer(GlanceModifier.width(10.dp))

                // Right Half: "UP NEXT" Queue Card
                Column(
                    modifier =
                        GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    Text(
                        text = "UP NEXT IN QUEUE",
                        style =
                            TextStyle(
                                color = palette.onSurfaceVariant,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                    )

                    Spacer(GlanceModifier.height(4.dp))

                    Box(
                        modifier =
                            GlanceModifier
                                .fillMaxWidth()
                                .background(palette.secondaryContainer)
                                .cornerRadius(8.dp)
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                    ) {
                        Column {
                            Text(
                                text = "1. Next Track Flow",
                                style =
                                    TextStyle(
                                        color = palette.onSecondaryContainer,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                maxLines = 1,
                            )
                            Text(
                                text = "Tap to view full queue",
                                style =
                                    TextStyle(
                                        color = palette.onSurfaceVariant,
                                        fontSize = 9.sp,
                                    ),
                                maxLines = 1,
                            )
                        }
                    }

                    Spacer(GlanceModifier.height(4.dp))

                    Box(
                        modifier =
                            GlanceModifier
                                .fillMaxWidth()
                                .background(palette.secondaryContainer)
                                .cornerRadius(8.dp)
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                    ) {
                        Column {
                            Text(
                                text = "2. Auto Playlists / Mix",
                                style =
                                    TextStyle(
                                        color = palette.onSecondaryContainer,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                maxLines = 1,
                            )
                            Text(
                                text = "Continuous streaming",
                                style =
                                    TextStyle(
                                        color = palette.onSurfaceVariant,
                                        fontSize = 9.sp,
                                    ),
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

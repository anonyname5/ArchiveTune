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

class MusicHubWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            MusicHubContent(context)
        }
    }
}

@Composable
private fun MusicHubContent(context: Context) {
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
        val compact = size.width < 290.dp || size.height < 120.dp

        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(palette.surface)
                    .cornerRadius(24.dp)
                    .padding(if (compact) 4.dp else 6.dp)
                    .clickable(openArchiveTuneAction(context)),
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                // Top: Now Playing Deck
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
                        cornerRadius = 10.dp,
                        palette = palette,
                        modifier = GlanceModifier.size(artSize),
                    )

                    Spacer(GlanceModifier.width(10.dp))

                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = state.title,
                            style =
                                TextStyle(
                                    color = palette.onSurface,
                                    fontSize = if (compact) 13.sp else 15.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                            maxLines = 1,
                        )
                        Text(
                            text = state.artist,
                            style =
                                TextStyle(
                                    color = palette.onSurfaceVariant,
                                    fontSize = if (compact) 11.sp else 12.sp,
                                ),
                            maxLines = 1,
                        )
                    }

                    if (state.isAvailable) {
                        Spacer(GlanceModifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WidgetControlButton(
                                modifier = GlanceModifier.size(32.dp),
                                action = skipPreviousAction(),
                                icon = R.drawable.skip_previous,
                                contentDescription = context.getString(R.string.widget_previous),
                                backgroundColor = palette.secondaryContainer,
                                contentColor = palette.onSecondaryContainer,
                                cornerRadius = 999.dp,
                                iconSize = 16.dp,
                            )
                            Spacer(GlanceModifier.width(6.dp))
                            WidgetControlButton(
                                modifier = GlanceModifier.size(38.dp),
                                action = playPauseAction(),
                                icon = if (state.isPlaying) R.drawable.pause else R.drawable.play,
                                contentDescription =
                                    context.getString(
                                        if (state.isPlaying) R.string.widget_pause else R.string.play,
                                    ),
                                backgroundColor = palette.primaryContainer,
                                contentColor = palette.onPrimaryContainer,
                                cornerRadius = 999.dp,
                                iconSize = 20.dp,
                            )
                            Spacer(GlanceModifier.width(6.dp))
                            WidgetControlButton(
                                modifier = GlanceModifier.size(32.dp),
                                action = skipNextAction(),
                                icon = R.drawable.skip_next,
                                contentDescription = context.getString(R.string.next),
                                backgroundColor = palette.secondaryContainer,
                                contentColor = palette.onSecondaryContainer,
                                cornerRadius = 999.dp,
                                iconSize = 16.dp,
                            )
                        }
                    }
                }

                Spacer(GlanceModifier.height(6.dp))

                // Progress Bar
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
                    Spacer(GlanceModifier.height(8.dp))
                }

                // Bottom: Quick Launch Command Dock
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val tileModifier =
                        GlanceModifier
                            .defaultWeight()
                            .height(if (compact) 32.dp else 36.dp)
                            .background(palette.secondaryContainer)
                            .cornerRadius(10.dp)
                            .clickable(openArchiveTuneAction(context))

                    Box(
                        modifier = tileModifier,
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "⚡ Mix",
                            style =
                                TextStyle(
                                    color = palette.onSecondaryContainer,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                    }

                    Spacer(GlanceModifier.width(6.dp))

                    Box(
                        modifier = tileModifier,
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "♥ Liked",
                            style =
                                TextStyle(
                                    color = palette.onSecondaryContainer,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                    }

                    Spacer(GlanceModifier.width(6.dp))

                    Box(
                        modifier = tileModifier,
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "📥 Saved",
                            style =
                                TextStyle(
                                    color = palette.onSecondaryContainer,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                    }

                    Spacer(GlanceModifier.width(6.dp))

                    Box(
                        modifier = tileModifier,
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "🔍 Search",
                            style =
                                TextStyle(
                                    color = palette.onSecondaryContainer,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                    }
                }
            }
        }
    }
}

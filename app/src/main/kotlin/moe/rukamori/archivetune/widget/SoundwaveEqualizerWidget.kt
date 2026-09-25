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

class SoundwaveEqualizerWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            SoundwaveEqualizerContent(context)
        }
    }
}

@Composable
private fun SoundwaveEqualizerContent(context: Context) {
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

        // Audiophile soundwave spectrum palette
        val chassisBg = ColorProvider(Color(0xFF13151A))
        val eqCyan = ColorProvider(Color(0xFF00E5FF))
        val eqBarDim = ColorProvider(Color(0xFF1E2833))
        val textPrimary = ColorProvider(Color(0xFFF2F5F8))
        val textSecondary = ColorProvider(Color(0xFF8590A0))
        val buttonBg = ColorProvider(Color(0xFF202530))

        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(chassisBg)
                    .cornerRadius(22.dp)
                    .padding(if (compact) 4.dp else 6.dp)
                    .clickable(openArchiveTuneAction(context)),
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left: Album Art with Hi-Res Badge
                val artSize = if (compact) 72.dp else 88.dp
                Box(
                    modifier =
                        GlanceModifier
                            .size(artSize)
                            .background(buttonBg)
                            .cornerRadius(12.dp)
                            .padding(2.dp),
                ) {
                    WidgetArtwork(
                        artPath = state.artPath,
                        context = context,
                        contentDescription = context.getString(R.string.album_cover_desc),
                        targetSize = artSize,
                        cornerRadius = 10.dp,
                        palette = palette,
                        modifier = GlanceModifier.fillMaxSize(),
                    )
                }

                Spacer(GlanceModifier.width(if (compact) 10.dp else 14.dp))

                // Right Section: Title, Multi-Bar Spectrum Equalizer, Progress, and Controls
                Column(
                    modifier =
                        GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Header Bar
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "SPECTRUM EQ",
                            style =
                                TextStyle(
                                    color = eqCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                        Spacer(GlanceModifier.defaultWeight())
                        Text(
                            text = "24-BIT • FLAC",
                            style =
                                TextStyle(
                                    color = textSecondary,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                    }

                    Spacer(GlanceModifier.height(2.dp))

                    Text(
                        text = state.title,
                        style =
                            TextStyle(
                                color = textPrimary,
                                fontSize = if (compact) 13.sp else 15.sp,
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

                    // Multi-band Graphic Equalizer Visualizer Bars
                    val heights =
                        if (state.isPlaying) {
                            listOf(8.dp, 16.dp, 22.dp, 14.dp, 24.dp, 18.dp, 12.dp, 20.dp, 15.dp, 10.dp, 22.dp, 16.dp, 8.dp)
                        } else {
                            listOf(4.dp, 6.dp, 8.dp, 6.dp, 8.dp, 6.dp, 5.dp, 8.dp, 6.dp, 4.dp, 7.dp, 5.dp, 4.dp)
                        }

                    Row(
                        modifier = GlanceModifier.fillMaxWidth().height(24.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        heights.forEach { barHeight ->
                            Box(
                                modifier =
                                    GlanceModifier
                                        .defaultWeight()
                                        .height(barHeight)
                                        .background(if (state.isPlaying) eqCyan else eqBarDim)
                                        .cornerRadius(2.dp),
                            ) {}
                            Spacer(GlanceModifier.width(2.dp))
                        }
                    }

                    Spacer(GlanceModifier.height(4.dp))

                    // Linear Progress Bar
                    if (state.isAvailable && state.playbackPosition > 0f) {
                        LinearProgressIndicator(
                            progress = state.playbackPosition,
                            color = eqCyan,
                            backgroundColor = eqBarDim,
                            modifier =
                                GlanceModifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .cornerRadius(2.dp),
                        )
                        Spacer(GlanceModifier.height(6.dp))
                    }

                    // Transport Controls
                    if (state.isAvailable) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 28.dp else 32.dp),
                                action = skipPreviousAction(),
                                icon = R.drawable.skip_previous,
                                contentDescription = context.getString(R.string.widget_previous),
                                backgroundColor = buttonBg,
                                contentColor = textPrimary,
                                cornerRadius = 6.dp,
                                iconSize = 16.dp,
                            )
                            Spacer(GlanceModifier.width(8.dp))
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 34.dp else 38.dp),
                                action = playPauseAction(),
                                icon = if (state.isPlaying) R.drawable.pause else R.drawable.play,
                                contentDescription =
                                    context.getString(
                                        if (state.isPlaying) R.string.widget_pause else R.string.play,
                                    ),
                                backgroundColor = eqCyan,
                                contentColor = ColorProvider(Color(0xFF0A1820)),
                                cornerRadius = 8.dp,
                                iconSize = 18.dp,
                            )
                            Spacer(GlanceModifier.width(8.dp))
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 28.dp else 32.dp),
                                action = skipNextAction(),
                                icon = R.drawable.skip_next,
                                contentDescription = context.getString(R.string.next),
                                backgroundColor = buttonBg,
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

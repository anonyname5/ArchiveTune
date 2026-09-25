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

class ZenTanzakuWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            ZenTanzakuContent(context)
        }
    }
}

@Composable
private fun ZenTanzakuContent(context: Context) {
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
        val isVertical = size.height > size.width * 1.1f

        // Washi paper & sumi-e ink colors
        val washiPaperBg = ColorProvider(Color(0xFFF7F4EE))
        val ensoInk = ColorProvider(Color(0xFF2C2E33))
        val hankoRed = ColorProvider(Color(0xFFB71C1C))
        val inkSecondary = ColorProvider(Color(0xFF6E7179))
        val bambooLine = ColorProvider(Color(0xFFDCD7CC))

        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(washiPaperBg)
                    .cornerRadius(18.dp)
                    .padding(4.dp)
                    .clickable(openArchiveTuneAction(context)),
        ) {
            if (isVertical) {
                // Portrait Tanzaku Scroll Layout
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    // Red Hanko Seal Stamp
                    Box(
                        modifier =
                            GlanceModifier
                                .size(24.dp)
                                .background(hankoRed)
                                .cornerRadius(4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "音",
                            style =
                                TextStyle(
                                    color = ColorProvider(Color.White),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                    }

                    Spacer(GlanceModifier.height(8.dp))

                    // Ensō Circular Artwork
                    val artSize = (size.width - 36.dp).coerceIn(60.dp, 100.dp)
                    Box(
                        modifier =
                            GlanceModifier
                                .size(artSize)
                                .background(ensoInk)
                                .cornerRadius(999.dp)
                                .padding(2.dp),
                    ) {
                        WidgetArtwork(
                            artPath = state.artPath,
                            context = context,
                            contentDescription = context.getString(R.string.album_cover_desc),
                            targetSize = artSize,
                            cornerRadius = 999.dp,
                            palette = palette,
                            modifier = GlanceModifier.fillMaxSize().cornerRadius(999.dp),
                        )
                    }

                    Spacer(GlanceModifier.height(8.dp))

                    Text(
                        text = state.title,
                        style =
                            TextStyle(
                                color = ensoInk,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        maxLines = 1,
                    )
                    Text(
                        text = state.artist,
                        style =
                            TextStyle(
                                color = inkSecondary,
                                fontSize = 11.sp,
                            ),
                        maxLines = 1,
                    )

                    Spacer(GlanceModifier.height(6.dp))

                    if (state.isAvailable && state.playbackPosition > 0f) {
                        LinearProgressIndicator(
                            progress = state.playbackPosition,
                            color = hankoRed,
                            backgroundColor = bambooLine,
                            modifier =
                                GlanceModifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .cornerRadius(2.dp),
                        )
                        Spacer(GlanceModifier.height(8.dp))
                    }

                    // Zen Minimalist Controls
                    if (state.isAvailable) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            WidgetControlButton(
                                modifier = GlanceModifier.size(28.dp),
                                action = skipPreviousAction(),
                                icon = R.drawable.skip_previous,
                                contentDescription = context.getString(R.string.widget_previous),
                                backgroundColor = ColorProvider(Color.Transparent),
                                contentColor = ensoInk,
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
                                backgroundColor = ensoInk,
                                contentColor = ColorProvider(Color.White),
                                cornerRadius = 999.dp,
                                iconSize = 18.dp,
                            )
                            Spacer(GlanceModifier.width(8.dp))
                            WidgetControlButton(
                                modifier = GlanceModifier.size(28.dp),
                                action = skipNextAction(),
                                icon = R.drawable.skip_next,
                                contentDescription = context.getString(R.string.next),
                                backgroundColor = ColorProvider(Color.Transparent),
                                contentColor = ensoInk,
                                cornerRadius = 999.dp,
                                iconSize = 16.dp,
                            )
                        }
                    }
                }
            } else {
                // Horizontal Tanzaku Banner Layout
                Row(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Ensō Circular Artwork Frame
                    val artSize = 74.dp
                    Box(
                        modifier =
                            GlanceModifier
                                .size(artSize)
                                .background(ensoInk)
                                .cornerRadius(999.dp)
                                .padding(2.dp),
                    ) {
                        WidgetArtwork(
                            artPath = state.artPath,
                            context = context,
                            contentDescription = context.getString(R.string.album_cover_desc),
                            targetSize = artSize,
                            cornerRadius = 999.dp,
                            palette = palette,
                            modifier = GlanceModifier.fillMaxSize().cornerRadius(999.dp),
                        )
                    }

                    Spacer(GlanceModifier.width(12.dp))

                    Column(
                        modifier =
                            GlanceModifier
                                .defaultWeight()
                                .fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Red Hanko Seal Stamp
                            Box(
                                modifier =
                                    GlanceModifier
                                        .size(16.dp)
                                        .background(hankoRed)
                                        .cornerRadius(3.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "音",
                                    style =
                                        TextStyle(
                                            color = ColorProvider(Color.White),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                )
                            }
                            Spacer(GlanceModifier.width(6.dp))
                            Text(
                                text = "和音 • ARCHIVETUNE",
                                style =
                                    TextStyle(
                                        color = inkSecondary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                            )
                        }

                        Spacer(GlanceModifier.height(3.dp))

                        Text(
                            text = state.title,
                            style =
                                TextStyle(
                                    color = ensoInk,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                            maxLines = 1,
                        )

                        Text(
                            text = state.artist,
                            style =
                                TextStyle(
                                    color = inkSecondary,
                                    fontSize = 11.sp,
                                ),
                            maxLines = 1,
                        )

                        Spacer(GlanceModifier.height(4.dp))

                        if (state.isAvailable && state.playbackPosition > 0f) {
                            LinearProgressIndicator(
                                progress = state.playbackPosition,
                                color = hankoRed,
                                backgroundColor = bambooLine,
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
                                    backgroundColor = ColorProvider(Color.Transparent),
                                    contentColor = ensoInk,
                                    cornerRadius = 999.dp,
                                    iconSize = 16.dp,
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
                                    backgroundColor = ensoInk,
                                    contentColor = ColorProvider(Color.White),
                                    cornerRadius = 999.dp,
                                    iconSize = 18.dp,
                                )
                                Spacer(GlanceModifier.width(6.dp))
                                WidgetControlButton(
                                    modifier = GlanceModifier.size(28.dp),
                                    action = skipNextAction(),
                                    icon = R.drawable.skip_next,
                                    contentDescription = context.getString(R.string.next),
                                    backgroundColor = ColorProvider(Color.Transparent),
                                    contentColor = ensoInk,
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

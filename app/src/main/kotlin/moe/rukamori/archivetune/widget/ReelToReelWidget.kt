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

class ReelToReelWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            ReelToReelContent(context)
        }
    }
}

@Composable
private fun ReelToReelContent(context: Context) {
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

        // Studio Master Reel-to-Reel console palette
        val consoleBg = ColorProvider(Color(0xFF1B1D22))
        val reelSilver = ColorProvider(Color(0xFFCBD2DA))
        val reelInner = ColorProvider(Color(0xFF8B94A0))
        val magneticTape = ColorProvider(Color(0xFF7A4A38))
        val tapeHeadBg = ColorProvider(Color(0xFF121316))
        val accentRed = ColorProvider(Color(0xFFE53935))
        val textPrimary = ColorProvider(Color(0xFFF0F2F6))
        val textSecondary = ColorProvider(Color(0xFF8E95A2))

        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(consoleBg)
                    .cornerRadius(22.dp)
                    .padding(if (compact) 4.dp else 6.dp)
                    .clickable(openArchiveTuneAction(context)),
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Dual Precision Aluminum Reels Assembly
                val reelSize = if (compact) 44.dp else 52.dp
                Box(
                    modifier =
                        GlanceModifier
                            .width(reelSize * 2 + 10.dp)
                            .height(reelSize + 10.dp)
                            .background(tapeHeadBg)
                            .cornerRadius(12.dp)
                            .padding(4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Left Reel (Supply)
                        Box(
                            modifier =
                                GlanceModifier
                                    .size(reelSize)
                                    .background(reelSilver)
                                    .cornerRadius(999.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier =
                                    GlanceModifier
                                        .size(reelSize * 0.45f)
                                        .background(reelInner)
                                        .cornerRadius(999.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier =
                                        GlanceModifier
                                            .size(6.dp)
                                            .background(tapeHeadBg)
                                            .cornerRadius(999.dp),
                                ) {}
                            }
                        }

                        // Center Magnetic Tape Span
                        Box(
                            modifier =
                                GlanceModifier
                                    .defaultWeight()
                                    .height(3.dp)
                                    .background(magneticTape),
                        ) {}

                        // Right Reel (Takeup) with Artwork Label
                        Box(
                            modifier =
                                GlanceModifier
                                    .size(reelSize)
                                    .background(reelSilver)
                                    .cornerRadius(999.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            WidgetArtwork(
                                artPath = state.artPath,
                                context = context,
                                contentDescription = context.getString(R.string.album_cover_desc),
                                targetSize = reelSize * 0.7f,
                                cornerRadius = 999.dp,
                                palette = palette,
                                modifier = GlanceModifier.size(reelSize * 0.7f).cornerRadius(999.dp),
                            )
                        }
                    }
                }

                Spacer(GlanceModifier.width(if (compact) 10.dp else 14.dp))

                // Right Section: Master Tape Telemetry, Track Info, and Controls
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
                            text = "MASTER TAPE • 15 IPS",
                            style =
                                TextStyle(
                                    color = accentRed,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                        Spacer(GlanceModifier.defaultWeight())
                        Text(
                            text = if (state.isPlaying) "REC ●" else "IDLE",
                            style =
                                TextStyle(
                                    color = if (state.isPlaying) accentRed else textSecondary,
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

                    // Magnetic Tape Roll Progress
                    if (state.isAvailable && state.playbackPosition > 0f) {
                        LinearProgressIndicator(
                            progress = state.playbackPosition,
                            color = magneticTape,
                            backgroundColor = ColorProvider(Color(0xFF2C2F36)),
                            modifier =
                                GlanceModifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .cornerRadius(2.dp),
                        )
                        Spacer(GlanceModifier.height(6.dp))
                    }

                    // Studio Heavy-Duty Toggles
                    if (state.isAvailable) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 30.dp else 34.dp),
                                action = skipPreviousAction(),
                                icon = R.drawable.skip_previous,
                                contentDescription = context.getString(R.string.widget_previous),
                                backgroundColor = ColorProvider(Color(0xFF282B32)),
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
                                backgroundColor = accentRed,
                                contentColor = ColorProvider(Color.White),
                                cornerRadius = 8.dp,
                                iconSize = 18.dp,
                            )
                            Spacer(GlanceModifier.width(8.dp))
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 30.dp else 34.dp),
                                action = skipNextAction(),
                                icon = R.drawable.skip_next,
                                contentDescription = context.getString(R.string.next),
                                backgroundColor = ColorProvider(Color(0xFF282B32)),
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

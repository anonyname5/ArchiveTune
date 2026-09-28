/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
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

class AppleLiquidPanoWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            AppleLiquidPanoContent(context)
        }
    }
}

@Composable
private fun AppleLiquidPanoContent(context: Context) {
    val prefs = currentState<Preferences>()
    val state = prefs.toWidgetPlaybackState(context)

    val dominant = state.dominantColor?.let { Color(it) }
    val glassBaseDark = Color(0x6012131D)
    val liquidAuraColor = remember(dominant) { dominant?.copy(alpha = 0.24f) ?: Color.Transparent }
    val liquidLensBg = remember(dominant) {
        dominant?.let {
            val r = (it.red * 0.45f + 1f * 0.55f).coerceIn(0f, 1f)
            val g = (it.green * 0.45f + 1f * 0.55f).coerceIn(0f, 1f)
            val b = (it.blue * 0.45f + 1f * 0.55f).coerceIn(0f, 1f)
            Color(red = r, green = g, blue = b, alpha = 0.40f)
        } ?: Color(0x35FFFFFF)
    }

    val textPrimary = ColorProvider(Color.White)
    val textSecondary = ColorProvider(Color(0xD0FFFFFF))
    val textTertiary = ColorProvider(Color(0x90FFFFFF))
    val scrubberTrack = ColorProvider(Color(0x28FFFFFF))
    val scrubberFill = ColorProvider(Color.White)

    val elapsedSec = if (state.durationMs > 0L) (state.positionMs / 1000L).toInt() else 0
    val totalSec = if (state.durationMs > 0L) (state.durationMs / 1000L).toInt() else 0
    val remainingSec = (totalSec - elapsedSec).coerceAtLeast(0)
    val elapsedStr = if (state.isAvailable && state.durationMs > 0L) "${elapsedSec / 60}:${(elapsedSec % 60).toString().padStart(2, '0')}" else "0:00"
    val remainingStr = if (state.isAvailable && totalSec > 0) "-${remainingSec / 60}:${(remainingSec % 60).toString().padStart(2, '0')}" else "-0:00"

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0x60FFFFFF))
            .cornerRadius(24.dp)
            .padding(1.2.dp)
            .clickable(openArchiveTuneAction(context)),
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0x30000000))
                .cornerRadius(23.dp)
                .padding(0.8.dp),
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(glassBaseDark)
                    .cornerRadius(22.dp),
            ) {
                if (state.dominantColor != null) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .background(liquidAuraColor)
                            .cornerRadius(22.dp),
                    ) {}
                }

                Row(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Left: Artwork Frame
                    Box(
                        modifier = GlanceModifier
                            .size(86.dp)
                            .background(Color(0x40FFFFFF))
                            .cornerRadius(18.dp)
                            .padding(1.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        WidgetAlbumArt(
                            artPath = prefs[MusicWidgetKeys.ART_PATH],
                            modifier = GlanceModifier
                                .fillMaxSize()
                                .cornerRadius(17.dp),
                            contentDescription = state.title,
                            targetSize = 130.dp,
                        )
                    }

                    Spacer(GlanceModifier.width(12.dp))

                    // Middle: Track Details & Live Scrubber
                    Column(
                        modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Lossless badge
                        Box(
                            modifier = GlanceModifier
                                .background(Color(0x30FFFFFF))
                                .cornerRadius(6.dp)
                                .padding(horizontal = 5.dp, vertical = 1.5.dp),
                        ) {
                            Text(
                                text = "LOSSLESS",
                                style = TextStyle(
                                    color = textTertiary,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                        }

                        Spacer(GlanceModifier.height(3.dp))

                        Text(
                            text = if (state.isAvailable) state.title else context.getString(R.string.app_name),
                            maxLines = 1,
                            style = TextStyle(
                                color = textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )

                        Text(
                            text = if (state.isAvailable) state.artist else context.getString(R.string.no_track_playing),
                            maxLines = 1,
                            style = TextStyle(
                                color = textSecondary,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Normal,
                            ),
                        )

                        Spacer(GlanceModifier.height(5.dp))

                        LinearProgressIndicator(
                            progress = if (state.isAvailable) state.playbackPosition else 0f,
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .cornerRadius(1.5.dp),
                            color = scrubberFill,
                            backgroundColor = scrubberTrack,
                        )

                        Spacer(GlanceModifier.height(2.dp))

                        Row(modifier = GlanceModifier.fillMaxWidth()) {
                            Text(
                                text = elapsedStr,
                                style = TextStyle(color = textTertiary, fontSize = 8.sp),
                            )
                            Spacer(GlanceModifier.defaultWeight())
                            Text(
                                text = remainingStr,
                                style = TextStyle(color = textTertiary, fontSize = 8.sp),
                            )
                        }
                    }

                    Spacer(GlanceModifier.width(10.dp))

                    // Right: Floating Control Capsule
                    Box(
                        modifier = GlanceModifier
                            .background(Color(0x35FFFFFF))
                            .cornerRadius(22.dp)
                            .padding(horizontal = 6.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = GlanceModifier.size(30.dp).clickable(skipPreviousAction()),
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_apple_backward),
                                    contentDescription = "Previous",
                                    colorFilter = ColorFilter.tint(textPrimary),
                                    modifier = GlanceModifier.size(16.dp),
                                )
                            }

                            Spacer(GlanceModifier.width(4.dp))

                            Box(
                                modifier = GlanceModifier
                                    .size(36.dp)
                                    .background(Color(0x60FFFFFF))
                                    .cornerRadius(18.dp)
                                    .padding(0.8.dp)
                                    .clickable(playPauseAction()),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = GlanceModifier
                                        .fillMaxSize()
                                        .background(liquidLensBg)
                                        .cornerRadius(17.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Image(
                                        provider = ImageProvider(
                                            if (state.isPlaying) R.drawable.ic_apple_pause else R.drawable.ic_apple_play,
                                        ),
                                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                                        colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                        modifier = GlanceModifier.size(18.dp),
                                    )
                                }
                            }

                            Spacer(GlanceModifier.width(4.dp))

                            Box(
                                modifier = GlanceModifier.size(30.dp).clickable(skipNextAction()),
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_apple_forward),
                                    contentDescription = "Next",
                                    colorFilter = ColorFilter.tint(textPrimary),
                                    modifier = GlanceModifier.size(16.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

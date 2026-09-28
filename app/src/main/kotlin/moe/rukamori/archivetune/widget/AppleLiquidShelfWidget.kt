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

class AppleLiquidShelfWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            AppleLiquidShelfContent(context)
        }
    }
}

@Composable
private fun AppleLiquidShelfContent(context: Context) {
    val prefs = currentState<Preferences>()
    val state = prefs.toWidgetPlaybackState(context)

    val dominant = state.dominantColor?.let { Color(it) }
    val glassBaseDark = Color(0x6012131D)
    val liquidAuraColor = remember(dominant) { dominant?.copy(alpha = 0.28f) ?: Color.Transparent }
    val liquidLensBg = remember(dominant) {
        dominant?.let {
            val r = (it.red * 0.45f + 1f * 0.55f).coerceIn(0f, 1f)
            val g = (it.green * 0.45f + 1f * 0.55f).coerceIn(0f, 1f)
            val b = (it.blue * 0.45f + 1f * 0.55f).coerceIn(0f, 1f)
            Color(red = r, green = g, blue = b, alpha = 0.42f)
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
            .cornerRadius(30.dp)
            .padding(1.2.dp)
            .clickable(openArchiveTuneAction(context)),
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0x30000000))
                .cornerRadius(29.dp)
                .padding(0.8.dp),
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(glassBaseDark)
                    .cornerRadius(28.dp),
            ) {
                if (state.dominantColor != null) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .background(liquidAuraColor)
                            .cornerRadius(28.dp),
                    ) {}
                }

                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(12.dp),
                ) {
                    // Top: Massive Artwork Stage
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .defaultWeight()
                            .background(Color(0x30FFFFFF))
                            .cornerRadius(20.dp)
                            .padding(1.dp),
                    ) {
                        WidgetAlbumArt(
                            artPath = prefs[MusicWidgetKeys.ART_PATH],
                            modifier = GlanceModifier
                                .fillMaxSize()
                                .cornerRadius(19.dp),
                            contentDescription = state.title,
                            targetSize = 260.dp,
                        )

                        // Floating Badges on Artwork
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = GlanceModifier
                                    .background(Color(0x60000000))
                                    .cornerRadius(6.dp)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = "LOSSLESS",
                                    style = TextStyle(
                                        color = textTertiary,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                )
                            }

                            Spacer(GlanceModifier.defaultWeight())

                            Row(
                                modifier = GlanceModifier
                                    .background(Color(0x60000000))
                                    .cornerRadius(10.dp)
                                    .padding(horizontal = 7.dp, vertical = 2.5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_apple_airplay),
                                    contentDescription = "AirPlay",
                                    colorFilter = ColorFilter.tint(ColorProvider(Color(0xFF38A3FF))),
                                    modifier = GlanceModifier.size(10.dp),
                                )
                                Spacer(GlanceModifier.width(3.dp))
                                Text(
                                    text = "AirPlay",
                                    style = TextStyle(
                                        color = ColorProvider(Color.White),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                )
                            }
                        }
                    }

                    Spacer(GlanceModifier.height(10.dp))

                    // Bottom: Crystalline Glass Control Shelf
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .background(Color(0x35FFFFFF))
                            .cornerRadius(20.dp)
                            .padding(12.dp),
                    ) {
                        Column(modifier = GlanceModifier.fillMaxWidth()) {
                            // Title & Artist
                            Text(
                                text = if (state.isAvailable) state.title else context.getString(R.string.app_name),
                                maxLines = 1,
                                style = TextStyle(
                                    color = textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                            Text(
                                text = if (state.isAvailable) state.artist else context.getString(R.string.no_track_playing),
                                maxLines = 1,
                                style = TextStyle(
                                    color = textSecondary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Normal,
                                ),
                            )

                            Spacer(GlanceModifier.height(8.dp))

                            // Scrubber
                            LinearProgressIndicator(
                                progress = if (state.isAvailable) state.playbackPosition else 0f,
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .height(3.5.dp)
                                    .cornerRadius(2.dp),
                                color = scrubberFill,
                                backgroundColor = scrubberTrack,
                            )

                            Spacer(GlanceModifier.height(2.dp))

                            Row(modifier = GlanceModifier.fillMaxWidth()) {
                                Text(
                                    text = elapsedStr,
                                    style = TextStyle(color = textTertiary, fontSize = 8.5.sp),
                                )
                                Spacer(GlanceModifier.defaultWeight())
                                Text(
                                    text = remainingStr,
                                    style = TextStyle(color = textTertiary, fontSize = 8.5.sp),
                                )
                            }

                            Spacer(GlanceModifier.height(8.dp))

                            // Full 5-element Control Row
                            Row(
                                modifier = GlanceModifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = GlanceModifier.size(32.dp).clickable(volumeDownAction()),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_apple_volume_min),
                                        contentDescription = "Volume Down",
                                        colorFilter = ColorFilter.tint(textSecondary),
                                        modifier = GlanceModifier.size(16.dp),
                                    )
                                }

                                Spacer(GlanceModifier.defaultWeight())

                                Box(
                                    modifier = GlanceModifier.size(36.dp).clickable(skipPreviousAction()),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_apple_backward),
                                        contentDescription = "Previous",
                                        colorFilter = ColorFilter.tint(textPrimary),
                                        modifier = GlanceModifier.size(20.dp),
                                    )
                                }

                                Spacer(GlanceModifier.width(12.dp))

                                Box(
                                    modifier = GlanceModifier
                                        .size(46.dp)
                                        .background(Color(0x60FFFFFF))
                                        .cornerRadius(23.dp)
                                        .padding(1.dp)
                                        .clickable(playPauseAction()),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Box(
                                        modifier = GlanceModifier
                                            .fillMaxSize()
                                            .background(liquidLensBg)
                                            .cornerRadius(22.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Image(
                                            provider = ImageProvider(
                                                if (state.isPlaying) R.drawable.ic_apple_pause else R.drawable.ic_apple_play,
                                            ),
                                            contentDescription = if (state.isPlaying) "Pause" else "Play",
                                            colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                            modifier = GlanceModifier.size(22.dp),
                                        )
                                    }
                                }

                                Spacer(GlanceModifier.width(12.dp))

                                Box(
                                    modifier = GlanceModifier.size(36.dp).clickable(skipNextAction()),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_apple_forward),
                                        contentDescription = "Next",
                                        colorFilter = ColorFilter.tint(textPrimary),
                                        modifier = GlanceModifier.size(20.dp),
                                    )
                                }

                                Spacer(GlanceModifier.defaultWeight())

                                Box(
                                    modifier = GlanceModifier.size(32.dp).clickable(volumeUpAction()),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_apple_volume_max),
                                        contentDescription = "Volume Up",
                                        colorFilter = ColorFilter.tint(textSecondary),
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
}

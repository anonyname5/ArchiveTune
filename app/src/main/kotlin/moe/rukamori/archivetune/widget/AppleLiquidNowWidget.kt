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

class AppleLiquidNowWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            AppleLiquidNowContent(context)
        }
    }
}

@Composable
private fun AppleLiquidNowContent(context: Context) {
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
                    // Left: Artwork Box
                    Box(
                        modifier = GlanceModifier
                            .size(80.dp)
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
                            targetSize = 120.dp,
                        )
                    }

                    Spacer(GlanceModifier.width(10.dp))

                    // Right: Information & Control Deck
                    Column(
                        modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Tag
                        Box(
                            modifier = GlanceModifier
                                .background(Color(0x30FFFFFF))
                                .cornerRadius(6.dp)
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                        ) {
                            Text(
                                text = "NOW PLAYING",
                                style = TextStyle(
                                    color = textTertiary,
                                    fontSize = 7.sp,
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
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )

                        Text(
                            text = if (state.isAvailable) state.artist else context.getString(R.string.no_track_playing),
                            maxLines = 1,
                            style = TextStyle(
                                color = textSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal,
                            ),
                        )

                        Spacer(GlanceModifier.height(6.dp))

                        // Controls
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = GlanceModifier.size(28.dp).clickable(skipPreviousAction()),
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_apple_backward),
                                    contentDescription = "Previous",
                                    colorFilter = ColorFilter.tint(textPrimary),
                                    modifier = GlanceModifier.size(15.dp),
                                )
                            }

                            Spacer(GlanceModifier.width(8.dp))

                            Box(
                                modifier = GlanceModifier
                                    .size(34.dp)
                                    .background(Color(0x60FFFFFF))
                                    .cornerRadius(17.dp)
                                    .padding(0.8.dp)
                                    .clickable(playPauseAction()),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = GlanceModifier
                                        .fillMaxSize()
                                        .background(liquidLensBg)
                                        .cornerRadius(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Image(
                                        provider = ImageProvider(
                                            if (state.isPlaying) R.drawable.ic_apple_pause else R.drawable.ic_apple_play,
                                        ),
                                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                                        colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                        modifier = GlanceModifier.size(17.dp),
                                    )
                                }
                            }

                            Spacer(GlanceModifier.width(8.dp))

                            Box(
                                modifier = GlanceModifier.size(28.dp).clickable(skipNextAction()),
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_apple_forward),
                                    contentDescription = "Next",
                                    colorFilter = ColorFilter.tint(textPrimary),
                                    modifier = GlanceModifier.size(15.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

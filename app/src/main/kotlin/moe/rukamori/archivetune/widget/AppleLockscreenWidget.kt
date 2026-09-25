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
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
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

class AppleLockscreenWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            AppleLockscreenContent(context)
        }
    }
}

@Composable
private fun AppleLockscreenContent(context: Context) {
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
        val compact = size.width < 280.dp || size.height < 120.dp
        val showVolume = size.height >= 140.dp && size.width >= 240.dp

        // Apple Liquid Glass Palette
        val dominant = state.dominantColor?.let { Color(it) }

        // Liquid glass fluid base: translucent obsidian crystal infused with artwork dye
        val liquidGlassBg =
            remember(dominant) {
                dominant?.let {
                    val tinted = it.blendWith(Color(0xFF12131A), 0.78f)
                    tinted.copy(alpha = 0.82f)
                } ?: Color(0xD8181922)
            }

        // Crystalline liquid play/pause lens highlight
        val liquidLensBg =
            remember(dominant) {
                dominant?.let {
                    it.blendWith(Color(0xFFFFFFFF), 0.28f).copy(alpha = 0.36f)
                } ?: Color(0x38FFFFFF)
            }

        val textPrimary = ColorProvider(Color.White)
        val textSecondary = ColorProvider(Color(0xFFB0B0B8)) // iOS Liquid Secondary Label
        val textTertiary = ColorProvider(Color(0x94FFFFFF)) // iOS Monospace Timestamp
        val airplayBlue = ColorProvider(Color(0xFF0A84FF)) // iOS Electric Blue
        val airplayText = ColorProvider(Color(0xEEFFFFFF))
        val scrubberTrack = ColorProvider(Color(0x2EFFFFFF))
        val scrubberFill = ColorProvider(Color.White)

        // Estimated position & remaining countdown
        val elapsedSec = (state.playbackPosition * 210).toInt()
        val remainingSec = (210 - elapsedSec).coerceAtLeast(0)
        val elapsedStr = "${elapsedSec / 60}:${(elapsedSec % 60).toString().padStart(2, '0')}"
        val remainingStr = "-${remainingSec / 60}:${(remainingSec % 60).toString().padStart(2, '0')}"

        // Outer Crystalline Specular Rim (Refracts light along outer bevel)
        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(Color(0x40FFFFFF))
                    .cornerRadius(26.dp)
                    .padding(1.dp)
                    .clickable(openArchiveTuneAction(context)),
        ) {
            // Liquid Glass Body Container
            Box(
                modifier =
                    GlanceModifier
                        .fillMaxSize()
                        .background(liquidGlassBg)
                        .cornerRadius(25.dp)
                        .padding(if (compact) 9.dp else 13.dp),
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    // Top Specular Gleam Line (Apple visionOS / liquid glass light reflection)
                    Box(
                        modifier =
                            GlanceModifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0x48FFFFFF))
                                .cornerRadius(1.dp),
                    ) {}

                    Spacer(GlanceModifier.height(if (compact) 5.dp else 7.dp))

                    // 1. Top Section: Acrylic Recessed Artwork, Metadata, and AirPlay Glass Capsule
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val artSize = if (compact) 44.dp else 52.dp

                        // Artwork encased in a polished acrylic crystal pocket
                        Box(
                            modifier =
                                GlanceModifier
                                    .size(artSize + 4.dp)
                                    .background(Color(0x38FFFFFF))
                                    .cornerRadius(14.dp)
                                    .padding(1.dp),
                        ) {
                            Box(
                                modifier =
                                    GlanceModifier
                                        .fillMaxSize()
                                        .background(Color(0x60000000))
                                        .cornerRadius(13.dp)
                                        .padding(1.dp),
                            ) {
                                WidgetArtwork(
                                    artPath = state.artPath,
                                    context = context,
                                    contentDescription = context.getString(R.string.album_cover_desc),
                                    targetSize = artSize,
                                    cornerRadius = 12.dp,
                                    palette = palette,
                                    modifier = GlanceModifier.fillMaxSize(),
                                )
                            }
                        }

                        Spacer(GlanceModifier.width(if (compact) 10.dp else 12.dp))

                        // Track Title and Artist with Apple SF styling
                        Column(
                            modifier = GlanceModifier.defaultWeight(),
                            verticalAlignment = Alignment.Vertical.CenterVertically,
                        ) {
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

                            Spacer(GlanceModifier.height(1.dp))

                            Text(
                                text = state.artist,
                                style =
                                    TextStyle(
                                        color = textSecondary,
                                        fontSize = if (compact) 11.sp else 12.sp,
                                        fontWeight = FontWeight.Medium,
                                    ),
                                maxLines = 1,
                            )
                        }

                        Spacer(GlanceModifier.width(8.dp))

                        // Liquid Glass AirPlay Destination Pill
                        Box(
                            modifier =
                                GlanceModifier
                                    .background(Color(0x36FFFFFF))
                                    .cornerRadius(13.dp)
                                    .padding(1.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier =
                                    GlanceModifier
                                        .background(Color(0x28FFFFFF))
                                        .cornerRadius(12.dp)
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_apple_airplay),
                                        contentDescription = "AirPlay",
                                        colorFilter = ColorFilter.tint(airplayBlue),
                                        modifier = GlanceModifier.size(12.dp),
                                    )
                                    Spacer(GlanceModifier.width(4.dp))
                                    Text(
                                        text = "AirPlay",
                                        style =
                                            TextStyle(
                                                color = airplayText,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(GlanceModifier.height(if (compact) 6.dp else 8.dp))

                    // 2. Liquid Glass Tube Scrubber & Monospace Timestamps
                    Box(
                        modifier =
                            GlanceModifier
                                .fillMaxWidth()
                                .height(if (compact) 3.5.dp else 4.dp)
                                .background(Color(0x24FFFFFF))
                                .cornerRadius(2.dp),
                    ) {
                        LinearProgressIndicator(
                            progress = if (state.isAvailable) state.playbackPosition else 0f,
                            modifier = GlanceModifier.fillMaxSize().cornerRadius(2.dp),
                            color = scrubberFill,
                            backgroundColor = scrubberTrack,
                        )
                    }

                    Spacer(GlanceModifier.height(3.dp))

                    Row(
                        modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 1.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (state.isAvailable) elapsedStr else "0:00",
                            style =
                                TextStyle(
                                    color = textTertiary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Normal,
                                ),
                        )
                        Spacer(GlanceModifier.defaultWeight())
                        Text(
                            text = if (state.isAvailable) remainingStr else "-0:00",
                            style =
                                TextStyle(
                                    color = textTertiary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Normal,
                                ),
                        )
                    }

                    Spacer(GlanceModifier.height(if (compact) 4.dp else 6.dp))

                    // 3. Apple Liquid Glass Media Controls (Glass Pebble Buttons)
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Previous Track Glass Pebble
                        Box(
                            modifier =
                                GlanceModifier
                                    .size(if (compact) 36.dp else 40.dp)
                                    .background(Color(0x30FFFFFF))
                                    .cornerRadius(20.dp)
                                    .padding(1.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier =
                                    GlanceModifier
                                        .fillMaxSize()
                                        .background(Color(0x1CFFFFFF))
                                        .cornerRadius(19.dp)
                                        .clickable(skipPreviousAction()),
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.skip_previous),
                                    contentDescription = context.getString(R.string.widget_previous),
                                    colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                    modifier = GlanceModifier.size(if (compact) 18.dp else 20.dp),
                                )
                            }
                        }

                        Spacer(GlanceModifier.width(if (compact) 22.dp else 30.dp))

                        // Prominent Liquid Glass Bubble Lens (Play / Pause)
                        Box(
                            modifier =
                                GlanceModifier
                                    .size(if (compact) 42.dp else 48.dp)
                                    .background(Color(0x54FFFFFF))
                                    .cornerRadius(if (compact) 21.dp else 24.dp)
                                    .padding(1.2.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier =
                                    GlanceModifier
                                        .fillMaxSize()
                                        .background(liquidLensBg)
                                        .cornerRadius(if (compact) 20.dp else 23.dp)
                                        .clickable(playPauseAction()),
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    provider =
                                        ImageProvider(
                                            if (state.isPlaying) R.drawable.pause else R.drawable.play,
                                        ),
                                    contentDescription =
                                        context.getString(
                                            if (state.isPlaying) R.string.widget_pause else R.string.play,
                                        ),
                                    colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                    modifier = GlanceModifier.size(if (compact) 22.dp else 25.dp),
                                )
                            }
                        }

                        Spacer(GlanceModifier.width(if (compact) 22.dp else 30.dp))

                        // Next Track Glass Pebble
                        Box(
                            modifier =
                                GlanceModifier
                                    .size(if (compact) 36.dp else 40.dp)
                                    .background(Color(0x30FFFFFF))
                                    .cornerRadius(20.dp)
                                    .padding(1.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier =
                                    GlanceModifier
                                        .fillMaxSize()
                                        .background(Color(0x1CFFFFFF))
                                        .cornerRadius(19.dp)
                                        .clickable(skipNextAction()),
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.skip_next),
                                    contentDescription = context.getString(R.string.next),
                                    colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                    modifier = GlanceModifier.size(if (compact) 18.dp else 20.dp),
                                )
                            }
                        }
                    }

                    // 4. Liquid Glass Volume Rail (shown when height >= 140.dp)
                    if (showVolume) {
                        Spacer(GlanceModifier.height(8.dp))

                        Row(
                            modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.volume_off),
                                contentDescription = "Volume Min",
                                colorFilter = ColorFilter.tint(textTertiary),
                                modifier = GlanceModifier.size(11.dp),
                            )

                            Spacer(GlanceModifier.width(8.dp))

                            // Liquid Fluid Volume Channel
                            Box(
                                modifier =
                                    GlanceModifier
                                        .defaultWeight()
                                        .height(3.5.dp)
                                        .background(Color(0x25FFFFFF))
                                        .cornerRadius(2.dp)
                                        .padding(0.5.dp),
                            ) {
                                Row(
                                    modifier = GlanceModifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier =
                                            GlanceModifier
                                                .defaultWeight()
                                                .fillMaxHeight()
                                                .background(Color(0x75FFFFFF))
                                                .cornerRadius(1.5.dp),
                                    ) {}
                                    Spacer(GlanceModifier.width(2.dp))
                                    Box(
                                        modifier =
                                            GlanceModifier
                                                .width(36.dp)
                                                .fillMaxHeight()
                                                .background(Color(0x18FFFFFF))
                                                .cornerRadius(1.5.dp),
                                    ) {}
                                }
                            }

                            Spacer(GlanceModifier.width(8.dp))

                            Image(
                                provider = ImageProvider(R.drawable.volume_up),
                                contentDescription = "Volume Max",
                                colorFilter = ColorFilter.tint(textTertiary),
                                modifier = GlanceModifier.size(11.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

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
    val palette = rememberWidgetPalette(state.dominantColor)

    val size = LocalSize.current
    val isRibbonMode = size.height < 112.dp
    val isCompact = size.width < 270.dp || size.height < 135.dp
    val showVolume = size.height >= 142.dp && size.width >= 240.dp

    // Apple Pure Liquid Glass Palette (Decoupled from Android system Material You muddying)
    val dominant = state.dominantColor?.let { Color(it) }

    // 1. Crystal Smoked Obsidian Glass Base
    val glassBaseDark = Color(0x5813141D)

    // 2. Dynamic Liquid Artwork Aura
    val liquidAuraColor =
        remember(dominant) {
            dominant?.copy(alpha = 0.20f) ?: Color.Transparent
        }

    // 3. Floating Liquid Lens Highlight (Play/Pause glass bubble)
    val liquidLensBg =
        remember(dominant) {
            dominant?.let {
                it.blendWith(Color.White, 0.40f).copy(alpha = 0.38f)
            } ?: Color(0x3DFFFFFF)
        }

    // High-contrast Apple Glass Typography & Controls
    val textPrimary = ColorProvider(Color.White)
    val textSecondary = ColorProvider(Color(0xD0FFFFFF))
    val textTertiary = ColorProvider(Color(0x95FFFFFF))
    val airplayBlue = ColorProvider(Color(0xFF38A3FF))
    val airplayText = ColorProvider(Color.White)
    val scrubberTrack = ColorProvider(Color(0x30FFFFFF))
    val scrubberFill = ColorProvider(Color.White)

    // Position & remaining countdown calculation
    val elapsedSec = (state.playbackPosition * 210).toInt()
    val remainingSec = (210 - elapsedSec).coerceAtLeast(0)
    val elapsedStr = "${elapsedSec / 60}:${(elapsedSec % 60).toString().padStart(2, '0')}"
    val remainingStr = "-${remainingSec / 60}:${(remainingSec % 60).toString().padStart(2, '0')}"

    // Layer 1: Outer Crystalline Specular Glass Rim (Apple 26dp squircle)
    Box(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .background(Color(0x65FFFFFF))
                .cornerRadius(26.dp)
                .padding(1.2.dp)
                .clickable(openArchiveTuneAction(context)),
    ) {
        // Layer 2: Inner Refraction Depth Shadow (3D glass thickness)
        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(Color(0x30000000))
                    .cornerRadius(25.dp)
                    .padding(0.8.dp),
        ) {
            // Layer 3: Highly Translucent Smoked Crystal Acrylic Base
            Box(
                modifier =
                    GlanceModifier
                        .fillMaxSize()
                        .background(glassBaseDark)
                        .cornerRadius(24.dp),
            ) {
                // Layer 4: Dynamic Liquid Artwork Dye Layer
                if (state.dominantColor != null) {
                    Box(
                        modifier =
                            GlanceModifier
                                .fillMaxSize()
                                .background(liquidAuraColor)
                                .cornerRadius(24.dp),
                    ) {}
                }

                // Layer 5: Glass Content & Controls
                Column(
                    modifier =
                        GlanceModifier
                            .fillMaxSize()
                            .padding(horizontal = 7.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    // Top Specular Gleam Line (Apple visionOS / liquid glass light reflection)
                    Box(
                        modifier =
                            GlanceModifier
                                .fillMaxWidth()
                                .height(1.2.dp)
                                .background(Color(0x80FFFFFF))
                                .cornerRadius(1.dp),
                    ) {}

                    Spacer(GlanceModifier.height(if (isRibbonMode) 4.dp else 6.dp))

                    if (isRibbonMode) {
                        // ─────────────────────────────────────────────────────────────
                        // COMPACT RIBBON MODE (Apple Live Activity / Dynamic Island Style)
                        // ─────────────────────────────────────────────────────────────
                        Row(
                            modifier = GlanceModifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Squircle Artwork
                            Box(
                                modifier =
                                    GlanceModifier
                                        .size(44.dp)
                                        .background(Color(0x50FFFFFF))
                                        .cornerRadius(12.dp)
                                        .padding(1.dp),
                            ) {
                                Box(
                                    modifier =
                                        GlanceModifier
                                            .fillMaxSize()
                                            .background(Color(0x50000000))
                                            .cornerRadius(11.dp)
                                            .padding(0.8.dp),
                                ) {
                                    WidgetArtwork(
                                        artPath = state.artPath,
                                        context = context,
                                        contentDescription = context.getString(R.string.album_cover_desc),
                                        targetSize = 42.dp,
                                        cornerRadius = 10.dp,
                                        palette = palette,
                                        modifier = GlanceModifier.fillMaxSize(),
                                    )
                                }
                            }

                            Spacer(GlanceModifier.width(8.dp))

                            // Metadata & Micro Scrubber
                            Column(
                                modifier = GlanceModifier.defaultWeight(),
                                verticalAlignment = Alignment.Vertical.CenterVertically,
                            ) {
                                Text(
                                    text = state.title,
                                    style =
                                        TextStyle(
                                            color = textPrimary,
                                            fontSize = 13.sp,
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
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                        ),
                                    maxLines = 1,
                                )
                                Spacer(GlanceModifier.height(3.dp))
                                Box(
                                    modifier =
                                        GlanceModifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .background(Color(0x28FFFFFF))
                                            .cornerRadius(1.5.dp),
                                ) {
                                    LinearProgressIndicator(
                                        progress = if (state.isAvailable) state.playbackPosition else 0f,
                                        modifier = GlanceModifier.fillMaxSize().cornerRadius(1.5.dp),
                                        color = scrubberFill,
                                        backgroundColor = scrubberTrack,
                                    )
                                }
                            }

                            Spacer(GlanceModifier.width(8.dp))

                            // Apple SF Media Controls
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier =
                                        GlanceModifier
                                            .size(32.dp)
                                            .clickable(skipPreviousAction()),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_apple_backward),
                                        contentDescription = context.getString(R.string.widget_previous),
                                        colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                        modifier = GlanceModifier.size(18.dp),
                                    )
                                }

                                Spacer(GlanceModifier.width(4.dp))

                                Box(
                                    modifier =
                                        GlanceModifier
                                            .size(36.dp)
                                            .background(Color(0x80FFFFFF))
                                            .cornerRadius(18.dp)
                                            .padding(1.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Box(
                                        modifier =
                                            GlanceModifier
                                                .fillMaxSize()
                                                .background(liquidLensBg)
                                                .cornerRadius(17.dp)
                                                .clickable(playPauseAction()),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Image(
                                            provider =
                                                ImageProvider(
                                                    if (state.isPlaying) R.drawable.ic_apple_pause else R.drawable.ic_apple_play,
                                                ),
                                            contentDescription =
                                                context.getString(
                                                    if (state.isPlaying) R.string.widget_pause else R.string.play,
                                                ),
                                            colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                            modifier = GlanceModifier.size(20.dp),
                                        )
                                    }
                                }

                                Spacer(GlanceModifier.width(4.dp))

                                Box(
                                    modifier =
                                        GlanceModifier
                                            .size(32.dp)
                                            .clickable(skipNextAction()),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_apple_forward),
                                        contentDescription = context.getString(R.string.next),
                                        colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                        modifier = GlanceModifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    } else {
                        // ─────────────────────────────────────────────────────────────
                        // EXPANDED LOCK SCREEN PLAYER (Authentic iOS 17/18 Media Card)
                        // ─────────────────────────────────────────────────────────────
                        val artSize = if (isCompact) 48.dp else 56.dp

                        // 1. Header: Artwork, Title/Artist, and AirPlay Capsule Pill
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Apple Squircle Artwork
                            Box(
                                modifier =
                                    GlanceModifier
                                        .size(artSize + 2.dp)
                                        .background(Color(0x55FFFFFF))
                                        .cornerRadius(14.dp)
                                        .padding(1.dp),
                            ) {
                                Box(
                                    modifier =
                                        GlanceModifier
                                            .fillMaxSize()
                                            .background(Color(0x60000000))
                                            .cornerRadius(13.dp)
                                            .padding(0.8.dp),
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

                            Spacer(GlanceModifier.width(if (isCompact) 10.dp else 12.dp))

                            // Track Metadata
                            Column(
                                modifier = GlanceModifier.defaultWeight(),
                                verticalAlignment = Alignment.Vertical.CenterVertically,
                            ) {
                                Text(
                                    text = state.title,
                                    style =
                                        TextStyle(
                                            color = textPrimary,
                                            fontSize = if (isCompact) 14.sp else 16.sp,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                    maxLines = 1,
                                )

                                Spacer(GlanceModifier.height(1.5.dp))

                                Text(
                                    text = state.artist,
                                    style =
                                        TextStyle(
                                            color = textSecondary,
                                            fontSize = if (isCompact) 11.5.sp else 13.sp,
                                            fontWeight = FontWeight.Medium,
                                        ),
                                    maxLines = 1,
                                )
                            }

                            Spacer(GlanceModifier.width(8.dp))

                            // Apple AirPlay Destination Pill
                            Box(
                                modifier =
                                    GlanceModifier
                                        .background(Color(0x65FFFFFF))
                                        .cornerRadius(13.dp)
                                        .padding(1.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier =
                                        GlanceModifier
                                            .background(Color(0x30FFFFFF))
                                            .cornerRadius(12.dp)
                                            .padding(horizontal = 8.dp, vertical = 3.5.dp)
                                            .clickable(openArchiveTuneAction(context)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            provider = ImageProvider(R.drawable.ic_apple_airplay),
                                            contentDescription = "AirPlay",
                                            colorFilter = ColorFilter.tint(airplayBlue),
                                            modifier = GlanceModifier.size(13.dp),
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

                        Spacer(GlanceModifier.height(if (isCompact) 6.dp else 9.dp))

                        // 2. Precision Scrubber & Monospace Apple Timestamps
                        Column(modifier = GlanceModifier.fillMaxWidth()) {
                            Box(
                                modifier =
                                    GlanceModifier
                                        .fillMaxWidth()
                                        .height(if (isCompact) 3.5.dp else 4.dp)
                                        .background(Color(0x30FFFFFF))
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
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Normal,
                                        ),
                                )
                                Spacer(GlanceModifier.defaultWeight())
                                Text(
                                    text = if (state.isAvailable) remainingStr else "-0:00",
                                    style =
                                        TextStyle(
                                            color = textTertiary,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Normal,
                                        ),
                                )
                            }
                        }

                        Spacer(GlanceModifier.height(if (isCompact) 4.dp else 8.dp))

                        // 3. Apple SF Media Controls (Floating Chevrons + Frosted Glass Lens Play/Pause)
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Apple backward.fill
                            Box(
                                modifier =
                                    GlanceModifier
                                        .size(if (isCompact) 38.dp else 44.dp)
                                        .clickable(skipPreviousAction()),
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_apple_backward),
                                    contentDescription = context.getString(R.string.widget_previous),
                                    colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                    modifier = GlanceModifier.size(if (isCompact) 20.dp else 24.dp),
                                )
                            }

                            Spacer(GlanceModifier.width(if (isCompact) 24.dp else 36.dp))

                            // Apple play.fill / pause.fill in Frosted Glass Bubble Lens
                            Box(
                                modifier =
                                    GlanceModifier
                                        .size(if (isCompact) 44.dp else 52.dp)
                                        .background(Color(0x85FFFFFF))
                                        .cornerRadius(if (isCompact) 22.dp else 26.dp)
                                        .padding(1.2.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier =
                                        GlanceModifier
                                            .fillMaxSize()
                                            .background(liquidLensBg)
                                            .cornerRadius(if (isCompact) 21.dp else 25.dp)
                                            .clickable(playPauseAction()),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Image(
                                        provider =
                                            ImageProvider(
                                                if (state.isPlaying) R.drawable.ic_apple_pause else R.drawable.ic_apple_play,
                                            ),
                                        contentDescription =
                                            context.getString(
                                                if (state.isPlaying) R.string.widget_pause else R.string.play,
                                            ),
                                        colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                        modifier = GlanceModifier.size(if (isCompact) 22.dp else 26.dp),
                                    )
                                }
                            }

                            Spacer(GlanceModifier.width(if (isCompact) 24.dp else 36.dp))

                            // Apple forward.fill
                            Box(
                                modifier =
                                    GlanceModifier
                                        .size(if (isCompact) 38.dp else 44.dp)
                                        .clickable(skipNextAction()),
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_apple_forward),
                                    contentDescription = context.getString(R.string.next),
                                    colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                    modifier = GlanceModifier.size(if (isCompact) 20.dp else 24.dp),
                                )
                            }
                        }

                        // 4. Apple Lock Screen Volume Deck (Shown when height >= 142.dp)
                        if (showVolume) {
                            Spacer(GlanceModifier.height(if (isCompact) 6.dp else 9.dp))

                            Row(
                                modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_apple_volume_min),
                                    contentDescription = "Volume Min",
                                    colorFilter = ColorFilter.tint(textTertiary),
                                    modifier = GlanceModifier.size(11.dp),
                                )

                                Spacer(GlanceModifier.width(9.dp))

                                // Liquid Glass Volume Channel
                                Box(
                                    modifier =
                                        GlanceModifier
                                            .defaultWeight()
                                            .height(3.5.dp)
                                            .background(Color(0x28FFFFFF))
                                            .cornerRadius(2.dp),
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
                                                    .background(Color(0x80FFFFFF))
                                                    .cornerRadius(2.dp),
                                        ) {}
                                        Spacer(GlanceModifier.width(2.dp))
                                        Box(
                                            modifier =
                                                GlanceModifier
                                                    .width(38.dp)
                                                    .fillMaxHeight()
                                                    .background(Color(0x15FFFFFF))
                                                    .cornerRadius(2.dp),
                                        ) {}
                                    }
                                }

                                Spacer(GlanceModifier.width(9.dp))

                                Image(
                                    provider = ImageProvider(R.drawable.ic_apple_volume_max),
                                    contentDescription = "Volume Max",
                                    colorFilter = ColorFilter.tint(textTertiary),
                                    modifier = GlanceModifier.size(12.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

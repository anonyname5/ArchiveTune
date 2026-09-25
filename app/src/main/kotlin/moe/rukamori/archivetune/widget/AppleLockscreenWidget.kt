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

        // Apple iOS Frosted Dark Glassmorphic Palette
        val dominant = state.dominantColor?.let { Color(it) }
        val glassBg =
            remember(dominant) {
                dominant?.blendWith(Color(0xFF141416), 0.88f) ?: Color(0xEE1C1C1E)
            }

        val textPrimary = ColorProvider(Color.White)
        val textSecondary = ColorProvider(Color(0xFF98989F)) // iOS Secondary Label Color
        val textTertiary = ColorProvider(Color(0x8AFFFFFF)) // iOS Timestamp Caption Color
        val airplayPillBg = Color(0x2EFFFFFF)
        val airplayBlue = ColorProvider(Color(0xFF0A84FF)) // iOS System Blue Accent
        val airplayText = ColorProvider(Color(0xEEFFFFFF))
        val scrubberTrack = ColorProvider(Color(0x33FFFFFF))
        val scrubberFill = ColorProvider(Color.White)
        val playButtonBg = ColorProvider(Color(0x2EFFFFFF))

        // Estimated position/remaining calculation
        val elapsedSec = (state.playbackPosition * 210).toInt()
        val remainingSec = (210 - elapsedSec).coerceAtLeast(0)
        val elapsedStr = "${elapsedSec / 60}:${(elapsedSec % 60).toString().padStart(2, '0')}"
        val remainingStr = "-${remainingSec / 60}:${(remainingSec % 60).toString().padStart(2, '0')}"

        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(glassBg)
                    .cornerRadius(26.dp)
                    .padding(if (compact) 10.dp else 14.dp)
                    .clickable(openArchiveTuneAction(context)),
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                // 1. Top Section: Album Artwork, Track Metadata, and AirPlay Route Pill
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val artSize = if (compact) 44.dp else 52.dp
                    // Artwork with subtle iOS squircle border
                    Box(
                        modifier =
                            GlanceModifier
                                .size(artSize)
                                .background(Color(0x28FFFFFF))
                                .cornerRadius(12.dp)
                                .padding(1.dp),
                    ) {
                        WidgetArtwork(
                            artPath = state.artPath,
                            context = context,
                            contentDescription = context.getString(R.string.album_cover_desc),
                            targetSize = artSize,
                            cornerRadius = 11.dp,
                            palette = palette,
                            modifier = GlanceModifier.fillMaxSize(),
                        )
                    }

                    Spacer(GlanceModifier.width(if (compact) 10.dp else 12.dp))

                    // Track Title and Artist
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

                    // iOS AirPlay Destination Route Pill
                    Box(
                        modifier =
                            GlanceModifier
                                .background(airplayPillBg)
                                .cornerRadius(12.dp)
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_apple_airplay),
                                contentDescription = "AirPlay",
                                colorFilter = ColorFilter.tint(airplayBlue),
                                modifier = GlanceModifier.size(12.dp),
                            )
                            Spacer(GlanceModifier.width(3.dp))
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

                Spacer(GlanceModifier.height(if (compact) 6.dp else 8.dp))

                // 2. iOS Thin Scrubber Bar & Timestamps
                LinearProgressIndicator(
                    progress = if (state.isAvailable) state.playbackPosition else 0f,
                    modifier =
                        GlanceModifier
                            .fillMaxWidth()
                            .height(3.5.dp)
                            .cornerRadius(2.dp),
                    color = scrubberFill,
                    backgroundColor = scrubberTrack,
                )

                Spacer(GlanceModifier.height(3.dp))

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
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

                // 3. Apple Media Controls (Skip Previous, Circular Play/Pause, Skip Next)
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WidgetControlButton(
                        modifier = GlanceModifier.size(if (compact) 36.dp else 40.dp),
                        action = skipPreviousAction(),
                        icon = R.drawable.skip_previous,
                        contentDescription = context.getString(R.string.widget_previous),
                        backgroundColor = ColorProvider(Color.Transparent),
                        contentColor = ColorProvider(Color.White),
                        cornerRadius = 20.dp,
                        iconSize = if (compact) 20.dp else 22.dp,
                    )

                    Spacer(GlanceModifier.width(if (compact) 22.dp else 32.dp))

                    // Prominent Center Play / Pause Round Button
                    WidgetControlButton(
                        modifier = GlanceModifier.size(if (compact) 40.dp else 46.dp),
                        action = playPauseAction(),
                        icon = if (state.isPlaying) R.drawable.pause else R.drawable.play,
                        contentDescription =
                            context.getString(
                                if (state.isPlaying) R.string.widget_pause else R.string.play,
                            ),
                        backgroundColor = playButtonBg,
                        contentColor = ColorProvider(Color.White),
                        cornerRadius = if (compact) 20.dp else 23.dp,
                        iconSize = if (compact) 22.dp else 24.dp,
                    )

                    Spacer(GlanceModifier.width(if (compact) 22.dp else 32.dp))

                    WidgetControlButton(
                        modifier = GlanceModifier.size(if (compact) 36.dp else 40.dp),
                        action = skipNextAction(),
                        icon = R.drawable.skip_next,
                        contentDescription = context.getString(R.string.next),
                        backgroundColor = ColorProvider(Color.Transparent),
                        contentColor = ColorProvider(Color.White),
                        cornerRadius = 20.dp,
                        iconSize = if (compact) 20.dp else 22.dp,
                    )
                }

                // 4. Bottom Simulated Volume Slider (shown when ample vertical space)
                if (showVolume) {
                    Spacer(GlanceModifier.height(8.dp))

                    Row(
                        modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.volume_off),
                            contentDescription = "Volume Min",
                            colorFilter = ColorFilter.tint(textTertiary),
                            modifier = GlanceModifier.size(11.dp),
                        )

                        Spacer(GlanceModifier.width(8.dp))

                        Row(
                            modifier =
                                GlanceModifier
                                    .defaultWeight()
                                    .height(3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier =
                                    GlanceModifier
                                        .defaultWeight()
                                        .height(3.dp)
                                        .background(Color(0x66FFFFFF))
                                        .cornerRadius(1.5.dp),
                            ) {}
                            Spacer(GlanceModifier.width(2.dp))
                            Box(
                                modifier =
                                    GlanceModifier
                                        .width(42.dp)
                                        .height(3.dp)
                                        .background(Color(0x24FFFFFF))
                                        .cornerRadius(1.5.dp),
                            ) {}
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

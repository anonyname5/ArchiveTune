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

class CdJewelCaseWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            CdJewelCaseContent(context)
        }
    }
}

@Composable
private fun CdJewelCaseContent(context: Context) {
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

        // Acrylic transparent jewel case styling
        val acrylicBg = ColorProvider(Color(0xFF1E2024))
        val cdSilver = ColorProvider(Color(0xFFCFD6DF))
        val cdInnerRing = ColorProvider(Color(0xFF8C98A6))
        val spineBg = ColorProvider(Color(0xFF141518))
        val textPrimary = ColorProvider(Color(0xFFF5F5F7))
        val textSecondary = ColorProvider(Color(0xFFA0A5B0))
        val holographicCyan = ColorProvider(Color(0xFF64D2FF))

        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(acrylicBg)
                    .cornerRadius(20.dp)
                    .padding(if (compact) 3.dp else 5.dp)
                    .clickable(openArchiveTuneAction(context)),
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Jewel Case Left Spine
                Box(
                    modifier =
                        GlanceModifier
                            .width(10.dp)
                            .fillMaxHeight()
                            .background(spineBg)
                            .cornerRadius(4.dp),
                ) {}

                Spacer(GlanceModifier.width(6.dp))

                // Square Booklet Artwork inside the acrylic case
                val coverSize = if (compact) 82.dp else 98.dp
                Box(
                    modifier =
                        GlanceModifier
                            .size(coverSize)
                            .cornerRadius(6.dp),
                ) {
                    WidgetArtwork(
                        artPath = state.artPath,
                        context = context,
                        contentDescription = context.getString(R.string.album_cover_desc),
                        targetSize = coverSize,
                        cornerRadius = 6.dp,
                        palette = palette,
                        modifier = GlanceModifier.size(coverSize),
                    )
                }

                Spacer(GlanceModifier.width(8.dp))

                // Silver Compact Disc peeking out from the jewel tray
                val cdSize = if (compact) 64.dp else 76.dp
                Box(
                    modifier =
                        GlanceModifier
                            .size(cdSize)
                            .background(cdSilver)
                            .cornerRadius(999.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    // Inner iridescent ring
                    Box(
                        modifier =
                            GlanceModifier
                                .size(cdSize * 0.58f)
                                .background(cdInnerRing)
                                .cornerRadius(999.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        // Clear plastic clamping ring
                        Box(
                            modifier =
                                GlanceModifier
                                    .size(cdSize * 0.34f)
                                    .background(acrylicBg)
                                    .cornerRadius(999.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            // Spindle hole
                            Box(
                                modifier =
                                    GlanceModifier
                                        .size(10.dp)
                                        .background(spineBg)
                                        .cornerRadius(999.dp),
                            ) {}
                        }
                    }
                }

                Spacer(GlanceModifier.width(8.dp))

                // Right details & Acrylic controls
                Column(
                    modifier =
                        GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "COMPACT DISC",
                            style =
                                TextStyle(
                                    color = holographicCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                        Spacer(GlanceModifier.width(4.dp))
                        Text(
                            text = "DIGITAL AUDIO",
                            style =
                                TextStyle(
                                    color = textSecondary,
                                    fontSize = 8.sp,
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

                    // Track laser reading progress
                    if (state.isAvailable && state.playbackPosition > 0f) {
                        LinearProgressIndicator(
                            progress = state.playbackPosition,
                            color = holographicCyan,
                            backgroundColor = ColorProvider(Color(0xFF2E333C)),
                            modifier =
                                GlanceModifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .cornerRadius(2.dp),
                        )
                        Spacer(GlanceModifier.height(6.dp))
                    }

                    if (state.isAvailable) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 30.dp else 34.dp),
                                action = skipPreviousAction(),
                                icon = R.drawable.skip_previous,
                                contentDescription = context.getString(R.string.widget_previous),
                                backgroundColor = ColorProvider(Color(0xFF2C3038)),
                                contentColor = textPrimary,
                                cornerRadius = 8.dp,
                                iconSize = 16.dp,
                            )
                            Spacer(GlanceModifier.width(6.dp))
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 34.dp else 38.dp),
                                action = playPauseAction(),
                                icon = if (state.isPlaying) R.drawable.pause else R.drawable.play,
                                contentDescription =
                                    context.getString(
                                        if (state.isPlaying) R.string.widget_pause else R.string.play,
                                    ),
                                backgroundColor = holographicCyan,
                                contentColor = ColorProvider(Color(0xFF0F172A)),
                                cornerRadius = 10.dp,
                                iconSize = 20.dp,
                            )
                            Spacer(GlanceModifier.width(6.dp))
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 30.dp else 34.dp),
                                action = skipNextAction(),
                                icon = R.drawable.skip_next,
                                contentDescription = context.getString(R.string.next),
                                backgroundColor = ColorProvider(Color(0xFF2C3038)),
                                contentColor = textPrimary,
                                cornerRadius = 8.dp,
                                iconSize = 16.dp,
                            )
                        }
                    }
                }
            }
        }
    }
}

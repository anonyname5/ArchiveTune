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

class SynthwaveDashboardWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            SynthwaveDashboardContent(context)
        }
    }
}

@Composable
private fun SynthwaveDashboardContent(context: Context) {
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

        // 80s Synthwave / Cyberpunk Dashboard Palette
        val dashBg = ColorProvider(Color(0xFF0C0A16))
        val panelBg = ColorProvider(Color(0xFF161326))
        val neonPink = ColorProvider(Color(0xFFFF007F))
        val neonCyan = ColorProvider(Color(0xFF00E5FF))
        val neonAmber = ColorProvider(Color(0xFFFFB300))
        val textPrimary = ColorProvider(Color(0xFFFFFFFF))
        val textSecondary = ColorProvider(Color(0xFF8884A4))

        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(dashBg)
                    .cornerRadius(22.dp)
                    .padding(if (compact) 4.dp else 6.dp)
                    .clickable(openArchiveTuneAction(context)),
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Digital Cockpit Bezel with Album Artwork
                val artSize = if (compact) 72.dp else 88.dp
                Box(
                    modifier =
                        GlanceModifier
                            .size(artSize)
                            .background(panelBg)
                            .cornerRadius(10.dp)
                            .padding(3.dp),
                ) {
                    WidgetArtwork(
                        artPath = state.artPath,
                        context = context,
                        contentDescription = context.getString(R.string.album_cover_desc),
                        targetSize = artSize,
                        cornerRadius = 8.dp,
                        palette = palette,
                        modifier = GlanceModifier.fillMaxSize(),
                    )
                }

                Spacer(GlanceModifier.width(if (compact) 10.dp else 14.dp))

                // Right: Cockpit HUD, RPM Progress, and Flight Controls
                Column(
                    modifier =
                        GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // HUD Top Bar
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "TURBO RPM // HIGHWAY 86",
                            style =
                                TextStyle(
                                    color = neonCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                        Spacer(GlanceModifier.defaultWeight())
                        Text(
                            text = if (state.isPlaying) "168 MPH" else "0 MPH",
                            style =
                                TextStyle(
                                    color = neonAmber,
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

                    // Tachometer Progress Gauge (Neon Cyan with Pink Redline track)
                    if (state.isAvailable && state.playbackPosition > 0f) {
                        LinearProgressIndicator(
                            progress = state.playbackPosition,
                            color = neonCyan,
                            backgroundColor = ColorProvider(Color(0xFF281C3D)),
                            modifier =
                                GlanceModifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .cornerRadius(2.dp),
                        )
                        Spacer(GlanceModifier.height(6.dp))
                    }

                    // Cyberpunk Cockpit Controls
                    if (state.isAvailable) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 30.dp else 34.dp),
                                action = skipPreviousAction(),
                                icon = R.drawable.skip_previous,
                                contentDescription = context.getString(R.string.widget_previous),
                                backgroundColor = panelBg,
                                contentColor = neonCyan,
                                cornerRadius = 8.dp,
                                iconSize = 16.dp,
                            )
                            Spacer(GlanceModifier.width(8.dp))
                            // Hot Pink Turbo Engine Button
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 34.dp else 38.dp),
                                action = playPauseAction(),
                                icon = if (state.isPlaying) R.drawable.pause else R.drawable.play,
                                contentDescription =
                                    context.getString(
                                        if (state.isPlaying) R.string.widget_pause else R.string.play,
                                    ),
                                backgroundColor = neonPink,
                                contentColor = ColorProvider(Color.White),
                                cornerRadius = 10.dp,
                                iconSize = 20.dp,
                            )
                            Spacer(GlanceModifier.width(8.dp))
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 30.dp else 34.dp),
                                action = skipNextAction(),
                                icon = R.drawable.skip_next,
                                contentDescription = context.getString(R.string.next),
                                backgroundColor = panelBg,
                                contentColor = neonCyan,
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

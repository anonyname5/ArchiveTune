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

class CassetteTapeWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            CassetteTapeContent(context)
        }
    }
}

@Composable
private fun CassetteTapeContent(context: Context) {
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

        // Cassette shell colors
        val shellBg = ColorProvider(Color(0xFF222226))
        val labelBg = ColorProvider(Color(0xFFF7F4EE))
        val labelText = ColorProvider(Color(0xFF1E1E22))
        val tapeWindowBg = ColorProvider(Color(0xFF141416))
        val tapeSpool = ColorProvider(Color(0xFFE8E8EC))
        val magneticTape = ColorProvider(Color(0xFF8D5B4C))
        val retroOrange = ColorProvider(Color(0xFFE65100))

        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(shellBg)
                    .cornerRadius(20.dp)
                    .padding(if (compact) 3.dp else 5.dp)
                    .clickable(openArchiveTuneAction(context)),
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                // Cassette Mixtape Upper Label Section
                Box(
                    modifier =
                        GlanceModifier
                            .fillMaxWidth()
                            .background(labelBg)
                            .cornerRadius(12.dp)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Artwork Sticker on the cassette label
                        val artSize = if (compact) 32.dp else 40.dp
                        WidgetArtwork(
                            artPath = state.artPath,
                            context = context,
                            contentDescription = context.getString(R.string.album_cover_desc),
                            targetSize = artSize,
                            cornerRadius = 6.dp,
                            palette = palette,
                            modifier = GlanceModifier.size(artSize),
                        )

                        Spacer(GlanceModifier.width(8.dp))

                        Column(modifier = GlanceModifier.defaultWeight()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SIDE A • C-90 HIGH BIAS",
                                    style =
                                        TextStyle(
                                            color = retroOrange,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                )
                            }
                            Text(
                                text = state.title,
                                style =
                                    TextStyle(
                                        color = labelText,
                                        fontSize = if (compact) 12.sp else 14.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                maxLines = 1,
                            )
                            Text(
                                text = state.artist,
                                style =
                                    TextStyle(
                                        color = ColorProvider(Color(0xFF5A5A60)),
                                        fontSize = 11.sp,
                                    ),
                                maxLines = 1,
                            )
                        }
                    }
                }

                Spacer(GlanceModifier.height(if (compact) 5.dp else 8.dp))

                // Middle Section: Cassette Tape Window with Dual Spools & Transport Controls
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Transparent Tape Window showing spools and magnetic tape
                    Box(
                        modifier =
                            GlanceModifier
                                .defaultWeight()
                                .height(if (compact) 38.dp else 44.dp)
                                .background(tapeWindowBg)
                                .cornerRadius(10.dp)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Left Spool
                            Box(
                                modifier =
                                    GlanceModifier
                                        .size(24.dp)
                                        .background(tapeSpool)
                                        .cornerRadius(999.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier =
                                        GlanceModifier
                                            .size(8.dp)
                                            .background(tapeWindowBg)
                                            .cornerRadius(999.dp),
                                ) {}
                            }

                            Spacer(GlanceModifier.width(6.dp))

                            // Magnetic Tape Progress Reel between spools
                            Box(modifier = GlanceModifier.defaultWeight()) {
                                LinearProgressIndicator(
                                    progress = state.playbackPosition,
                                    color = magneticTape,
                                    backgroundColor = ColorProvider(Color(0xFF2C2C32)),
                                    modifier =
                                        GlanceModifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .cornerRadius(3.dp),
                                )
                            }

                            Spacer(GlanceModifier.width(6.dp))

                            // Right Spool
                            Box(
                                modifier =
                                    GlanceModifier
                                        .size(24.dp)
                                        .background(tapeSpool)
                                        .cornerRadius(999.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier =
                                        GlanceModifier
                                            .size(8.dp)
                                            .background(tapeWindowBg)
                                            .cornerRadius(999.dp),
                                ) {}
                            }
                        }
                    }

                    if (state.isAvailable) {
                        Spacer(GlanceModifier.width(8.dp))

                        // Mechanical Walkman Play/Skip buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 32.dp else 36.dp),
                                action = skipPreviousAction(),
                                icon = R.drawable.skip_previous,
                                contentDescription = context.getString(R.string.widget_previous),
                                backgroundColor = ColorProvider(Color(0xFF33333A)),
                                contentColor = ColorProvider(Color.White),
                                cornerRadius = 8.dp,
                                iconSize = 16.dp,
                            )
                            Spacer(GlanceModifier.width(6.dp))
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 36.dp else 42.dp),
                                action = playPauseAction(),
                                icon = if (state.isPlaying) R.drawable.pause else R.drawable.play,
                                contentDescription =
                                    context.getString(
                                        if (state.isPlaying) R.string.widget_pause else R.string.play,
                                    ),
                                backgroundColor = retroOrange,
                                contentColor = ColorProvider(Color.White),
                                cornerRadius = 10.dp,
                                iconSize = 20.dp,
                            )
                            Spacer(GlanceModifier.width(6.dp))
                            WidgetControlButton(
                                modifier = GlanceModifier.size(if (compact) 32.dp else 36.dp),
                                action = skipNextAction(),
                                icon = R.drawable.skip_next,
                                contentDescription = context.getString(R.string.next),
                                backgroundColor = ColorProvider(Color(0xFF33333A)),
                                contentColor = ColorProvider(Color.White),
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

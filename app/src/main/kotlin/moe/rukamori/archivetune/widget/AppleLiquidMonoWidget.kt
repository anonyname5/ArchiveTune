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
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.unit.ColorProvider
import moe.rukamori.archivetune.R

class AppleLiquidMonoWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            AppleLiquidMonoContent(context)
        }
    }
}

@Composable
private fun AppleLiquidMonoContent(context: Context) {
    val prefs = currentState<Preferences>()
    val state = prefs.toWidgetPlaybackState(context)

    val dominant = state.dominantColor?.let { Color(it) }
    val glassBaseDark = Color(0x6012131D)
    val liquidAuraColor = remember(dominant) { dominant?.copy(alpha = 0.35f) ?: Color.Transparent }
    val liquidLensBg = remember(dominant) {
        dominant?.let {
            val r = (it.red * 0.45f + 1f * 0.55f).coerceIn(0f, 1f)
            val g = (it.green * 0.45f + 1f * 0.55f).coerceIn(0f, 1f)
            val b = (it.blue * 0.45f + 1f * 0.55f).coerceIn(0f, 1f)
            Color(red = r, green = g, blue = b, alpha = 0.50f)
        } ?: Color(0x45FFFFFF)
    }

    // Outer Specular Crystalline Glass Rim
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0x70FFFFFF))
            .cornerRadius(28.dp)
            .padding(1.2.dp)
            .clickable(playPauseAction()),
        contentAlignment = Alignment.Center,
    ) {
        // Inner Refraction Shadow
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0x35000000))
                .cornerRadius(27.dp)
                .padding(0.8.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Smoked Acrylic Base
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(glassBaseDark)
                    .cornerRadius(26.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Dynamic Liquid Aura Layer
                if (state.dominantColor != null) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .background(liquidAuraColor)
                            .cornerRadius(26.dp),
                    ) {}
                }

                // Inner Liquid Lens Button
                Box(
                    modifier = GlanceModifier
                        .size(38.dp)
                        .background(Color(0x50FFFFFF))
                        .cornerRadius(19.dp)
                        .padding(0.8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .background(liquidLensBg)
                            .cornerRadius(18.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            provider = ImageProvider(
                                if (state.isPlaying) R.drawable.ic_apple_pause else R.drawable.ic_apple_play,
                            ),
                            contentDescription = if (state.isPlaying) "Pause" else "Play",
                            colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                            modifier = GlanceModifier.size(19.dp),
                        )
                    }
                }
            }
        }
    }
}

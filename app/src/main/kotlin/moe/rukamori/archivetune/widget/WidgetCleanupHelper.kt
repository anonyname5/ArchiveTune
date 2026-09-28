/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.widget

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

/**
 * Programmatically disables old/removed widget receiver components
 * so that the system launcher removes them from the widget picker.
 *
 * On some launchers (Xiaomi HyperOS), simply removing the receiver
 * from the manifest or deleting the class is not enough — the launcher
 * caches widget entries aggressively. By keeping disabled stub receivers
 * in the manifest AND explicitly calling [PackageManager.setComponentEnabledSetting]
 * with [PackageManager.COMPONENT_ENABLED_STATE_DISABLED], we force the system
 * to issue a package-changed broadcast that clears the cached entries.
 */
internal object WidgetCleanupHelper {

    /**
     * Fully-qualified class names of all removed widget receivers.
     * These must match the `android:name` attribute in the manifest stubs.
     */
    private val removedReceiverClassNames = listOf(
        "moe.rukamori.archivetune.widget.AlbumArtWidgetReceiver",
        "moe.rukamori.archivetune.widget.AnalogVuMeterWidgetReceiver",
        "moe.rukamori.archivetune.widget.BraunMinimalistWidgetReceiver",
        "moe.rukamori.archivetune.widget.CassetteTapeWidgetReceiver",
        "moe.rukamori.archivetune.widget.CdJewelCaseWidgetReceiver",
        "moe.rukamori.archivetune.widget.ChromaticAuraWidgetReceiver",
        "moe.rukamori.archivetune.widget.ConcertTicketWidgetReceiver",
        "moe.rukamori.archivetune.widget.ExpressivePetalWidgetReceiver",
        "moe.rukamori.archivetune.widget.GameBoyConsoleWidgetReceiver",
        "moe.rukamori.archivetune.widget.ListeningInsightsWidgetReceiver",
        "moe.rukamori.archivetune.widget.LofiPixelRoomWidgetReceiver",
        "moe.rukamori.archivetune.widget.ModularSynthWidgetReceiver",
        "moe.rukamori.archivetune.widget.MusicHubWidgetReceiver",
        "moe.rukamori.archivetune.widget.MusicWidgetReceiver",
        "moe.rukamori.archivetune.widget.NeonJukeboxWidgetReceiver",
        "moe.rukamori.archivetune.widget.NowPlayingCardWidgetReceiver",
        "moe.rukamori.archivetune.widget.PlaybackCapsuleWidgetReceiver",
        "moe.rukamori.archivetune.widget.PlaybackCommandWidgetReceiver",
        "moe.rukamori.archivetune.widget.PlaybackDeckWidgetReceiver",
        "moe.rukamori.archivetune.widget.PolaroidPhotoWidgetReceiver",
        "moe.rukamori.archivetune.widget.QueueFlowWidgetReceiver",
        "moe.rukamori.archivetune.widget.ReelToReelWidgetReceiver",
        "moe.rukamori.archivetune.widget.RetroClickWheelWidgetReceiver",
        "moe.rukamori.archivetune.widget.SoundwaveEqualizerWidgetReceiver",
        "moe.rukamori.archivetune.widget.SwissBrutalistWidgetReceiver",
        "moe.rukamori.archivetune.widget.SynthwaveDashboardWidgetReceiver",
        "moe.rukamori.archivetune.widget.VinylTurntableWidgetReceiver",
        "moe.rukamori.archivetune.widget.VoyagerCosmicWidgetReceiver",
    )

    /**
     * Call once on app startup (e.g. from [Application.onCreate] or a content provider).
     * Disables each removed receiver component if it isn't already disabled.
     * This triggers `ACTION_PACKAGE_CHANGED` which forces the launcher to
     * re-scan and drop ghost widget entries.
     */
    fun disableRemovedWidgets(context: Context) {
        val pm = context.packageManager
        for (className in removedReceiverClassNames) {
            val component = ComponentName(context.packageName, className)
            val currentState = try {
                pm.getComponentEnabledSetting(component)
            } catch (_: Exception) {
                // Component may not exist in this build variant
                continue
            }
            if (currentState != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                try {
                    pm.setComponentEnabledSetting(
                        component,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP,
                    )
                } catch (_: Exception) {
                    // Ignore — component may not exist in manifest
                }
            }
        }
    }
}

/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import moe.rukamori.archivetune.BuildConfig
import moe.rukamori.archivetune.utils.GlobalLog
import timber.log.Timber

/**
 * Diagnostic and debugging logger for the ArchiveTune widget system.
 *
 * Scans Android's AppWidgetManager, PackageManager component enabled states,
 * currently placed widget instances, and launcher environment.
 * Output is logged both to Timber and [GlobalLog] (visible in Settings -> Debug Logs).
 */
object WidgetDebugLogger {

    private const val TAG = "WidgetDebug"

    data class DiagnosticsSummary(
        val packageName: String,
        val versionCode: Int,
        val versionName: String,
        val registeredProvidersCount: Int,
        val registeredProviderNames: List<String>,
        val activeWidgetStates: Map<String, String>,
        val ghostWidgetStates: Map<String, String>,
        val placedWidgetIds: Map<String, List<Int>>,
        val totalPlacedInstances: Int,
        val defaultLauncher: String,
        val isXiaomiDevice: Boolean,
        val miuiVersion: String?,
    )

    private val activeWidgetClasses = listOf(
        AppleLockscreenWidgetReceiver::class.java,
        PlaybackSpotlightWidgetReceiver::class.java,
        ZenTanzakuWidgetReceiver::class.java,
        AppleLiquidCompactWidgetReceiver::class.java,
        AppleLiquidPillWidgetReceiver::class.java,
        AppleLiquidHeroWidgetReceiver::class.java,
        AppleLiquidMiniWidgetReceiver::class.java,
        AppleLiquidSplitWidgetReceiver::class.java,
        AppleLiquidPosterWidgetReceiver::class.java,
        AppleLiquidIslandWidgetReceiver::class.java,
        AppleLiquidOrbWidgetReceiver::class.java,
        AppleLiquidPanoWidgetReceiver::class.java,
        AppleLiquidMonoWidgetReceiver::class.java,
        AppleLiquidDuoWidgetReceiver::class.java,
        AppleLiquidShelfWidgetReceiver::class.java,
        AppleLiquidNowWidgetReceiver::class.java,
    )

    private val removedWidgetClassNames = listOf(
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

    private fun stateToString(state: Int): String = when (state) {
        -1 -> "ABSENT (not in manifest)"
        PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> "DEFAULT (uses manifest)"
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> "ENABLED"
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> "DISABLED"
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER -> "DISABLED_USER"
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED -> "DISABLED_UNTIL_USED"
        else -> "UNKNOWN ($state)"
    }

    /**
     * Gathers a complete diagnostics snapshot of all widgets, providers, and placements.
     */
    fun getDiagnostics(context: Context): DiagnosticsSummary {
        val pm = context.packageManager
        val awm = AppWidgetManager.getInstance(context)

        // 1. Registered AppWidget providers in the OS
        val providers = try {
            awm.getInstalledProvidersForPackage(context.packageName, Process.myUserHandle())
        } catch (_: Exception) {
            awm.installedProviders.filter { it.provider.packageName == context.packageName }
        }
        val providerNames = providers.map { it.provider.shortClassName.removePrefix(".") }

        // 2. Active widget states
        val activeStates = mutableMapOf<String, String>()
        for (clazz in activeWidgetClasses) {
            val cn = ComponentName(context, clazz)
            val st = try {
                pm.getReceiverInfo(cn, 0)
                pm.getComponentEnabledSetting(cn)
            } catch (_: PackageManager.NameNotFoundException) {
                -1
            } catch (_: Exception) {
                -2
            }
            activeStates[clazz.simpleName] = stateToString(st)
        }

        // 3. Removed ghost widget states
        val ghostStates = mutableMapOf<String, String>()
        for (name in removedWidgetClassNames) {
            val cn = ComponentName(context.packageName, name)
            val st = try {
                pm.getReceiverInfo(cn, 0)
                pm.getComponentEnabledSetting(cn)
            } catch (_: PackageManager.NameNotFoundException) {
                -1
            } catch (_: Exception) {
                -2
            }
            val short = name.substringAfterLast(".")
            ghostStates[short] = stateToString(st)
        }

        // 4. Placed widget IDs
        val placedMap = mutableMapOf<String, List<Int>>()
        var totalPlaced = 0
        for (clazz in activeWidgetClasses) {
            val ids = try {
                awm.getAppWidgetIds(ComponentName(context, clazz)).toList()
            } catch (_: Exception) {
                emptyList()
            }
            if (ids.isNotEmpty()) {
                placedMap[clazz.simpleName] = ids
                totalPlaced += ids.size
            }
        }
        // Also check if any ghost widget has orphaned IDs
        for (name in removedWidgetClassNames) {
            val ids = try {
                awm.getAppWidgetIds(ComponentName(context.packageName, name)).toList()
            } catch (_: Exception) {
                emptyList()
            }
            if (ids.isNotEmpty()) {
                val short = name.substringAfterLast(".")
                placedMap["[GHOST] $short"] = ids
                totalPlaced += ids.size
            }
        }

        // 5. Default launcher
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = pm.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        val launcherPkg = resolveInfo?.activityInfo?.packageName ?: "Unknown"

        // 6. Device OEM info
        val isXiaomi = Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true) ||
            Build.BRAND.equals("Xiaomi", ignoreCase = true) ||
            Build.BRAND.equals("Redmi", ignoreCase = true) ||
            Build.BRAND.equals("POCO", ignoreCase = true)

        val miuiVersion = try {
            val clazz = Class.forName("android.os.SystemProperties")
            val getMethod = clazz.getMethod("get", String::class.java)
            val ver = getMethod.invoke(null, "ro.miui.ui.version.name") as? String
            ver?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }

        return DiagnosticsSummary(
            packageName = context.packageName,
            versionCode = BuildConfig.VERSION_CODE,
            versionName = BuildConfig.VERSION_NAME,
            registeredProvidersCount = providers.size,
            registeredProviderNames = providerNames,
            activeWidgetStates = activeStates,
            ghostWidgetStates = ghostStates,
            placedWidgetIds = placedMap,
            totalPlacedInstances = totalPlaced,
            defaultLauncher = launcherPkg,
            isXiaomiDevice = isXiaomi,
            miuiVersion = miuiVersion,
        )
    }

    /**
     * Dumps the full widget diagnostic report into Timber and GlobalLog.
     * Returns the formatted string report.
     */
    fun dumpToLog(context: Context): String {
        val diag = getDiagnostics(context)
        val sb = StringBuilder()

        sb.appendLine("═══════════════════════════════════════════════════════")
        sb.appendLine("       ARCHIVETUNE WIDGET SYSTEM DIAGNOSTICS")
        sb.appendLine("═══════════════════════════════════════════════════════")
        sb.appendLine("• App: ${diag.packageName} (v${diag.versionName}, code=${diag.versionCode})")
        sb.appendLine("• Device: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT})")
        sb.appendLine("• Launcher: ${diag.defaultLauncher}")
        if (diag.isXiaomiDevice) {
            sb.appendLine("• Xiaomi OS Detected: HyperOS/MIUI version=${diag.miuiVersion ?: "N/A"}")
        }
        sb.appendLine("• Placed Widget Instances: ${diag.totalPlacedInstances}")
        diag.placedWidgetIds.forEach { (name, ids) ->
            sb.appendLine("   └─ $name -> ids=$ids")
        }

        sb.appendLine("───────────────────────────────────────────────────────")
        sb.appendLine("OS-REGISTERED PROVIDERS (${diag.registeredProvidersCount} total recognized by Android):")
        diag.registeredProviderNames.forEachIndexed { i, name ->
            sb.appendLine("   ${i + 1}. $name")
        }

        sb.appendLine("───────────────────────────────────────────────────────")
        sb.appendLine("ACTIVE WIDGETS (${diag.activeWidgetStates.size} defined):")
        diag.activeWidgetStates.forEach { (name, state) ->
            sb.appendLine("   • $name: $state")
        }

        sb.appendLine("───────────────────────────────────────────────────────")
        sb.appendLine("REMOVED OLD WIDGETS (${diag.ghostWidgetStates.size} removed targets):")
        var absentCount = 0
        var disabledCount = 0
        var activeCount = 0
        diag.ghostWidgetStates.forEach { (name, state) ->
            when {
                state.startsWith("ABSENT") -> absentCount++
                state.startsWith("DISABLED") -> disabledCount++
                else -> {
                    activeCount++
                    sb.appendLine("   ⚠️ $name: $state (STILL ACTIVE!)")
                }
            }
        }
        sb.appendLine("   └─ Summary: $absentCount absent (cleanly uninstalled), $disabledCount disabled, $activeCount active")
        if (activeCount == 0) {
            sb.appendLine("   └─ Clean: All 28 old widgets are cleanly removed from the manifest.")
        }
        sb.appendLine("═══════════════════════════════════════════════════════")

        val report = sb.toString()

        // Log each line to android.util.Log, Timber and GlobalLog
        report.lines().forEach { line ->
            android.util.Log.i(TAG, line)
            Timber.tag(TAG).i(line)
            GlobalLog.append(android.util.Log.INFO, TAG, line)
        }

        return report
    }
}

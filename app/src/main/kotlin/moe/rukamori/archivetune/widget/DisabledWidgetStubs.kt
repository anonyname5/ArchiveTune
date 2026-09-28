/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

@file:Suppress("unused")

package moe.rukamori.archivetune.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Stub receivers for removed widgets.
 *
 * These exist solely so the AndroidManifest can declare them with
 * `android:enabled="false"`, which forces the system (especially
 * Xiaomi HyperOS launcher) to deregister ghost widget entries
 * from the widget picker cache.
 *
 * They are never instantiated at runtime because the manifest
 * declares them as disabled. The classes must exist for the
 * manifest merger / build to succeed.
 */
open class DisabledWidgetStub : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) = Unit
}

class AlbumArtWidgetReceiver : DisabledWidgetStub()
class AnalogVuMeterWidgetReceiver : DisabledWidgetStub()
class BraunMinimalistWidgetReceiver : DisabledWidgetStub()
class CassetteTapeWidgetReceiver : DisabledWidgetStub()
class CdJewelCaseWidgetReceiver : DisabledWidgetStub()
class ChromaticAuraWidgetReceiver : DisabledWidgetStub()
class ConcertTicketWidgetReceiver : DisabledWidgetStub()
class ExpressivePetalWidgetReceiver : DisabledWidgetStub()
class GameBoyConsoleWidgetReceiver : DisabledWidgetStub()
class ListeningInsightsWidgetReceiver : DisabledWidgetStub()
class LofiPixelRoomWidgetReceiver : DisabledWidgetStub()
class ModularSynthWidgetReceiver : DisabledWidgetStub()
class MusicHubWidgetReceiver : DisabledWidgetStub()
class MusicWidgetReceiver : DisabledWidgetStub()
class NeonJukeboxWidgetReceiver : DisabledWidgetStub()
class NowPlayingCardWidgetReceiver : DisabledWidgetStub()
class PlaybackCapsuleWidgetReceiver : DisabledWidgetStub()
class PlaybackCommandWidgetReceiver : DisabledWidgetStub()
class PlaybackDeckWidgetReceiver : DisabledWidgetStub()
class PolaroidPhotoWidgetReceiver : DisabledWidgetStub()
class QueueFlowWidgetReceiver : DisabledWidgetStub()
class ReelToReelWidgetReceiver : DisabledWidgetStub()
class RetroClickWheelWidgetReceiver : DisabledWidgetStub()
class SoundwaveEqualizerWidgetReceiver : DisabledWidgetStub()
class SwissBrutalistWidgetReceiver : DisabledWidgetStub()
class SynthwaveDashboardWidgetReceiver : DisabledWidgetStub()
class VinylTurntableWidgetReceiver : DisabledWidgetStub()
class VoyagerCosmicWidgetReceiver : DisabledWidgetStub()

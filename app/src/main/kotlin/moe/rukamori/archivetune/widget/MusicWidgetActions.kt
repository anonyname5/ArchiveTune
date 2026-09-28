/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.widget

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import moe.rukamori.archivetune.playback.MusicService

class PlayPauseAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        sendWidgetAction(context, ACTION_PLAY_PAUSE)
    }
}

class SkipNextAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        sendWidgetAction(context, ACTION_SKIP_NEXT)
    }
}

class SkipPrevAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        sendWidgetAction(context, ACTION_SKIP_PREV)
    }
}

class VolumeDownAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
        syncWidgetVolume(context, audioManager, glanceId)
    }
}

class VolumeUpAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
        syncWidgetVolume(context, audioManager, glanceId)
    }
}

private suspend fun syncWidgetVolume(
    context: Context,
    audioManager: AudioManager,
    glanceId: GlanceId,
) {
    runCatching {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val progress = (current.toFloat() / max.toFloat()).coerceIn(0f, 1f)
        val widget = AppleLockscreenWidget()
        val manager = GlanceAppWidgetManager(context)
        val ids = manager.getGlanceIds(AppleLockscreenWidget::class.java).ifEmpty { listOf(glanceId) }
        ids.forEach { id ->
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                prefs.toMutableWidgetPreferences().apply {
                    this[MusicWidgetKeys.VOLUME_PROGRESS] = progress
                }
            }
            widget.update(context, id)
        }
    }.onFailure { error ->
        Log.e(TAG, "Failed to sync widget volume", error)
    }
}

private const val ACTION_PLAY_PAUSE = "moe.rukamori.archivetune.WIDGET_PLAY_PAUSE"
private const val ACTION_SKIP_NEXT = "moe.rukamori.archivetune.WIDGET_SKIP_NEXT"
private const val ACTION_SKIP_PREV = "moe.rukamori.archivetune.WIDGET_SKIP_PREV"
private const val TAG = "MusicWidgetActions"

private fun sendWidgetAction(
    context: Context,
    action: String,
) {
    val intent = Intent(action).setClass(context, MusicService::class.java)
    runCatching {
        context.startService(intent)
    }.onFailure { error ->
        Log.e(TAG, "Failed to send widget action: $action", error)
    }
}

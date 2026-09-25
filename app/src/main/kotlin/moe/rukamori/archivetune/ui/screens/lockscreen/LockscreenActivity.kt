/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.ui.screens.lockscreen

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import moe.rukamori.archivetune.R
import moe.rukamori.archivetune.db.MusicDatabase
import moe.rukamori.archivetune.extensions.togglePlayPause
import moe.rukamori.archivetune.models.MediaMetadata
import moe.rukamori.archivetune.playback.MusicService
import moe.rukamori.archivetune.playback.PlayerConnection
import moe.rukamori.archivetune.ui.theme.ArchiveTuneTheme
import javax.inject.Inject

@AndroidEntryPoint
class LockscreenActivity : ComponentActivity() {
    @Inject
    lateinit var database: MusicDatabase

    private var playerConnection by mutableStateOf<PlayerConnection?>(null)

    private val serviceConnection =
        object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName?,
                binder: IBinder?,
            ) {
                if (binder is MusicService.MusicBinder) {
                    playerConnection =
                        PlayerConnection(
                            this@LockscreenActivity,
                            binder,
                            database,
                            lifecycleScope,
                        )
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                playerConnection = null
            }
        }

    private val screenOffReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?,
            ) {
                if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                    finish()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure native lockscreen display flags
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
        )

        WindowCompat.setDecorFitsSystemWindows(window, false)

        onBackPressedDispatcher.addCallback(this) {
            dismissLockscreen()
        }

        val screenFilter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(screenOffReceiver, screenFilter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(screenOffReceiver, screenFilter)
        }

        bindService(
            Intent(this, MusicService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE,
        )

        setContent {
            ArchiveTuneTheme {
                val conn = playerConnection
                val fallbackMetadata = remember { MutableStateFlow<MediaMetadata?>(null) }
                val fallbackPlaying = remember { MutableStateFlow(false) }
                val mediaMetadata by (conn?.mediaMetadata ?: fallbackMetadata).collectAsStateWithLifecycle()
                val isPlaying by (conn?.isPlaying ?: fallbackPlaying).collectAsStateWithLifecycle()

                var currentPos by remember { mutableLongStateOf(0L) }
                var songDuration by remember { mutableLongStateOf(0L) }

                LaunchedEffect(conn, isPlaying) {
                    if (conn != null) {
                        currentPos = (conn.player?.currentPosition ?: 0L).coerceAtLeast(0L)
                        songDuration = conn.player?.duration?.coerceAtLeast(0L) ?: 0L
                        while (isPlaying) {
                            currentPos = (conn.player?.currentPosition ?: 0L).coerceAtLeast(0L)
                            songDuration = conn.player?.duration?.coerceAtLeast(0L) ?: 0L
                            delay(500L)
                        }
                    }
                }

                val fallbackSkip = remember { MutableStateFlow(true) }
                val canSkipPrev by (conn?.canSkipPrevious ?: fallbackSkip).collectAsStateWithLifecycle()
                val canSkipNxt by (conn?.canSkipNext ?: fallbackSkip).collectAsStateWithLifecycle()

                val metadata =
                    mediaMetadata ?: MediaMetadata(
                        id = "",
                        title = getString(R.string.app_name),
                        artists = emptyList(),
                        duration = 0,
                    )

                LockscreenPlayerContent(
                    mediaMetadata = metadata,
                    isPlaying = isPlaying,
                    position = currentPos,
                    duration = songDuration,
                    canSkipPrevious = canSkipPrev,
                    canSkipNext = canSkipNxt,
                    onPlayPause = { conn?.player?.togglePlayPause() },
                    onSkipPrevious = { conn?.seekToPrevious() },
                    onSkipNext = { conn?.seekToNext() },
                    onSeek = { conn?.player?.seekTo(it) },
                    onDismiss = ::dismissLockscreen,
                )
            }
        }
    }

    private fun dismissLockscreen() {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager?.requestDismissKeyguard(
                this,
                object : KeyguardManager.KeyguardDismissCallback() {
                    override fun onDismissSucceeded() {
                        finish()
                    }

                    override fun onDismissCancelled() {
                        // User cancelled unlock dialog, keep player active
                    }

                    override fun onDismissError() {
                        finish()
                    }
                },
            )
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(screenOffReceiver)
        } catch (_: IllegalArgumentException) {
        }
        try {
            unbindService(serviceConnection)
        } catch (_: IllegalArgumentException) {
        }
    }
}

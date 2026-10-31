package com.soulspace.app.service

import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import kotlinx.coroutines.*

class AudioPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var sleepTimerJob: Job? = null

    companion object {
        const val ACTION_PLAY_SOUND = "com.soulspace.app.ACTION_PLAY_SOUND"
        const val ACTION_STOP_SOUND = "com.soulspace.app.ACTION_STOP_SOUND"
        const val ACTION_SET_SLEEP_TIMER = "com.soulspace.app.ACTION_SET_SLEEP_TIMER"
        const val EXTRA_SOUND_URI = "extra_sound_uri"
        const val EXTRA_SLEEP_MINUTES = "extra_sleep_minutes"
        const val EXTRA_SOUND_TITLE = "extra_sound_title"
    }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
        }
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_SOUND -> {
                val uriString = intent.getStringExtra(EXTRA_SOUND_URI)
                val title = intent.getStringExtra(EXTRA_SOUND_TITLE) ?: "Calm Soundscape"
                if (!uriString.isNullOrEmpty()) {
                    val mediaItem = MediaItem.Builder()
                        .setMediaId(uriString)
                        .setUri(uriString)
                        .build()
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.play()
                }
            }
            ACTION_STOP_SOUND -> {
                player.stop()
                sleepTimerJob?.cancel()
            }
            ACTION_SET_SLEEP_TIMER -> {
                val minutes = intent.getIntExtra(EXTRA_SLEEP_MINUTES, 30)
                scheduleSleepTimer(minutes)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun scheduleSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        sleepTimerJob = serviceScope.launch {
            // Convert to millis, fade out during the last 60 seconds
            val delayMillis = (minutes * 60 * 1000L) - 60_000L
            if (delayMillis > 0) {
                delay(delayMillis)
            }
            // Gentle fade out
            for (i in 10 downTo 0) {
                player.volume = i / 10f
                delay(6000L)
            }
            player.stop()
            player.volume = 1f
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        serviceScope.cancel()
        super.onDestroy()
    }
}


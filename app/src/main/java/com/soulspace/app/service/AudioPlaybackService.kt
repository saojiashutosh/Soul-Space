package com.soulspace.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Keeps soundscape playback alive in the background. The audio itself lives in [SoundMixer];
 * this service only owns the foreground notification and shuts down when nothing is playing.
 */
class AudioPlaybackService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var observer: Job? = null

    companion object {
        const val ACTION_START = "com.soulspace.app.ACTION_START_SOUNDS"
        const val ACTION_STOP_ALL = "com.soulspace.app.ACTION_STOP_ALL_SOUNDS"

        private const val CHANNEL_ID = "soulspace_soundscapes"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_ALL) {
            SoundMixer.stopAll()
            shutDown()
            return START_NOT_STICKY
        }

        // Must run right after startForegroundService(), or Android kills the app after ~5 seconds.
        enterForeground(buildNotification())

        if (observer == null) {
            observer = serviceScope.launch {
                SoundMixer.active.collect { active ->
                    if (active.isEmpty()) shutDown() else updateNotification()
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun buildNotification(): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Soundscapes", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val stopIntent = PendingIntent.getService(
            this, 0,
            Intent(this, AudioPlaybackService::class.java).setAction(ACTION_STOP_ALL),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val openIntent = packageManager.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 1, it, PendingIntent.FLAG_IMMUTABLE)
        }
        val names = SoundMixer.playingTitles()
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(if (names.size > 1) "${names.size} soundscapes playing" else "Soundscape playing")
            .setContentText(names.joinToString(" · ").ifEmpty { "SOULSPACE" })
            .setContentIntent(openIntent)
            .addAction(android.R.drawable.ic_media_pause, "Stop all", stopIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun enterForeground(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification() {
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, buildNotification())
    }

    /** The app was swiped away from the recents list: stop playing and remove the notification. */
    override fun onTaskRemoved(rootIntent: Intent?) {
        SoundMixer.stopAll()
        shutDown()
        super.onTaskRemoved(rootIntent)
    }

    private fun shutDown() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        SoundMixer.stopAll()
        super.onDestroy()
    }
}

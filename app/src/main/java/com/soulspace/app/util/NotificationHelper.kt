package com.soulspace.app.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.soulspace.app.MainActivity
import com.soulspace.app.R
import com.soulspace.app.service.ExactAlarmReceiver
import java.util.Calendar

object NotificationHelper {

    const val CHANNEL_REMINDERS = "soulspace_reminders"
    const val CHANNEL_BLOCKING = "soulspace_blocking"
    const val CHANNEL_AUDIO = "soulspace_audio"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                context.getString(R.string.channel_reminders_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_reminders_desc)
                enableVibration(true)
            }

            val blockingChannel = NotificationChannel(
                CHANNEL_BLOCKING,
                context.getString(R.string.channel_blocking_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_blocking_desc)
                enableVibration(true)
            }

            val audioChannel = NotificationChannel(
                CHANNEL_AUDIO,
                context.getString(R.string.channel_audio_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.channel_audio_desc)
            }

            notificationManager.createNotificationChannels(listOf(reminderChannel, blockingChannel, audioChannel))
        }
    }

    fun scheduleDailyExactAlarm(
        context: Context,
        requestCode: Int,
        hour: Int,
        minute: Int,
        title: String,
        message: String,
        category: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ExactAlarmReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("category", category)
            putExtra("requestCode", requestCode)
            putExtra("hour", hour)
            putExtra("minute", minute)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // Android 12+ lets users turn exact alarms off; setting one then throws, so fall back to an inexact one
        val exactAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (!exactAllowed) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun showMindfulNotification(
        context: Context,
        id: Int,
        title: String,
        message: String,
        channelId: String = CHANNEL_REMINDERS
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(id, builder.build())
    }
}


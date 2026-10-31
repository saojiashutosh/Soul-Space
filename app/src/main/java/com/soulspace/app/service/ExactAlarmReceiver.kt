package com.soulspace.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.soulspace.app.util.NotificationHelper
import java.util.Calendar

class ExactAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Mindful Reminder"
        val message = intent.getStringExtra("message") ?: "Time for your SoulSpace practice."
        val category = intent.getStringExtra("category") ?: "REMINDER"
        val requestCode = intent.getIntExtra("requestCode", 100)

        // Display Notification
        NotificationHelper.showMindfulNotification(
            context = context,
            id = requestCode,
            title = title,
            message = message,
            channelId = NotificationHelper.CHANNEL_REMINDERS
        )

        // Reschedule for the next day to maintain exact 24h recurrence without time drift
        val originalHour = intent.getIntExtra("hour", Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
        val originalMinute = intent.getIntExtra("minute", Calendar.getInstance().get(Calendar.MINUTE))

        NotificationHelper.scheduleDailyExactAlarm(
            context = context,
            requestCode = requestCode,
            hour = originalHour,
            minute = originalMinute,
            title = title,
            message = message,
            category = category
        )
    }
}


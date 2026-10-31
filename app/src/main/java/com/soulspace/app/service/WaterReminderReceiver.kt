package com.soulspace.app.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.soulspace.app.MainActivity
import com.soulspace.app.data.local.SoulSpaceDatabase
import com.soulspace.app.data.local.entity.WaterLogEntity
import com.soulspace.app.data.preferences.UserPreferencesRepository
import com.soulspace.app.util.NotificationHelper
import com.soulspace.app.util.WaterReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Shows the "time to drink" notification, or logs a glass from its action button. */
class WaterReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = UserPreferencesRepository(context).userPreferencesFlow.first()
                val db = SoulSpaceDatabase.getDatabase(context)
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                if (intent.action == WaterReminderScheduler.ACTION_ADD_GLASS) {
                    db.waterDao().logWater(
                        WaterLogEntity(dateString = today, amountMl = WaterReminderScheduler.GLASS_ML)
                    )
                    NotificationManagerCompat.from(context).cancel(WaterReminderScheduler.NOTIFICATION_ID)
                } else {
                    // Never ring outside the day window, even if the alarm fired late or the clock changed
                    if (prefs.waterReminderEnabled && WaterReminderScheduler.isWithinWindow(Calendar.getInstance(), prefs)) {
                        val drunk = db.waterDao().getTotalWaterForDate(today).first() ?: 0
                        if (drunk < prefs.dailyWaterGoalMl) {
                            showReminder(context, drunk, prefs.dailyWaterGoalMl)
                        }
                    }
                    WaterReminderScheduler.schedule(context, prefs)
                }
            } finally {
                pending.finish()
            }
        }
    }

    private fun showReminder(context: Context, drunkMl: Int, goalMl: Int) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val open = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val addGlass = PendingIntent.getBroadcast(
            context, 1,
            Intent(context, WaterReminderReceiver::class.java).setAction(WaterReminderScheduler.ACTION_ADD_GLASS),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Time for some water")
            .setContentText("$drunkMl of $goalMl ml so far today")
            .setContentIntent(open)
            .addAction(0, "Drank ${WaterReminderScheduler.GLASS_ML} ml", addGlass)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(WaterReminderScheduler.NOTIFICATION_ID, notification)
    }
}

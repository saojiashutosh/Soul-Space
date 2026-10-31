package com.soulspace.app.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.soulspace.app.data.preferences.UserPreferences
import com.soulspace.app.data.preferences.UserPreferencesRepository
import com.soulspace.app.service.WaterReminderReceiver
import kotlinx.coroutines.flow.first
import java.util.Calendar

/**
 * Schedules the next "drink water" reminder. Reminders ring every N minutes, but only between the
 * user's day start and day end, so nothing rings during the night. One alarm is pending at a time;
 * each time it fires the receiver schedules the next one.
 */
object WaterReminderScheduler {

    const val ACTION_REMIND = "com.soulspace.app.ACTION_WATER_REMIND"
    const val ACTION_ADD_GLASS = "com.soulspace.app.ACTION_WATER_ADD_GLASS"
    const val GLASS_ML = 250
    const val NOTIFICATION_ID = 3001
    private const val REQUEST_CODE = 3001

    private fun startMinutes(p: UserPreferences) = p.dayStartHour * 60 + p.dayStartMinute
    private fun endMinutes(p: UserPreferences) = p.dayEndHour * 60 + p.dayEndMinute

    /** True if [cal]'s time of day is inside the window. Works for windows that cross midnight too. */
    fun isWithinWindow(cal: Calendar, p: UserPreferences): Boolean {
        val now = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val start = startMinutes(p)
        val end = endMinutes(p)
        return if (start <= end) now in start..end else now >= start || now <= end
    }

    /**
     * The first reminder time after [from]: slots start at day start and repeat every interval
     * until day end. Null when reminders are off.
     */
    fun nextReminderMillis(p: UserPreferences, from: Long = System.currentTimeMillis()): Long? {
        if (!p.waterReminderEnabled) return null
        val intervalMillis = p.waterReminderIntervalMin.coerceAtLeast(5) * 60_000L
        val start = startMinutes(p)
        val end = endMinutes(p)
        val lengthMinutes = if (end >= start) end - start else end + 24 * 60 - start
        // -1 covers a window that began yesterday evening and runs past midnight
        for (dayOffset in -1..1) {
            val windowStart = Calendar.getInstance().apply {
                timeInMillis = from
                add(Calendar.DAY_OF_YEAR, dayOffset)
                set(Calendar.HOUR_OF_DAY, p.dayStartHour)
                set(Calendar.MINUTE, p.dayStartMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val windowEnd = windowStart + lengthMinutes * 60_000L
            var slot = windowStart
            while (slot <= windowEnd) {
                if (slot > from) return slot
                slot += intervalMillis
            }
        }
        return null
    }

    /** Replaces any pending reminder with the next one for these preferences. */
    fun schedule(context: Context, prefs: UserPreferences) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = pendingIntent(context)
        alarmManager.cancel(pendingIntent)
        val triggerAt = nextReminderMillis(prefs) ?: return

        val canBeExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (canBeExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            // Exact alarms are off by default on newer Android; a slightly late reminder is fine
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    suspend fun reschedule(context: Context) {
        schedule(context, UserPreferencesRepository(context).userPreferencesFlow.first())
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, WaterReminderReceiver::class.java).setAction(ACTION_REMIND)
        return PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

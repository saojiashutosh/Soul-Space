package com.soulspace.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.soulspace.app.data.local.SoulSpaceDatabase
import com.soulspace.app.util.NotificationHelper
import com.soulspace.app.util.WaterReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = SoulSpaceDatabase.getDatabase(context)

                    // Alarms are wiped by a reboot: restore the water reminder chain
                    WaterReminderScheduler.reschedule(context)

                    // Reschedule habit alarms
                    val habits = db.habitDao().getAllHabits().first()
                    habits.forEach { habit ->
                        if (habit.reminderHour != null && habit.reminderMinute != null) {
                            NotificationHelper.scheduleDailyExactAlarm(
                                context = context,
                                requestCode = (1000 + habit.id).toInt(),
                                hour = habit.reminderHour,
                                minute = habit.reminderMinute,
                                title = "Mindful Habit: ${habit.title}",
                                message = habit.description.ifEmpty { "Time for your daily sanctuary ritual." },
                                category = "HABIT"
                            )
                        }
                    }

                    // Reschedule timetable alarms
                    val tasks = db.timetableDao().getAllTasks().first()
                    tasks.forEach { task ->
                        if (task.reminderEnabled) {
                            NotificationHelper.scheduleDailyExactAlarm(
                                context = context,
                                requestCode = (2000 + task.id).toInt(),
                                hour = task.startHour,
                                minute = task.startMinute,
                                title = "Timetable: ${task.title}",
                                message = task.description.ifEmpty { "Scheduled activity starting now." },
                                category = "TIMETABLE"
                            )
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}


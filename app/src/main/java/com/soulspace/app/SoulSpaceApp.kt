package com.soulspace.app

import android.app.Application
import com.soulspace.app.data.local.SoulSpaceDatabase
import com.soulspace.app.service.DailyResetWorker
import com.soulspace.app.util.NotificationHelper
import com.soulspace.app.util.WaterReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SoulSpaceApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Notification Channels
        NotificationHelper.createNotificationChannels(this)
        // Warm up Database
        SoulSpaceDatabase.getDatabase(this)
        // Schedule Daily Maintenance & Reset Worker
        DailyResetWorker.schedule(this)
        // Make sure the next water reminder is pending (e.g. after the app was force-stopped)
        CoroutineScope(Dispatchers.IO).launch { WaterReminderScheduler.reschedule(this@SoulSpaceApp) }
    }
}


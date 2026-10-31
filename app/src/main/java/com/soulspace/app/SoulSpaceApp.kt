package com.soulspace.app

import android.app.Application
import com.soulspace.app.data.local.SoulSpaceDatabase
import com.soulspace.app.service.DailyResetWorker
import com.soulspace.app.util.NotificationHelper

class SoulSpaceApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Notification Channels
        NotificationHelper.createNotificationChannels(this)
        // Warm up Database
        SoulSpaceDatabase.getDatabase(this)
        // Schedule Daily Maintenance & Reset Worker
        DailyResetWorker.schedule(this)
    }
}


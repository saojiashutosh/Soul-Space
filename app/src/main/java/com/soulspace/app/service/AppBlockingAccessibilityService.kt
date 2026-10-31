package com.soulspace.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.soulspace.app.data.local.SoulSpaceDatabase
import com.soulspace.app.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.Calendar

class AppBlockingAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var database: SoulSpaceDatabase
    private lateinit var preferencesRepo: UserPreferencesRepository

    companion object {
        var isServiceRunning = false
            private set
        var temporaryAllowedPackage: String? = null
        var temporaryAllowedUntil: Long = 0L
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        database = SoulSpaceDatabase.getDatabase(applicationContext)
        preferencesRepo = UserPreferencesRepository(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        // Ignore self, system UI, and common home launcher components
        if (packageName == applicationContext.packageName ||
            packageName.contains("systemui") ||
            packageName.contains("launcher") ||
            packageName.contains("dialer") ||
            packageName.contains("emergency")
        ) {
            return
        }

        // Check if package is temporarily allowed via emergency flow
        if (temporaryAllowedPackage == packageName && System.currentTimeMillis() < temporaryAllowedUntil) {
            return
        }

        serviceScope.launch {
            checkAndEnforceBlock(packageName)
        }
    }

    private suspend fun checkAndEnforceBlock(packageName: String) {
        val blockedApp = database.blockerDao().getBlockedApp(packageName) ?: return
        if (!blockedApp.isBlocked) return

        val userPrefs = preferencesRepo.userPreferencesFlow.first()
        val now = System.currentTimeMillis()
        var shouldBlock = false
        var reason = "Scheduled Focus"
        var remainingSeconds = 0L

        // 1. Check temporary block (e.g. Focus Pomodoro or Quick Block)
        if (userPrefs.temporaryBlockEndTimeMillis > now) {
            shouldBlock = true
            reason = userPrefs.temporaryBlockLabel.ifEmpty { "Focus Session" }
            remainingSeconds = (userPrefs.temporaryBlockEndTimeMillis - now) / 1000
        }

        // 2. Check active recurring schedules
        if (!shouldBlock) {
            val activeSchedules = database.blockerDao().getActiveSchedules().first()
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)
            val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
            val currentDayBit = 1 shl (currentDayOfWeek - 1)

            for (schedule in activeSchedules) {
                // Check if today is active
                if ((schedule.daysOfWeekMask and currentDayBit) != 0) {
                    val isWithinTime = isTimeInRange(
                        currentHour, currentMinute,
                        schedule.startHour, schedule.startMinute,
                        schedule.endHour, schedule.endMinute
                    )
                    if (isWithinTime) {
                        shouldBlock = true
                        reason = "${schedule.title} (${schedule.modeName} Mode)"
                        remainingSeconds = calculateSecondsUntil(schedule.endHour, schedule.endMinute)
                        break
                    }
                }
            }
        }

        if (shouldBlock) {
            triggerLockoutScreen(packageName, blockedApp.appName, reason, remainingSeconds)
        }
    }

    private fun isTimeInRange(
        curH: Int, curM: Int,
        startH: Int, startM: Int,
        endH: Int, endM: Int
    ): Boolean {
        val curTotal = curH * 60 + curM
        val startTotal = startH * 60 + startM
        val endTotal = endH * 60 + endM

        return if (startTotal <= endTotal) {
            // Normal range during same day (e.g., 9:00 AM to 5:00 PM)
            curTotal in startTotal..endTotal
        } else {
            // Overnight range crossing midnight (e.g., 11:00 PM to 7:00 AM)
            curTotal >= startTotal || curTotal <= endTotal
        }
    }

    private fun calculateSecondsUntil(endH: Int, endM: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, endH)
            set(Calendar.MINUTE, endM)
            set(Calendar.SECOND, 0)
            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return ((target.timeInMillis - now.timeInMillis) / 1000).coerceAtLeast(60L)
    }

    private fun triggerLockoutScreen(
        packageName: String,
        appName: String,
        reason: String,
        remainingSeconds: Long
    ) {
        val intent = Intent(this, LockoutActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("PACKAGE_NAME", packageName)
            putExtra("APP_NAME", appName)
            putExtra("REASON", reason)
            putExtra("REMAINING_SECONDS", remainingSeconds)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        isServiceRunning = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        serviceScope.cancel()
    }
}


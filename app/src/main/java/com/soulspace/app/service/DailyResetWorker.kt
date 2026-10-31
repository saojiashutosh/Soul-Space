package com.soulspace.app.service

import android.content.Context
import androidx.work.*
import com.soulspace.app.data.preferences.UserPreferencesRepository
import com.soulspace.app.util.UsageStatsHelper
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DailyResetWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = UserPreferencesRepository(applicationContext)

        // Clean up expired temporary blocks
        val userPrefs = prefs.userPreferencesFlow.first()
        if (userPrefs.temporaryBlockEndTimeMillis in 1..System.currentTimeMillis()) {
            prefs.clearTemporaryBlock()
        }

        // Check if commitment mode has concluded
        if (userPrefs.isCommitmentModeActive && userPrefs.commitmentEndTimeMillis in 1..System.currentTimeMillis()) {
            prefs.setCommitmentMode(false, 0L)
        }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "soulspace_daily_reset_work"

        fun schedule(context: Context) {
            val now = Calendar.getInstance()
            val midnight = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 5)
                set(Calendar.SECOND, 0)
                if (before(now)) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val initialDelayMillis = midnight.timeInMillis - now.timeInMillis

            val workRequest = PeriodicWorkRequestBuilder<DailyResetWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}

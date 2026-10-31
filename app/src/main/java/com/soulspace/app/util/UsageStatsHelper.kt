package com.soulspace.app.util

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import java.util.Calendar

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val totalTimeInForegroundMillis: Long,
    val launchCount: Int = 0,
    val category: String = "Distracting"
)

object UsageStatsHelper {

    fun getDailyAppUsage(context: Context): List<AppUsageInfo> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyList()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val pm = context.packageManager
        val queryStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        if (queryStats.isNullOrEmpty()) {
            return emptyList()
        }

        val appUsageList = mutableListOf<AppUsageInfo>()

        for (stat in queryStats) {
            if (stat.totalTimeInForeground > 60_000) { // More than 1 min
                val pkg = stat.packageName
                // Ignore self and core system packages
                if (pkg == context.packageName || pkg.contains("launcher") || pkg.contains("systemui")) {
                    continue
                }

                val appName = try {
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    pkg
                }

                val category = guessCategory(pkg)

                appUsageList.add(
                    AppUsageInfo(
                        packageName = pkg,
                        appName = appName,
                        totalTimeInForegroundMillis = stat.totalTimeInForeground,
                        category = category
                    )
                )
            }
        }

        return appUsageList.sortedByDescending { it.totalTimeInForegroundMillis }
    }

    fun getTotalDailyScreenTimeMillis(context: Context): Long {
        val list = getDailyAppUsage(context)
        return list.sumOf { it.totalTimeInForegroundMillis }
    }

    private fun guessCategory(packageName: String): String {
        val lower = packageName.lowercase()
        return when {
            lower.contains("instagram") || lower.contains("facebook") || lower.contains("twitter") ||
            lower.contains("tiktok") || lower.contains("threads") || lower.contains("snapchat") ||
            lower.contains("reddit") -> "Social"

            lower.contains("youtube") || lower.contains("netflix") || lower.contains("spotify") ||
            lower.contains("twitch") || lower.contains("disney") || lower.contains("primevideo") -> "Entertainment"

            lower.contains("game") || lower.contains("play") -> "Games"
            lower.contains("gmail") || lower.contains("docs") || lower.contains("slack") ||
            lower.contains("zoom") || lower.contains("notion") -> "Productivity"
            else -> "Other"
        }
    }
}


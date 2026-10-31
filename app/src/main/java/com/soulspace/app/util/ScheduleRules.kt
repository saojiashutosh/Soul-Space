package com.soulspace.app.util

import com.soulspace.app.data.local.entity.BlockScheduleEntity
import java.util.Calendar

/**
 * When is a block schedule in force? Shared by the blocker service and the screens so they always agree.
 *
 * Days are a bitmask: bit 0 = Sunday ... bit 6 = Saturday (Calendar.DAY_OF_WEEK - 1).
 * For a schedule that runs past midnight (e.g. 11 PM to 7 AM) the selected day is the day the window
 * STARTS. With Saturday unselected, Saturday night is free, while Friday night still runs until 7 AM Saturday.
 */
object ScheduleRules {

    const val ALL_DAYS = 127
    const val WEEKDAYS = 0b0111110 // Mon-Fri
    const val WEEKENDS = 0b1000001 // Sun + Sat

    private val shortNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val dayLetters = listOf("S", "M", "T", "W", "T", "F", "S")

    fun isOvernight(s: BlockScheduleEntity): Boolean =
        s.startHour * 60 + s.startMinute > s.endHour * 60 + s.endMinute

    fun isActiveAt(s: BlockScheduleEntity, now: Calendar): Boolean {
        if (!s.isActive) return false
        val nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val start = s.startHour * 60 + s.startMinute
        val end = s.endHour * 60 + s.endMinute
        val today = now.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday

        val windowDay = if (start <= end) {
            if (nowMin in start..end) today else return false
        } else {
            when {
                nowMin >= start -> today                // evening part: the window began today
                nowMin <= end -> (today + 6) % 7        // after midnight: it began yesterday
                else -> return false
            }
        }
        return s.daysOfWeekMask and (1 shl windowDay) != 0
    }

    fun daysSummary(mask: Int): String = when (mask and ALL_DAYS) {
        0 -> "No days selected"
        ALL_DAYS -> "Every day"
        WEEKDAYS -> "Weekdays"
        WEEKENDS -> "Weekends"
        else -> shortNames.filterIndexed { i, _ -> mask and (1 shl i) != 0 }.joinToString(", ")
    }
}

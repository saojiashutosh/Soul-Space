package com.soulspace.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timetable_tasks")
data class TimetableTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val dateString: String, // "yyyy-MM-dd" or "RECURRING"
    val isCompleted: Boolean = false,
    val reminderEnabled: Boolean = true,
    val category: String = "Productivity" // Routine, Work, Wellness, Rest
)


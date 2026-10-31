package com.soulspace.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Mindfulness", // Mindfulness, Health, Productivity, Sleep
    val targetDaysPerWeek: Int = 7,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
    val streakCount: Int = 0,
    val bestStreak: Int = 0,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

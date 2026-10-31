package com.soulspace.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_logs",
    indices = [Index(value = ["habitId", "dateString"], unique = true)]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val dateString: String, // "yyyy-MM-dd"
    val isCompleted: Boolean = true,
    val completedAt: Long = System.currentTimeMillis()
)


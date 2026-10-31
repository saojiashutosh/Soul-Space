package com.soulspace.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_records")
data class FocusRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val durationMinutes: Int,
    val targetMinutes: Int,
    val sessionType: String, // 25/5, 50/10, Deep Work, Custom
    val completedAt: Long = System.currentTimeMillis(),
    val wasAutoBlockEnabled: Boolean = false
)

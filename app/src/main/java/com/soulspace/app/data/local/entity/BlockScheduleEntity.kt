package com.soulspace.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "block_schedules")
data class BlockScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeekMask: Int = 127, // All 7 days active by default (bits 1..7)
    val isActive: Boolean = true,
    val isCommitmentMode: Boolean = false,
    val modeName: String = "Focus" // Study, Work, Sleep, Meditation, Custom
)

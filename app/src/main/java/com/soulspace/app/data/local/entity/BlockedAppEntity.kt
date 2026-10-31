package com.soulspace.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_apps")
data class BlockedAppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val isBlocked: Boolean = true,
    val category: String = "Distracting", // Social, Entertainment, Games, Distracting
    val blockModeMask: Int = 31 // Bitmask: 1=Study, 2=Work, 4=Sleep, 8=Meditation, 16=Custom
)

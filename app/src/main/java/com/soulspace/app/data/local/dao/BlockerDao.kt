package com.soulspace.app.data.local.dao

import androidx.room.*
import com.soulspace.app.data.local.entity.BlockedAppEntity
import com.soulspace.app.data.local.entity.BlockScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockerDao {
    @Query("SELECT * FROM blocked_apps ORDER BY appName ASC")
    fun getAllBlockedApps(): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps WHERE isBlocked = 1")
    fun getActiveBlockedApps(): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps WHERE packageName = :pkg LIMIT 1")
    suspend fun getBlockedApp(pkg: String): BlockedAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateApp(app: BlockedAppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<BlockedAppEntity>)

    @Query("UPDATE blocked_apps SET isBlocked = :blocked WHERE packageName = :pkg")
    suspend fun updateAppBlockedStatus(pkg: String, blocked: Boolean)

    @Query("DELETE FROM blocked_apps WHERE packageName = :pkg")
    suspend fun deleteApp(pkg: String)

    // Schedules
    @Query("SELECT * FROM block_schedules ORDER BY id ASC")
    fun getAllSchedules(): Flow<List<BlockScheduleEntity>>

    @Query("SELECT * FROM block_schedules WHERE isActive = 1")
    fun getActiveSchedules(): Flow<List<BlockScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: BlockScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: BlockScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: BlockScheduleEntity)
}


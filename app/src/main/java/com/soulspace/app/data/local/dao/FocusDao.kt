package com.soulspace.app.data.local.dao

import androidx.room.*
import com.soulspace.app.data.local.entity.FocusRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusDao {
    @Query("SELECT * FROM focus_records ORDER BY completedAt DESC")
    fun getAllRecords(): Flow<List<FocusRecordEntity>>

    @Query("SELECT SUM(durationMinutes) FROM focus_records")
    fun getTotalFocusMinutes(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: FocusRecordEntity): Long
}


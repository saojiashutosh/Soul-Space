package com.soulspace.app.data.local.dao

import androidx.room.*
import com.soulspace.app.data.local.entity.WaterLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_logs WHERE dateString = :dateString ORDER BY timestamp ASC")
    fun getLogsForDate(dateString: String): Flow<List<WaterLogEntity>>

    @Query("SELECT SUM(amountMl) FROM water_logs WHERE dateString = :dateString")
    fun getTotalWaterForDate(dateString: String): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun logWater(waterLog: WaterLogEntity): Long

    @Query("DELETE FROM water_logs WHERE id = (SELECT id FROM water_logs WHERE dateString = :dateString ORDER BY id DESC LIMIT 1)")
    suspend fun removeLastEntry(dateString: String)
}


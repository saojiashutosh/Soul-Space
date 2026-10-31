package com.soulspace.app.data.local.dao

import androidx.room.*
import com.soulspace.app.data.local.entity.TimetableTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable_tasks ORDER BY startHour ASC, startMinute ASC")
    fun getAllTasks(): Flow<List<TimetableTaskEntity>>

    @Query("SELECT * FROM timetable_tasks WHERE dateString = :dateString OR dateString = 'RECURRING' ORDER BY startHour ASC, startMinute ASC")
    fun getTasksForDate(dateString: String): Flow<List<TimetableTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TimetableTaskEntity): Long

    @Update
    suspend fun updateTask(task: TimetableTaskEntity)

    @Query("UPDATE timetable_tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun setTaskCompleted(id: Long, completed: Boolean)

    @Delete
    suspend fun deleteTask(task: TimetableTaskEntity)
}

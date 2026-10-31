package com.soulspace.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.soulspace.app.data.local.dao.*
import com.soulspace.app.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BlockedAppEntity::class,
        BlockScheduleEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        TimetableTaskEntity::class,
        WaterLogEntity::class,
        FocusRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SoulSpaceDatabase : RoomDatabase() {
    abstract fun blockerDao(): BlockerDao
    abstract fun habitDao(): HabitDao
    abstract fun timetableDao(): TimetableDao
    abstract fun waterDao(): WaterDao
    abstract fun focusDao(): FocusDao

    companion object {
        @Volatile
        private var INSTANCE: SoulSpaceDatabase? = null

        fun getDatabase(context: Context): SoulSpaceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SoulSpaceDatabase::class.java,
                    "soulspace_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate with mindful starter habits and a starter timetable
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getDatabase(context)
                            populateDefaultData(database)
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateDefaultData(db: SoulSpaceDatabase) {
            // Starter Habits
            val defaultHabits = listOf(
                HabitEntity(title = "Morning Meditation", description = "Sit in silence for 10 minutes", category = "Mindfulness", reminderHour = 7, reminderMinute = 0),
                HabitEntity(title = "Drink 2L Water", description = "Hydrate your body gently", category = "Health", reminderHour = 10, reminderMinute = 0),
                HabitEntity(title = "Deep Work Session", description = "Focused work without phone notifications", category = "Productivity", reminderHour = 14, reminderMinute = 0),
                HabitEntity(title = "Read a Book", description = "Read 15 pages before bed", category = "Mindfulness", reminderHour = 21, reminderMinute = 30),
                HabitEntity(title = "Screen-Free Wind Down", description = "Put phone away 45 minutes before sleep", category = "Sleep", reminderHour = 22, reminderMinute = 15)
            )
            defaultHabits.forEach { db.habitDao().insertHabit(it) }

            // Starter Timetable
            val defaultTasks = listOf(
                TimetableTaskEntity(title = "Wake Up & Hydrate", description = "Gentle morning start", startHour = 6, startMinute = 30, endHour = 7, endMinute = 0, dateString = "RECURRING", category = "Wellness"),
                TimetableTaskEntity(title = "Mindful Meditation", description = "Breathing & centering", startHour = 7, startMinute = 0, endHour = 7, endMinute = 20, dateString = "RECURRING", category = "Wellness"),
                TimetableTaskEntity(title = "Deep Work / Study Block 1", description = "Uninterrupted focus", startHour = 9, startMinute = 0, endHour = 11, endMinute = 30, dateString = "RECURRING", category = "Work"),
                TimetableTaskEntity(title = "Mindful Lunch & Walk", description = "Rest your eyes, breathe fresh air", startHour = 12, startMinute = 30, endHour = 13, endMinute = 30, dateString = "RECURRING", category = "Wellness"),
                TimetableTaskEntity(title = "Deep Work Block 2", description = "Primary deliverables", startHour = 14, startMinute = 0, endHour = 17, endMinute = 0, dateString = "RECURRING", category = "Work"),
                TimetableTaskEntity(title = "Evening Wind Down & Read", description = "No social media", startHour = 21, startMinute = 30, endHour = 22, endMinute = 30, dateString = "RECURRING", category = "Rest")
            )
            defaultTasks.forEach { db.timetableDao().insertTask(it) }

            // Default Sleep Mode Block Schedule (11:00 PM to 7:00 AM)
            db.blockerDao().insertSchedule(
                BlockScheduleEntity(
                    title = "Night Sleep Sanctuary",
                    startHour = 23,
                    startMinute = 0,
                    endHour = 7,
                    endMinute = 0,
                    daysOfWeekMask = 127,
                    isActive = true,
                    isCommitmentMode = false,
                    modeName = "Sleep"
                )
            )
        }
    }
}

package com.soulspace.app.ui.screens.habits

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soulspace.app.data.local.SoulSpaceDatabase
import com.soulspace.app.data.local.entity.HabitEntity
import com.soulspace.app.data.local.entity.HabitLogEntity
import com.soulspace.app.data.preferences.UserPreferencesRepository
import com.soulspace.app.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class HabitWithStatus(
    val habit: HabitEntity,
    val isCompletedToday: Boolean,
    val completionHistoryLast7Days: List<Boolean>
)

data class HabitsUiState(
    val habits: List<HabitWithStatus> = emptyList(),
    val totalActiveHabits: Int = 0,
    val completedTodayCount: Int = 0,
    val bestOverallStreak: Int = 0
)

class HabitsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SoulSpaceDatabase.getDatabase(application)
    private val prefs = UserPreferencesRepository(application)
    private val todayString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private val _uiState = MutableStateFlow(HabitsUiState())
    val uiState: StateFlow<HabitsUiState> = _uiState.asStateFlow()

    init {
        loadHabits()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            combine(
                db.habitDao().getAllHabits(),
                db.habitDao().getLogsForDate(todayString)
            ) { allHabits, todayLogs ->
                val todayCompletedIds = todayLogs.filter { it.isCompleted }.map { it.habitId }.toSet()

                val habitWithStatusList = allHabits.map { habit ->
                    HabitWithStatus(
                        habit = habit,
                        isCompletedToday = todayCompletedIds.contains(habit.id),
                        completionHistoryLast7Days = listOf(true, true, false, true, true, true, todayCompletedIds.contains(habit.id))
                    )
                }

                val completedCount = habitWithStatusList.count { it.isCompletedToday }
                val maxStreak = allHabits.maxOfOrNull { it.bestStreak } ?: 0

                _uiState.update {
                    it.copy(
                        habits = habitWithStatusList,
                        totalActiveHabits = allHabits.size,
                        completedTodayCount = completedCount,
                        bestOverallStreak = maxStreak
                    )
                }
            }.collect()
        }
    }

    fun toggleHabitToday(habitId: Long, isCompleted: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isCompleted) {
                db.habitDao().insertOrUpdateLog(
                    HabitLogEntity(habitId = habitId, dateString = todayString, isCompleted = true)
                )
                // Update streak in HabitEntity
                val habit = db.habitDao().getHabitById(habitId)
                if (habit != null) {
                    val newStreak = habit.streakCount + 1
                    val newBest = maxOf(newStreak, habit.bestStreak)
                    db.habitDao().updateHabit(habit.copy(streakCount = newStreak, bestStreak = newBest))
                }
                prefs.addMindfulPoints(10)
            } else {
                db.habitDao().removeLog(habitId, todayString)
                val habit = db.habitDao().getHabitById(habitId)
                if (habit != null && habit.streakCount > 0) {
                    db.habitDao().updateHabit(habit.copy(streakCount = habit.streakCount - 1))
                }
            }
        }
    }

    fun addHabit(title: String, description: String, category: String, reminderH: Int?, reminderM: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            val newId = db.habitDao().insertHabit(
                HabitEntity(
                    title = title,
                    description = description,
                    category = category,
                    reminderHour = reminderH,
                    reminderMinute = reminderM
                )
            )

            // Schedule reminder if configured
            if (reminderH != null && reminderM != null) {
                NotificationHelper.scheduleDailyExactAlarm(
                    context = getApplication(),
                    requestCode = (1000 + newId).toInt(),
                    hour = reminderH,
                    minute = reminderM,
                    title = "Habit: $title",
                    message = description.ifEmpty { "Time for your daily ritual." },
                    category = "HABIT"
                )
            }
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.habitDao().deleteHabit(habit)
        }
    }
}


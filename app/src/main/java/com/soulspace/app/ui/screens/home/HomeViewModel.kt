package com.soulspace.app.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soulspace.app.data.local.SoulSpaceDatabase
import com.soulspace.app.data.local.entity.HabitEntity
import com.soulspace.app.data.local.entity.HabitLogEntity
import com.soulspace.app.data.local.entity.TimetableTaskEntity
import com.soulspace.app.data.preferences.UserPreferences
import com.soulspace.app.data.preferences.UserPreferencesRepository
import com.soulspace.app.util.UsageStatsHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class HomeUiState(
    val greeting: String = "Good morning",
    val formattedDate: String = "",
    val quote: String = "You will never regret the moments you spent being present.",
    val nextTask: TimetableTaskEntity? = null,
    val completedHabitsCount: Int = 0,
    val totalHabitsCount: Int = 0,
    val waterIntakeMl: Int = 0,
    val waterGoalMl: Int = 2000,
    val dailyScreenTimeMinutes: Long = 0L,
    val timeSavedMinutes: Long = 45L,
    val isAppBlockerActive: Boolean = false,
    val activeBlockedAppCount: Int = 0,
    val habits: List<Pair<HabitEntity, Boolean>> = emptyList()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SoulSpaceDatabase.getDatabase(application)
    private val prefs = UserPreferencesRepository(application)
    private val todayString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        determineGreetingAndDate()
        loadDashboardData()
    }

    private fun determineGreetingAndDate() {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Peaceful night"
        }
        val dateFmt = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
        _uiState.update { it.copy(greeting = greeting, formattedDate = dateFmt) }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            // Screen Time
            val totalScreenMillis = UsageStatsHelper.getTotalDailyScreenTimeMillis(getApplication())
            val screenMinutes = totalScreenMillis / (60 * 1000)

            // Blocked Apps count
            db.blockerDao().getActiveBlockedApps().collect { blockedApps ->
                _uiState.update {
                    it.copy(
                        activeBlockedAppCount = blockedApps.size,
                        isAppBlockerActive = blockedApps.isNotEmpty(),
                        dailyScreenTimeMinutes = screenMinutes
                    )
                }
            }
        }

        viewModelScope.launch {
            // Water
            db.waterDao().getTotalWaterForDate(todayString).collect { waterTotal ->
                _uiState.update { it.copy(waterIntakeMl = waterTotal ?: 0) }
            }
        }

        viewModelScope.launch {
            // Water goal chosen on the hydration screen
            prefs.userPreferencesFlow.collect { p ->
                _uiState.update { it.copy(waterGoalMl = p.dailyWaterGoalMl) }
            }
        }

        viewModelScope.launch {
            // Next Timetable Task
            db.timetableDao().getTasksForDate(todayString).collect { tasks ->
                val upcoming = tasks.firstOrNull { !it.isCompleted }
                _uiState.update { it.copy(nextTask = upcoming) }
            }
        }

        viewModelScope.launch {
            // Habits & completion for today
            combine(
                db.habitDao().getAllHabits(),
                db.habitDao().getLogsForDate(todayString)
            ) { allHabits, logs ->
                val completedIds = logs.filter { it.isCompleted }.map { it.habitId }.toSet()
                val habitList = allHabits.map { habit ->
                    Pair(habit, completedIds.contains(habit.id))
                }
                val completedCount = habitList.count { it.second }
                _uiState.update {
                    it.copy(
                        habits = habitList,
                        completedHabitsCount = completedCount,
                        totalHabitsCount = allHabits.size
                    )
                }
            }.collect()
        }
    }

    fun toggleHabit(habitId: Long, isDone: Boolean) {
        viewModelScope.launch {
            if (isDone) {
                db.habitDao().insertOrUpdateLog(
                    HabitLogEntity(habitId = habitId, dateString = todayString, isCompleted = true)
                )
                prefs.addMindfulPoints(10)
            } else {
                db.habitDao().removeLog(habitId, todayString)
            }
        }
    }

    fun quickAddWater(amountMl: Int) {
        viewModelScope.launch {
            db.waterDao().logWater(
                com.soulspace.app.data.local.entity.WaterLogEntity(
                    dateString = todayString,
                    amountMl = amountMl
                )
            )
            prefs.addMindfulPoints(5)
        }
    }
}


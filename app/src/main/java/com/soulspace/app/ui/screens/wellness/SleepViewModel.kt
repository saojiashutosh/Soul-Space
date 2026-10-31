package com.soulspace.app.ui.screens.wellness

import kotlinx.coroutines.Dispatchers
import com.soulspace.app.util.ScheduleRules
import com.soulspace.app.data.local.entity.BlockScheduleEntity
import com.soulspace.app.data.local.SoulSpaceDatabase
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soulspace.app.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SleepUiState(
    val bedtimeHour: Int = 23,
    val bedtimeMinute: Int = 0,
    val wakeHour: Int = 7,
    val wakeMinute: Int = 0,
    /** The overnight blocking schedule (mode "Sleep"), or null until the user first uses it. */
    val schedule: BlockScheduleEntity? = null,
    val restrictedAppCount: Int = 0,
    val checklist: List<Pair<String, Boolean>> = listOf(
        "Put phone away outside bedroom" to true,
        "Dim warm room lighting" to true,
        "5 minutes soothing breath pacer" to false,
        "Listen to gentle night soundscape" to false
    )
)

class SleepViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = UserPreferencesRepository(application)
    private val db = SoulSpaceDatabase.getDatabase(application)
    private val _uiState = MutableStateFlow(SleepUiState())
    val uiState: StateFlow<SleepUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.userPreferencesFlow.collect { p ->
                _uiState.update {
                    it.copy(
                        bedtimeHour = p.bedtimeHour,
                        bedtimeMinute = p.bedtimeMinute,
                        wakeHour = p.wakeHour,
                        wakeMinute = p.wakeMinute
                    )
                }
            }
        }
        viewModelScope.launch {
            db.blockerDao().getAllSchedules().collect { list ->
                _uiState.update { it.copy(schedule = list.firstOrNull { s -> s.modeName == SLEEP_MODE }) }
            }
        }
        viewModelScope.launch {
            db.blockerDao().getActiveBlockedApps().collect { apps ->
                _uiState.update { it.copy(restrictedAppCount = apps.size) }
            }
        }
    }

    /** Turns overnight blocking on. Apps are blocked between bedtime and wake time on the chosen days. */
    fun start() = saveSchedule(active = true)

    fun stop() = saveSchedule(active = false)

    fun setDays(mask: Int) = saveSchedule(active = _uiState.value.schedule?.isActive ?: false, daysMask = mask)

    fun setBedtime(hour: Int, minute: Int) = changeSleepTimes(hour, minute, _uiState.value.wakeHour, _uiState.value.wakeMinute)

    fun setWakeTime(hour: Int, minute: Int) = changeSleepTimes(_uiState.value.bedtimeHour, _uiState.value.bedtimeMinute, hour, minute)

    private fun changeSleepTimes(bedH: Int, bedM: Int, wakeH: Int, wakeM: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setSleepSchedule(bedH, bedM, wakeH, wakeM)
            _uiState.value.schedule?.let { db.blockerDao().updateSchedule(it.copy(startHour = bedH, startMinute = bedM, endHour = wakeH, endMinute = wakeM)) }
        }
    }

    private fun saveSchedule(active: Boolean, daysMask: Int? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value
            val existing = state.schedule
            val mask = daysMask ?: existing?.daysOfWeekMask ?: ScheduleRules.ALL_DAYS
            if (existing == null) {
                db.blockerDao().insertSchedule(
                    BlockScheduleEntity(
                        title = "Overnight Restrictions",
                        startHour = state.bedtimeHour,
                        startMinute = state.bedtimeMinute,
                        endHour = state.wakeHour,
                        endMinute = state.wakeMinute,
                        daysOfWeekMask = mask,
                        isActive = active,
                        modeName = SLEEP_MODE
                    )
                )
            } else {
                db.blockerDao().updateSchedule(
                    existing.copy(
                        startHour = state.bedtimeHour,
                        startMinute = state.bedtimeMinute,
                        endHour = state.wakeHour,
                        endMinute = state.wakeMinute,
                        daysOfWeekMask = mask,
                        isActive = active
                    )
                )
            }
        }
    }

    companion object {
        const val SLEEP_MODE = "Sleep"
    }

    fun toggleChecklistItem(index: Int) {
        val current = _uiState.value.checklist.toMutableList()
        val item = current[index]
        current[index] = item.copy(second = !item.second)
        _uiState.update { it.copy(checklist = current) }
    }
}


package com.soulspace.app.ui.screens.wellness

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
    val checklist: List<Pair<String, Boolean>> = listOf(
        "Put phone away outside bedroom" to true,
        "Dim warm room lighting" to true,
        "5 minutes soothing breath pacer" to false,
        "Listen to gentle night soundscape" to false
    )
)

class SleepViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = UserPreferencesRepository(application)
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
    }

    fun toggleChecklistItem(index: Int) {
        val current = _uiState.value.checklist.toMutableList()
        val item = current[index]
        current[index] = item.copy(second = !item.second)
        _uiState.update { it.copy(checklist = current) }
    }
}

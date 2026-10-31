package com.soulspace.app.ui.screens.focus

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soulspace.app.data.local.SoulSpaceDatabase
import com.soulspace.app.data.local.entity.FocusRecordEntity
import com.soulspace.app.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FocusUiState(
    val totalSeconds: Int = 25 * 60,
    val secondsRemaining: Int = 25 * 60,
    val isRunning: Boolean = false,
    val isBreak: Boolean = false,
    val currentPreset: String = "25/5 Standard",
    val autoBlockEnabled: Boolean = true,
    val totalFocusMinutesAllTime: Int = 0,
    val completedSessionsCount: Int = 0
)

class FocusViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SoulSpaceDatabase.getDatabase(application)
    private val prefs = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            db.focusDao().getTotalFocusMinutes().collect { totalMins ->
                _uiState.update { it.copy(totalFocusMinutesAllTime = totalMins ?: 0) }
            }
        }
        viewModelScope.launch {
            db.focusDao().getAllRecords().collect { records ->
                _uiState.update { it.copy(completedSessionsCount = records.size) }
            }
        }
    }

    fun selectPreset(presetName: String, focusMinutes: Int) {
        pauseTimer()
        val totalSecs = focusMinutes * 60
        _uiState.update {
            it.copy(
                currentPreset = presetName,
                totalSeconds = totalSecs,
                secondsRemaining = totalSecs,
                isBreak = false
            )
        }
    }

    fun toggleAutoBlock(enabled: Boolean) {
        _uiState.update { it.copy(autoBlockEnabled = enabled) }
    }

    fun startTimer() {
        if (_uiState.value.isRunning) return

        _uiState.update { it.copy(isRunning = true) }

        if (_uiState.value.autoBlockEnabled && !_uiState.value.isBreak) {
            val endMillis = System.currentTimeMillis() + (_uiState.value.secondsRemaining * 1000L)
            viewModelScope.launch {
                prefs.setTemporaryBlock(endMillis, "Active Focus Session")
            }
        }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.secondsRemaining > 0 && _uiState.value.isRunning) {
                delay(1000)
                _uiState.update { it.copy(secondsRemaining = it.secondsRemaining - 1) }
            }

            if (_uiState.value.secondsRemaining == 0) {
                onSessionCompleted()
            }
        }
    }

    fun pauseTimer() {
        _uiState.update { it.copy(isRunning = false) }
        timerJob?.cancel()
        viewModelScope.launch {
            prefs.clearTemporaryBlock()
        }
    }

    fun resetTimer() {
        pauseTimer()
        _uiState.update { it.copy(secondsRemaining = it.totalSeconds) }
    }

    private fun onSessionCompleted() {
        val completedState = _uiState.value
        _uiState.update { it.copy(isRunning = false) }

        viewModelScope.launch {
            prefs.clearTemporaryBlock()
            val focusedMinutes = completedState.totalSeconds / 60
            db.focusDao().insertRecord(
                FocusRecordEntity(
                    durationMinutes = focusedMinutes,
                    targetMinutes = focusedMinutes,
                    sessionType = completedState.currentPreset,
                    wasAutoBlockEnabled = completedState.autoBlockEnabled
                )
            )
            prefs.addMindfulPoints(25)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

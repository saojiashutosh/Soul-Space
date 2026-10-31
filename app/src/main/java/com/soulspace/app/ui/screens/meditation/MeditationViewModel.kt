package com.soulspace.app.ui.screens.meditation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soulspace.app.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MeditationSession(
    val id: String,
    val title: String,
    val description: String,
    val durationMinutes: Int,
    val category: String, // Morning, Stress, Evening, Focus
    val themeColorHex: Long
)

data class MeditationUiState(
    val sessions: List<MeditationSession> = emptyList(),
    val totalMindfulMinutes: Int = 45,
    val activeBreathingPattern: String = "4-4-6 Relax",
    val breathingPhase: String = "Inhale",
    val breathingSecondsLeftInPhase: Int = 4,
    val isBreathingActive: Boolean = false
)

class MeditationViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = UserPreferencesRepository(application)
    private val _uiState = MutableStateFlow(MeditationUiState())
    val uiState: StateFlow<MeditationUiState> = _uiState.asStateFlow()

    private var breathingJob: Job? = null

    init {
        loadSessions()
    }

    private fun loadSessions() {
        val list = listOf(
            MeditationSession(
                id = "m1",
                title = "Morning Centering",
                description = "Awaken gently and set calm intention for the day",
                durationMinutes = 5,
                category = "Morning",
                themeColorHex = 0xFF8A9A86
            ),
            MeditationSession(
                id = "m2",
                title = "Stress Release & Reset",
                description = "Release somatic tension from shoulders and chest",
                durationMinutes = 10,
                category = "Stress",
                themeColorHex = 0xFFC77A62
            ),
            MeditationSession(
                id = "m3",
                title = "Evening Wind-Down",
                description = "Prepare nervous system for deep restorative sleep",
                durationMinutes = 15,
                category = "Evening",
                themeColorHex = 0xFF536751
            ),
            MeditationSession(
                id = "m4",
                title = "Silent Zen Sit",
                description = "Unstructured stillness with gentle Tibetan bowl chimes",
                durationMinutes = 20,
                category = "Focus",
                themeColorHex = 0xFF67735E
            )
        )
        _uiState.update { it.copy(sessions = list) }
    }

    fun startBreathingExercise(pattern: String = "4-4-6 Relax") {
        _uiState.update { it.copy(isBreathingActive = true, activeBreathingPattern = pattern) }
        breathingJob?.cancel()
        breathingJob = viewModelScope.launch {
            val (inhaleTime, holdTime, exhaleTime) = when (pattern) {
                "4-4-4-4 Box" -> Triple(4, 4, 4)
                "4-7-8 Deep Sleep" -> Triple(4, 7, 8)
                else -> Triple(4, 4, 6)
            }

            while (_uiState.value.isBreathingActive) {
                // Inhale
                _uiState.update { it.copy(breathingPhase = "Inhale", breathingSecondsLeftInPhase = inhaleTime) }
                for (s in inhaleTime downTo 1) {
                    _uiState.update { it.copy(breathingSecondsLeftInPhase = s) }
                    delay(1000)
                }

                // Hold
                _uiState.update { it.copy(breathingPhase = "Hold", breathingSecondsLeftInPhase = holdTime) }
                for (s in holdTime downTo 1) {
                    _uiState.update { it.copy(breathingSecondsLeftInPhase = s) }
                    delay(1000)
                }

                // Exhale
                _uiState.update { it.copy(breathingPhase = "Exhale", breathingSecondsLeftInPhase = exhaleTime) }
                for (s in exhaleTime downTo 1) {
                    _uiState.update { it.copy(breathingSecondsLeftInPhase = s) }
                    delay(1000)
                }
            }
        }
    }

    fun stopBreathingExercise() {
        _uiState.update { it.copy(isBreathingActive = false) }
        breathingJob?.cancel()
        viewModelScope.launch {
            prefs.addMindfulPoints(15)
        }
    }

    override fun onCleared() {
        super.onCleared()
        breathingJob?.cancel()
    }
}

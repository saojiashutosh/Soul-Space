package com.soulspace.app.ui.screens.wellness

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soulspace.app.data.local.SoulSpaceDatabase
import com.soulspace.app.data.local.entity.WaterLogEntity
import com.soulspace.app.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class WaterUiState(
    val currentIntakeMl: Int = 0,
    val targetGoalMl: Int = 2000,
    val logs: List<WaterLogEntity> = emptyList()
)

class WaterViewModel(application: Application) : AndroidViewModel(application) {
    private val db = SoulSpaceDatabase.getDatabase(application)
    private val prefs = UserPreferencesRepository(application)
    private val todayString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private val _uiState = MutableStateFlow(WaterUiState())
    val uiState: StateFlow<WaterUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.userPreferencesFlow.collect { p ->
                _uiState.update { it.copy(targetGoalMl = p.dailyWaterGoalMl) }
            }
        }
        viewModelScope.launch {
            db.waterDao().getTotalWaterForDate(todayString).collect { total ->
                _uiState.update { it.copy(currentIntakeMl = total ?: 0) }
            }
        }
        viewModelScope.launch {
            db.waterDao().getLogsForDate(todayString).collect { list ->
                _uiState.update { it.copy(logs = list.reversed()) }
            }
        }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            db.waterDao().logWater(WaterLogEntity(dateString = todayString, amountMl = amountMl))
            prefs.addMindfulPoints(5)
        }
    }

    fun undoLast() {
        viewModelScope.launch(Dispatchers.IO) {
            db.waterDao().removeLastEntry(todayString)
        }
    }
}

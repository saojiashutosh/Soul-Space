package com.soulspace.app.ui.screens.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soulspace.app.util.AppUsageInfo
import com.soulspace.app.util.PermissionManager
import com.soulspace.app.util.UsageStatsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AnalyticsUiState(
    val isPermissionGranted: Boolean = false,
    val totalScreenTimeMinutes: Long = 0L,
    val timeSavedMinutes: Long = 52L,
    val topApps: List<AppUsageInfo> = emptyList(),
    val weeklyHoursList: List<Float> = listOf(3.2f, 4.1f, 2.8f, 3.5f, 2.1f, 1.8f, 2.5f), // Mon..Sun
    val socialMinutes: Long = 0L,
    val entertainmentMinutes: Long = 0L,
    val productivityMinutes: Long = 0L
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        refreshAnalytics()
    }

    fun refreshAnalytics() {
        val app = getApplication<Application>()
        val granted = PermissionManager.isUsageStatsPermissionGranted(app)
        _uiState.update { it.copy(isPermissionGranted = granted) }

        if (granted) {
            viewModelScope.launch {
                val appUsage = withContext(Dispatchers.IO) {
                    UsageStatsHelper.getDailyAppUsage(app)
                }
                val totalMillis = appUsage.sumOf { it.totalTimeInForegroundMillis }
                val totalMins = totalMillis / (60 * 1000)

                val social = appUsage.filter { it.category == "Social" }.sumOf { it.totalTimeInForegroundMillis } / (60 * 1000)
                val ent = appUsage.filter { it.category == "Entertainment" }.sumOf { it.totalTimeInForegroundMillis } / (60 * 1000)
                val prod = appUsage.filter { it.category == "Productivity" }.sumOf { it.totalTimeInForegroundMillis } / (60 * 1000)

                _uiState.update {
                    it.copy(
                        totalScreenTimeMinutes = totalMins,
                        topApps = appUsage.take(8),
                        socialMinutes = social,
                        entertainmentMinutes = ent,
                        productivityMinutes = prod
                    )
                }
            }
        }
    }
}

package com.soulspace.app.ui.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soulspace.app.data.local.SoulSpaceDatabase
import com.soulspace.app.data.preferences.UserPreferencesRepository
import com.soulspace.app.util.PermissionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean
)

data class ProfileUiState(
    val totalPoints: Int = 120,
    val totalFocusHours: Float = 4.5f,
    val totalCheckIns: Int = 18,
    val isDarkTheme: Boolean = false,
    val isAccessibilityGranted: Boolean = false,
    val isUsageStatsGranted: Boolean = false,
    val badges: List<AchievementBadge> = emptyList()
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SoulSpaceDatabase.getDatabase(application)
    private val prefs = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    fun checkPermissions() {
        val app = getApplication<Application>()
        _uiState.update {
            it.copy(
                isAccessibilityGranted = PermissionManager.isAccessibilityServiceEnabled(app),
                isUsageStatsGranted = PermissionManager.isUsageStatsPermissionGranted(app)
            )
        }
    }

    private fun loadProfileData() {
        checkPermissions()
        viewModelScope.launch {
            prefs.userPreferencesFlow.collect { p ->
                _uiState.update {
                    it.copy(
                        totalPoints = p.totalMindfulPoints,
                        isDarkTheme = p.darkTheme
                    )
                }
            }
        }

        val badgeList = listOf(
            AchievementBadge("b1", "Digital Sovereign", "Completed a strict 4-hour commitment session", true),
            AchievementBadge("b2", "Still Water", "Achieved 2,000ml hydration for 3 consecutive days", true),
            AchievementBadge("b3", "Dawn Walker", "Completed morning meditation before 7:30 AM", true),
            AchievementBadge("b4", "Zen Master", "Reached 100 mindful focus sessions", false)
        )
        _uiState.update { it.copy(badges = badgeList) }
    }

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            prefs.setDarkTheme(isDark)
        }
    }

    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            db.clearAllTables()
        }
    }
}

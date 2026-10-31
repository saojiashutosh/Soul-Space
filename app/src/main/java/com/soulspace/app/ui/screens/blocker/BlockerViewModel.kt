package com.soulspace.app.ui.screens.blocker

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soulspace.app.data.local.SoulSpaceDatabase
import com.soulspace.app.data.local.entity.BlockScheduleEntity
import com.soulspace.app.data.local.entity.BlockedAppEntity
import com.soulspace.app.data.preferences.UserPreferencesRepository
import com.soulspace.app.util.PermissionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val isBlocked: Boolean = false,
    val category: String = "Distracting"
)

data class BlockerUiState(
    val installedApps: List<InstalledAppItem> = emptyList(),
    val filteredApps: List<InstalledAppItem> = emptyList(),
    val searchQuery: String = "",
    val schedules: List<BlockScheduleEntity> = emptyList(),
    val isAccessibilityGranted: Boolean = false,
    val isUsageStatsGranted: Boolean = false,
    val isCommitmentActive: Boolean = false,
    val commitmentEndMillis: Long = 0L,
    val temporaryBlockEndMillis: Long = 0L,
    val selectedMode: String = "Study", // Study, Work, Sleep, Meditation, Custom
    val isLoadingApps: Boolean = true
)

class BlockerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SoulSpaceDatabase.getDatabase(application)
    private val prefs = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(BlockerUiState())
    val uiState: StateFlow<BlockerUiState> = _uiState.asStateFlow()

    init {
        checkPermissions()
        observePreferencesAndSchedules()
        loadInstalledApps()
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

    private fun observePreferencesAndSchedules() {
        viewModelScope.launch {
            prefs.userPreferencesFlow.collect { userPrefs ->
                _uiState.update {
                    it.copy(
                        isCommitmentActive = userPrefs.isCommitmentModeActive,
                        commitmentEndMillis = userPrefs.commitmentEndTimeMillis,
                        temporaryBlockEndMillis = userPrefs.temporaryBlockEndTimeMillis
                    )
                }
            }
        }

        viewModelScope.launch {
            db.blockerDao().getAllSchedules().collect { scheduleList ->
                _uiState.update { it.copy(schedules = scheduleList) }
            }
        }
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingApps = true) }
            val apps = withContext(Dispatchers.IO) {
                val pm = getApplication<Application>().packageManager
                val intent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos = pm.queryIntentActivities(intent, 0)
                val blockedFromDb = db.blockerDao().getAllBlockedApps().first().associateBy { it.packageName }

                resolveInfos.mapNotNull { resolveInfo ->
                    val pkg = resolveInfo.activityInfo.packageName
                    if (pkg == getApplication<Application>().packageName) return@mapNotNull null

                    val label = resolveInfo.loadLabel(pm).toString()
                    val isBlocked = blockedFromDb[pkg]?.isBlocked ?: false
                    val category = blockedFromDb[pkg]?.category ?: "Distracting"

                    InstalledAppItem(
                        packageName = pkg,
                        appName = label,
                        isBlocked = isBlocked,
                        category = category
                    )
                }.sortedBy { it.appName.lowercase() }
            }

            _uiState.update {
                it.copy(
                    installedApps = apps,
                    filteredApps = filterAppList(apps, it.searchQuery),
                    isLoadingApps = false
                )
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredApps = filterAppList(it.installedApps, query)
            )
        }
    }

    private fun filterAppList(apps: List<InstalledAppItem>, query: String): List<InstalledAppItem> {
        if (query.isBlank()) return apps
        return apps.filter {
            it.appName.contains(query, ignoreCase = true) ||
            it.packageName.contains(query, ignoreCase = true)
        }
    }

    fun toggleAppBlocked(pkg: String, appName: String, isBlocked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            db.blockerDao().insertOrUpdateApp(
                BlockedAppEntity(
                    packageName = pkg,
                    appName = appName,
                    isBlocked = isBlocked
                )
            )
            // Update local state
            val updated = _uiState.value.installedApps.map {
                if (it.packageName == pkg) it.copy(isBlocked = isBlocked) else it
            }
            _uiState.update {
                it.copy(
                    installedApps = updated,
                    filteredApps = filterAppList(updated, it.searchQuery)
                )
            }
        }
    }

    fun startTemporaryBlock(durationMinutes: Int, label: String) {
        viewModelScope.launch {
            val endTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
            prefs.setTemporaryBlock(endTime, label)
        }
    }

    fun clearTemporaryBlock() {
        viewModelScope.launch {
            prefs.clearTemporaryBlock()
        }
    }

    fun activateCommitmentMode(durationHours: Int) {
        viewModelScope.launch {
            val endTime = System.currentTimeMillis() + (durationHours * 3600 * 1000L)
            prefs.setCommitmentMode(true, endTime)
        }
    }

    fun addSchedule(title: String, startH: Int, startM: Int, endH: Int, endM: Int, mode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.blockerDao().insertSchedule(
                BlockScheduleEntity(
                    title = title,
                    startHour = startH,
                    startMinute = startM,
                    endHour = endH,
                    endMinute = endM,
                    modeName = mode
                )
            )
        }
    }

    fun toggleScheduleActive(schedule: BlockScheduleEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.blockerDao().updateSchedule(schedule.copy(isActive = !schedule.isActive))
        }
    }

    fun deleteSchedule(schedule: BlockScheduleEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.blockerDao().deleteSchedule(schedule)
        }
    }
}

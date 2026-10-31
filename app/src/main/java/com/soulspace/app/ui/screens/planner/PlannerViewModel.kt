package com.soulspace.app.ui.screens.planner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soulspace.app.data.local.SoulSpaceDatabase
import com.soulspace.app.data.local.entity.TimetableTaskEntity
import com.soulspace.app.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class PlannerUiState(
    val tasks: List<TimetableTaskEntity> = emptyList(),
    val todayDateFormatted: String = "",
    val completedCount: Int = 0
)

class PlannerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SoulSpaceDatabase.getDatabase(application)
    private val todayString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private val _uiState = MutableStateFlow(PlannerUiState())
    val uiState: StateFlow<PlannerUiState> = _uiState.asStateFlow()

    init {
        val dateDisplay = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
        _uiState.update { it.copy(todayDateFormatted = dateDisplay) }
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            db.timetableDao().getTasksForDate(todayString).collect { taskList ->
                _uiState.update {
                    it.copy(
                        tasks = taskList,
                        completedCount = taskList.count { t -> t.isCompleted }
                    )
                }
            }
        }
    }

    fun toggleTask(id: Long, completed: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            db.timetableDao().setTaskCompleted(id, completed)
        }
    }

    fun addTask(title: String, desc: String, sH: Int, sM: Int, eH: Int, eM: Int, cat: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val taskId = db.timetableDao().insertTask(
                TimetableTaskEntity(
                    title = title,
                    description = desc,
                    startHour = sH,
                    startMinute = sM,
                    endHour = eH,
                    endMinute = eM,
                    dateString = "RECURRING",
                    category = cat
                )
            )

            NotificationHelper.scheduleDailyExactAlarm(
                context = getApplication(),
                requestCode = (2000 + taskId).toInt(),
                hour = sH,
                minute = sM,
                title = "Timetable: $title",
                message = desc.ifEmpty { "Activity starting now." },
                category = "TIMETABLE"
            )
        }
    }

    fun deleteTask(task: TimetableTaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.timetableDao().deleteTask(task)
        }
    }
}


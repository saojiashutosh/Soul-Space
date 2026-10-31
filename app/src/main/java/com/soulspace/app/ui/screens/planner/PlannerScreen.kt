package com.soulspace.app.ui.screens.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulspace.app.data.local.entity.TimetableTaskEntity
import com.soulspace.app.ui.theme.*

@Composable
fun PlannerScreen(
    onBack: () -> Unit = {},
    viewModel: PlannerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = SanctuaryCream,
        topBar = {
            TopAppBar(
                title = { Text("Daily Timetable", fontFamily = FontFamily.Serif, color = CharcoalDeep) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = CharcoalDeep)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add", tint = SagePrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SanctuaryCream)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            // Sub-header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = uiState.todayDateFormatted.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = SageDark,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${uiState.completedCount} of ${uiState.tasks.size} Completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CharcoalMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timeline Items
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(uiState.tasks, key = { it.id }) { task ->
                    TimetableCard(
                        task = task,
                        onToggle = { isChecked -> viewModel.toggleTask(task.id, isChecked) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddTimetableDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { title, desc, sH, sM, eH, eM, cat ->
                    viewModel.addTask(title, desc, sH, sM, eH, eM, cat)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun TimetableCard(
    task: TimetableTaskEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val timeLabel = String.format("%02d:%02d — %02d:%02d", task.startHour, task.startMinute, task.endHour, task.endMinute)

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) SageLight.copy(alpha = 0.35f) else SanctuarySurface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggle(it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = SagePrimary,
                        uncheckedColor = CharcoalMuted
                    )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = SageDark,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = task.title,
                        fontFamily = FontFamily.Serif,
                        fontSize = 17.sp,
                        color = CharcoalDeep
                    )
                    if (task.description.isNotEmpty()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalMuted
                        )
                    }
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = CharcoalMuted)
            }
        }
    }
}

@Composable
fun AddTimetableDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Int, Int, Int, Int, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var startHour by remember { mutableIntStateOf(10) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(11) }
    var endMinute by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SanctuarySurface,
        title = { Text("Add Scheduled Activity", fontFamily = FontFamily.Serif) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Activity Title (e.g. Study Physics)") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Details / Notes") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title, desc, startHour, startMinute, endHour, endMinute, "Work")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
            ) {
                Text("Schedule", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CharcoalMuted)
            }
        }
    )
}


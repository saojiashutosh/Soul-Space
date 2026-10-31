package com.soulspace.app.ui.screens.habits

import com.soulspace.app.ui.components.EmptyHint
import com.soulspace.app.ui.components.soulTextFieldColors
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
import com.soulspace.app.ui.theme.*

@Composable
fun HabitsScreen(
    viewModel: HabitsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SanctuaryCream)
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "HABIT SANCTUARY",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 2.sp,
                    color = SageDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Mindful Rituals",
                    fontFamily = FontFamily.Serif,
                    fontSize = 26.sp,
                    color = CharcoalDeep
                )
            }

            Button(
                onClick = { showAddDialog = true },
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaClay, contentColor = Color.White)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Ritual", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Streak Progress Summary Card
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today's Consistency",
                        style = MaterialTheme.typography.labelMedium,
                        color = CharcoalMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${uiState.completedTodayCount} of ${uiState.totalActiveHabits} Done",
                        fontFamily = FontFamily.Serif,
                        fontSize = 24.sp,
                        color = CharcoalDeep
                    )
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SageLight.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Whatshot, contentDescription = null, tint = TerracottaClay, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${uiState.bestOverallStreak} Day Record",
                            style = MaterialTheme.typography.labelMedium,
                            color = CharcoalDeep
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Habit List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            if (uiState.habits.isEmpty()) {
                item {
                    EmptyHint(
                        title = "No rituals yet",
                        body = "Tap \u201cNew Ritual\u201d to add your first mindful habit."
                    )
                }
            }
            items(uiState.habits, key = { it.habit.id }) { item ->
                HabitCardItem(
                    item = item,
                    onToggle = { isDone -> viewModel.toggleHabitToday(item.habit.id, isDone) },
                    onDelete = { viewModel.deleteHabit(item.habit) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, desc, cat, h, m ->
                viewModel.addHabit(title, desc, cat, h, m)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun HabitCardItem(
    item: HabitWithStatus,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompletedToday) SageLight.copy(alpha = 0.35f) else SanctuarySurface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = item.isCompletedToday,
                        onCheckedChange = { onToggle(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = SagePrimary,
                            uncheckedColor = CharcoalMuted
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = item.habit.title,
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = CharcoalDeep
                        )
                        if (item.habit.description.isNotEmpty()) {
                            Text(
                                text = item.habit.description,
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

            Spacer(modifier = Modifier.height(12.dp))

            // Dot Matrix History
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item.completionHistoryLast7Days.forEach { done ->
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (done) SagePrimary else SanctuarySurfaceVariant)
                        )
                    }
                }

                Text(
                    text = "${item.habit.streakCount} streak",
                    style = MaterialTheme.typography.labelSmall,
                    color = SageDark
                )
            }
        }
    }
}

@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, Int?, Int?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Mindfulness") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SanctuarySurface,
        titleContentColor = CharcoalDeep,
        textContentColor = CharcoalMuted,
        title = { Text("New Mindful Ritual", fontFamily = FontFamily.Serif) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Ritual Title (e.g. Read 15 mins)") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = soulTextFieldColors()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Intention / Note") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = soulTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title, description, category, null, null)
                    }
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = SagePrimary, contentColor = Color.White)
            ) {
                Text("Create Ritual", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CharcoalMuted)
            }
        }
    )
}


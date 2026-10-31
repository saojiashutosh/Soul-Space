package com.soulspace.app.ui.screens.wellness

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulspace.app.data.preferences.UserPreferencesRepository
import com.soulspace.app.ui.theme.*
import kotlinx.coroutines.flow.*
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

@Composable
fun SleepScreen(
    onBack: () -> Unit = {},
    onNavigateToBlocker: () -> Unit = {},
    viewModel: SleepViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = SanctuaryCream,
        topBar = {
            TopAppBar(
                title = { Text("Sleep Sanctuary", fontFamily = FontFamily.Serif, color = CharcoalDeep) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = CharcoalDeep)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SanctuaryCream)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Circadian Schedule Card
            item {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Bedtime, contentDescription = null, tint = SageDark)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Circadian Rhythm Target",
                                style = MaterialTheme.typography.titleMedium,
                                color = CharcoalDeep
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("BEDTIME", style = MaterialTheme.typography.labelSmall, color = SageDark)
                                Text(
                                    text = String.format("%02d:%02d", uiState.bedtimeHour, uiState.bedtimeMinute),
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 28.sp,
                                    color = CharcoalDeep
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("WAKE UP", style = MaterialTheme.typography.labelSmall, color = SageDark)
                                Text(
                                    text = String.format("%02d:%02d", uiState.wakeHour, uiState.wakeMinute),
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 28.sp,
                                    color = CharcoalDeep
                                )
                            }
                        }
                    }
                }
            }

            // Night App Blocker Link
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SageLight.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.ShieldMoon, contentDescription = null, tint = SageDark)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Overnight App Restrictions", style = MaterialTheme.typography.titleSmall, color = CharcoalDeep)
                                Text("Active 11:00 PM — 7:00 AM", style = MaterialTheme.typography.bodySmall, color = CharcoalMuted)
                            }
                        }
                        Button(
                            onClick = onNavigateToBlocker,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SageDark)
                        ) {
                            Text("Manage", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Wind-down Rituals Checklist
            item {
                Text(
                    text = "EVENING WIND-DOWN CHECKLIST",
                    style = MaterialTheme.typography.labelSmall,
                    color = SageDark,
                    letterSpacing = 1.sp
                )
            }

            items(uiState.checklist.size) { index ->
                val (itemTitle, isDone) = uiState.checklist[index]
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isDone,
                            onCheckedChange = { viewModel.toggleChecklistItem(index) },
                            colors = CheckboxDefaults.colors(checkedColor = SagePrimary)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = itemTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDone) CharcoalMuted else CharcoalDeep
                        )
                    }
                }
            }
        }
    }
}

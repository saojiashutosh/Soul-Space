package com.soulspace.app.ui.screens.wellness

import android.app.Application
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulspace.app.data.local.SoulSpaceDatabase
import com.soulspace.app.data.local.entity.WaterLogEntity
import com.soulspace.app.data.preferences.UserPreferencesRepository
import com.soulspace.app.ui.theme.*
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

@Composable
fun WaterTrackerScreen(
    onBack: () -> Unit = {},
    viewModel: WaterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val progress = (uiState.currentIntakeMl.toFloat() / uiState.targetGoalMl.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600),
        label = "waterProgress"
    )

    Scaffold(
        containerColor = SanctuaryCream,
        topBar = {
            TopAppBar(
                title = { Text("Mindful Hydration", fontFamily = FontFamily.Serif, color = CharcoalDeep) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = CharcoalDeep)
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Animated Visual Water Pillar / Vessel
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(200.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Glass Vessel outline
                    drawRoundRect(
                        color = SanctuarySurfaceVariant,
                        size = Size(w, h),
                        cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx())
                    )

                    // Filled water level
                    val fillHeight = h * animatedProgress
                    drawRoundRect(
                        color = SagePrimary,
                        topLeft = Offset(0f, h - fillHeight),
                        size = Size(w, fillHeight),
                        cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx())
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${uiState.currentIntakeMl} ml",
                        fontFamily = FontFamily.Serif,
                        fontSize = 32.sp,
                        color = if (animatedProgress > 0.5f) Color.White else CharcoalDeep
                    )
                    Text(
                        text = "Goal: ${uiState.targetGoalMl} ml",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (animatedProgress > 0.5f) Color.White.copy(alpha = 0.8f) else CharcoalMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Add Chips (+250ml, +500ml, +750ml)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(250 to "Glass (250ml)", 500 to "Bottle (500ml)", 750 to "Flask (750ml)").forEach { (amt, label) ->
                    Button(
                        onClick = { viewModel.addWater(amt) },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SanctuarySurface),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("+$amt", fontFamily = FontFamily.Serif, fontSize = 16.sp, color = SageDark)
                            Text("ml", fontSize = 10.sp, color = CharcoalMuted)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S INTAKE LOG",
                    style = MaterialTheme.typography.labelSmall,
                    color = SageDark,
                    letterSpacing = 1.sp
                )
                TextButton(onClick = { viewModel.undoLast() }) {
                    Text("Undo Last", style = MaterialTheme.typography.labelSmall, color = CharcoalMuted)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(uiState.logs) { log ->
                    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(log.timestamp))
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.WaterDrop, contentDescription = null, tint = SagePrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "+${log.amountMl} ml", style = MaterialTheme.typography.bodyLarge, color = CharcoalDeep)
                            }
                            Text(text = timeStr, style = MaterialTheme.typography.labelSmall, color = CharcoalMuted)
                        }
                    }
                }
            }
        }
    }
}

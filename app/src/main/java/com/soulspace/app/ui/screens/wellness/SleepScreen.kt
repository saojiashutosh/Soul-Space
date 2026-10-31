package com.soulspace.app.ui.screens.wellness

import java.util.Locale
import java.util.Calendar
import com.soulspace.app.util.ScheduleRules
import com.soulspace.app.util.PermissionManager
import com.soulspace.app.ui.components.DaySelector
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import android.content.Context
import android.app.TimePickerDialog
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.CircleShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulspace.app.ui.theme.*

@Composable
fun SleepScreen(
    onBack: () -> Unit = {},
    onNavigateToBlocker: () -> Unit = {},
    viewModel: SleepViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Re-check accessibility when coming back from the system settings screen
    var accessibilityOn by remember { mutableStateOf(PermissionManager.isAccessibilityServiceEnabled(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) accessibilityOn = PermissionManager.isAccessibilityServiceEnabled(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable {
                                    pickTime(context, uiState.bedtimeHour, uiState.bedtimeMinute, viewModel::setBedtime)
                                }
                            ) {
                                Text("BEDTIME", style = MaterialTheme.typography.labelSmall, color = SageDark)
                                Text(
                                    text = String.format("%02d:%02d", uiState.bedtimeHour, uiState.bedtimeMinute),
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 28.sp,
                                    color = CharcoalDeep
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable {
                                    pickTime(context, uiState.wakeHour, uiState.wakeMinute, viewModel::setWakeTime)
                                }
                            ) {
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

            // Overnight app restrictions: Start / Stop and the days it runs
            item {
                val schedule = uiState.schedule
                val running = schedule?.isActive == true
                val mask = schedule?.daysOfWeekMask ?: ScheduleRules.ALL_DAYS
                val blockingNow = schedule != null && ScheduleRules.isActiveAt(schedule, Calendar.getInstance())
                val window = "${formatClock(uiState.bedtimeHour, uiState.bedtimeMinute)} — ${formatClock(uiState.wakeHour, uiState.wakeMinute)}"

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = if (running) SageLight.copy(alpha = 0.7f) else SageLight.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.ShieldMoon, contentDescription = null, tint = SageDark)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Overnight App Restrictions", style = MaterialTheme.typography.titleSmall, color = CharcoalDeep)
                                Text(
                                    text = when {
                                        blockingNow -> "Blocking right now"
                                        running -> "On · $window"
                                        else -> "Off · $window"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (blockingNow) TerracottaClay else CharcoalMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("REPEATS ON", style = MaterialTheme.typography.labelSmall, color = SageDark, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        DaySelector(mask = mask, onMaskChange = viewModel::setDays)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = ScheduleRules.daysSummary(mask) + if (mask != 0 && mask != ScheduleRules.ALL_DAYS) " · other nights are free" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalMuted
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { if (running) viewModel.stop() else viewModel.start() },
                            enabled = running || mask != 0,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (running) SanctuarySurfaceVariant else TerracottaClay,
                                contentColor = if (running) CharcoalDeep else Color.White
                            ),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Icon(if (running) Icons.Rounded.Stop else Icons.Rounded.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (running) "Stop" else "Start", style = MaterialTheme.typography.labelLarge)
                        }

                        if (running && !accessibilityOn) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Accessibility is off, so apps can't be blocked yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TerracottaClay
                            )
                            TextButton(onClick = { PermissionManager.openAccessibilitySettings(context) }) {
                                Text("Turn on", color = TerracottaClay, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (uiState.restrictedAppCount == 0) "No apps chosen yet" else "${uiState.restrictedAppCount} apps restricted",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (uiState.restrictedAppCount == 0) TerracottaClay else CharcoalMuted,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = onNavigateToBlocker) {
                                Text("Choose apps", color = SageDark, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                            }
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

private fun pickTime(context: Context, hour: Int, minute: Int, onPicked: (Int, Int) -> Unit) {
    TimePickerDialog(context, { _, h, m -> onPicked(h, m) }, hour, minute, false).show()
}

private fun formatClock(hour: Int, minute: Int): String {
    val h12 = if (hour % 12 == 0) 12 else hour % 12
    return String.format(Locale.getDefault(), "%d:%02d %s", h12, minute, if (hour < 12) "AM" else "PM")
}

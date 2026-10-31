package com.soulspace.app.ui.screens.wellness

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulspace.app.ui.components.EmptyHint
import com.soulspace.app.ui.components.SoulFilterChip
import com.soulspace.app.ui.components.soulSwitchColors
import com.soulspace.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

private val GoalPresets = listOf(1500, 2000, 2500, 3000)
private val IntervalOptions = listOf(15 to "15m", 30 to "30m", 45 to "45m", 60 to "1h", 120 to "2h")

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp)
        ) {
            // Animated Visual Water Pillar / Vessel
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(200.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        drawRoundRect(
                            color = SanctuarySurfaceVariant,
                            size = Size(w, h),
                            cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx())
                        )
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
            }

            // Quick add
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(250, 500, 750).forEach { amt ->
                        Button(
                            onClick = { viewModel.addWater(amt) },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SanctuarySurface, contentColor = Color.White),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("+$amt", fontFamily = FontFamily.Serif, fontSize = 16.sp, color = SageDark)
                                Text("ml", fontSize = 10.sp, color = CharcoalMuted)
                            }
                        }
                    }
                }
            }

            item {
                GoalCard(goalMl = uiState.targetGoalMl, onGoalChange = viewModel::setGoal)
            }

            item {
                ReminderCard(
                    state = uiState,
                    onEnabledChange = viewModel::setReminderEnabled,
                    onIntervalChange = viewModel::setReminderInterval,
                    onDayWindowChange = viewModel::setDayWindow
                )
            }

            // Log
            item {
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
            }

            if (uiState.logs.isEmpty()) {
                item { EmptyHint(title = "No water logged yet", body = "Tap an amount above each time you drink.") }
            }
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

/** Daily goal: drag the slider for any amount, or tap a preset. */
@Composable
private fun GoalCard(goalMl: Int, onGoalChange: (Int) -> Unit) {
    var draft by remember(goalMl) { mutableFloatStateOf(goalMl.toFloat()) }
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.WaterDrop, contentDescription = null, tint = SageDark)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Daily goal",
                    style = MaterialTheme.typography.titleSmall,
                    color = CharcoalDeep,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${(draft / 50f).roundToInt() * 50} ml",
                    fontFamily = FontFamily.Serif,
                    fontSize = 20.sp,
                    color = CharcoalDeep
                )
            }
            Slider(
                value = draft.coerceIn(1000f, 5000f),
                onValueChange = { draft = it },
                onValueChangeFinished = { onGoalChange((draft / 50f).roundToInt() * 50) },
                valueRange = 1000f..5000f,
                steps = 79, // 50 ml steps
                colors = SliderDefaults.colors(
                    thumbColor = SagePrimary,
                    activeTrackColor = SagePrimary,
                    inactiveTrackColor = CharcoalDivider
                )
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                GoalPresets.forEach { preset ->
                    SoulFilterChip(
                        selected = goalMl == preset,
                        label = "$preset",
                        onClick = { onGoalChange(preset) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/** Reminder switch, interval, and the day window that keeps reminders from ringing at night. */
@Composable
private fun ReminderCard(
    state: WaterUiState,
    onEnabledChange: (Boolean) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onDayWindowChange: (Int, Int, Int, Int) -> Unit
) {
    val context = LocalContext.current
    var permissionDenied by remember { mutableStateOf(false) }
    val notificationsOn = NotificationManagerCompat.from(context).areNotificationsEnabled()

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionDenied = !granted
        if (granted) onEnabledChange(true)
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = SageDark)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Drink reminders", style = MaterialTheme.typography.titleSmall, color = CharcoalDeep)
                    Text(
                        text = if (state.reminderEnabled) "On" else "Off",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalMuted
                    )
                }
                Switch(
                    checked = state.reminderEnabled,
                    onCheckedChange = { wantOn ->
                        if (!wantOn) {
                            onEnabledChange(false)
                        } else if (needsNotificationPermission(context)) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            permissionDenied = false
                            onEnabledChange(true)
                        }
                    },
                    colors = soulSwitchColors()
                )
            }

            if (permissionDenied || (state.reminderEnabled && !notificationsOn)) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Notifications are blocked for SOULSPACE, so reminders can't show.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TerracottaClay
                )
                TextButton(onClick = { openNotificationSettings(context) }) {
                    Text("Open settings", color = TerracottaClay, fontWeight = FontWeight.SemiBold)
                }
            }

            if (state.reminderEnabled) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "REMIND ME EVERY",
                    style = MaterialTheme.typography.labelSmall,
                    color = SageDark,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    IntervalOptions.forEach { (minutes, label) ->
                        SoulFilterChip(
                            selected = state.reminderIntervalMin == minutes,
                            label = label,
                            onClick = { onIntervalChange(minutes) },
                            modifier = Modifier.weight(1f),
                            horizontalPadding = 2.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "ACTIVE HOURS",
                    style = MaterialTheme.typography.labelSmall,
                    color = SageDark,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    TimeField(
                        label = "Day starts",
                        hour = state.dayStartHour,
                        minute = state.dayStartMinute,
                        modifier = Modifier.weight(1f)
                    ) { h, m -> onDayWindowChange(h, m, state.dayEndHour, state.dayEndMinute) }
                    TimeField(
                        label = "Day ends",
                        hour = state.dayEndHour,
                        minute = state.dayEndMinute,
                        modifier = Modifier.weight(1f)
                    ) { h, m -> onDayWindowChange(state.dayStartHour, state.dayStartMinute, h, m) }
                }

                Spacer(modifier = Modifier.height(10.dp))
                val sameTime = state.dayStartHour == state.dayEndHour && state.dayStartMinute == state.dayEndMinute
                Text(
                    text = if (sameTime) {
                        "Day start and end are the same, so only one reminder a day will ring."
                    } else {
                        "Quiet from ${formatClock(state.dayEndHour, state.dayEndMinute)} to " +
                            "${formatClock(state.dayStartHour, state.dayStartMinute)}: no reminders while you sleep."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = CharcoalMuted
                )
                state.nextReminderMillis?.let { next ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Next reminder: ${formatNext(next)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalDeep
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeField(
    label: String,
    hour: Int,
    minute: Int,
    modifier: Modifier = Modifier,
    onPicked: (Int, Int) -> Unit
) {
    val context = LocalContext.current
    OutlinedButton(
        onClick = { TimePickerDialog(context, { _, h, m -> onPicked(h, m) }, hour, minute, false).show() },
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = SanctuaryCardBg, contentColor = CharcoalDeep),
        border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalDivider),
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 11.sp, color = CharcoalMuted, maxLines = 1)
            Text(
                text = formatClock(hour, minute),
                fontFamily = FontFamily.Serif,
                fontSize = 17.sp,
                color = CharcoalDeep,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

private fun formatClock(hour: Int, minute: Int): String {
    val h12 = if (hour % 12 == 0) 12 else hour % 12
    return String.format(Locale.getDefault(), "%d:%02d %s", h12, minute, if (hour < 12) "AM" else "PM")
}

private fun formatNext(millis: Long): String {
    val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(millis))
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { timeInMillis = millis }
    return when {
        now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR) && now.get(Calendar.YEAR) == then.get(Calendar.YEAR) -> time
        else -> "tomorrow $time"
    }
}

private fun needsNotificationPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED

private fun openNotificationSettings(context: Context) {
    context.startActivity(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

package com.soulspace.app.ui.screens.blocker

import com.soulspace.app.ui.components.soulSwitchColors
import com.soulspace.app.ui.components.EmptyHint
import com.soulspace.app.ui.components.SoulSegmentedTabs
import com.soulspace.app.ui.components.SoulFilterChip
import com.soulspace.app.ui.components.soulTextFieldColors
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulspace.app.data.local.entity.BlockScheduleEntity
import com.soulspace.app.ui.theme.*
import com.soulspace.app.util.PermissionManager

@Composable
fun BlockerScreen(
    onBack: () -> Unit,
    viewModel: BlockerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Apps, 1=Schedules, 2=Commitment
    var showAddScheduleDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.checkPermissions()
    }

    Scaffold(
        containerColor = SanctuaryCream,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Smart App Blocker",
                        fontFamily = FontFamily.Serif,
                        color = CharcoalDeep
                    )
                },
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
                .padding(horizontal = 20.dp)
        ) {
            // Permission Warnings if needed
            if (!uiState.isAccessibilityGranted) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = TerracottaLight.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Security, contentDescription = null, tint = TerracottaClay)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Accessibility Required",
                                style = MaterialTheme.typography.titleSmall,
                                color = CharcoalDeep
                            )
                            Text(
                                text = "Needed to detect distracting app opens.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CharcoalMuted
                            )
                        }
                        Button(
                            onClick = { PermissionManager.openAccessibilitySettings(context) },
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaClay, contentColor = Color.White)
                        ) {
                            Text("Enable", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                        }
                    }
                }
            }

            // Tabs
            SoulSegmentedTabs(
                labels = listOf(
                    "Apps (${uiState.installedApps.count { it.isBlocked }})",
                    "Schedules",
                    "Commitment"
                ),
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    // Quick Temporary Block Strip
                    Text(
                        text = "QUICK TEMPORARY BLOCK",
                        style = MaterialTheme.typography.labelSmall,
                        color = SageDark,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15 to "15m", 30 to "30m", 60 to "1h", 120 to "2h", 240 to "4h").forEach { (mins, label) ->
                            SoulFilterChip(
                                selected = false,
                                label = label,
                                onClick = { viewModel.startTemporaryBlock(mins, "Quick Block ($label)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search Field
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search installed applications...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = CharcoalMuted) },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = soulTextFieldColors(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Installed Apps List
                    if (uiState.isLoadingApps) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = SagePrimary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(uiState.filteredApps, key = { it.packageName }) { appItem ->
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = appItem.appName,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium,
                                                color = CharcoalDeep
                                            )
                                            Text(
                                                text = appItem.packageName,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CharcoalMuted
                                            )
                                        }
                                        Switch(
                                            checked = appItem.isBlocked,
                                            onCheckedChange = { isChecked ->
                                                viewModel.toggleAppBlocked(appItem.packageName, appItem.appName, isChecked)
                                            },
                                            colors = soulSwitchColors()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Schedules Screen
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AUTOMATED SCHEDULES",
                            style = MaterialTheme.typography.labelSmall,
                            color = SageDark,
                            letterSpacing = 1.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { showAddScheduleDialog = true },
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaClay, contentColor = Color.White)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Schedule", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        if (uiState.schedules.isEmpty()) {
                            item {
                                EmptyHint(
                                    title = "No schedules yet",
                                    body = "Tap \u201cNew Schedule\u201d to block distracting apps automatically."
                                )
                            }
                        }
                        items(uiState.schedules) { schedule ->
                            ScheduleCard(
                                schedule = schedule,
                                onToggleActive = { viewModel.toggleScheduleActive(schedule) },
                                onDelete = { viewModel.deleteSchedule(schedule) }
                            )
                        }
                    }
                }

                2 -> {
                    // Commitment Mode Screen
                    CommitmentModeTab(
                        isActive = uiState.isCommitmentActive,
                        endTimeMillis = uiState.commitmentEndMillis,
                        onActivate = { hours -> viewModel.activateCommitmentMode(hours) }
                    )
                }
            }
        }

        if (showAddScheduleDialog) {
            AddScheduleDialog(
                onDismiss = { showAddScheduleDialog = false },
                onAdd = { title, sH, sM, eH, eM, mode ->
                    viewModel.addSchedule(title, sH, sM, eH, eM, mode)
                    showAddScheduleDialog = false
                }
            )
        }
    }
}

@Composable
fun ScheduleCard(
    schedule: BlockScheduleEntity,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    val isOvernight = (schedule.startHour * 60 + schedule.startMinute) > (schedule.endHour * 60 + schedule.endMinute)

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = schedule.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = CharcoalDeep
                    )
                    Text(
                        text = String.format("%02d:%02d — %02d:%02d", schedule.startHour, schedule.startMinute, schedule.endHour, schedule.endMinute),
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        color = CharcoalDeep
                    )
                }
                Switch(
                    checked = schedule.isActive,
                    onCheckedChange = { onToggleActive() },
                    colors = soulSwitchColors()
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SageLight.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = schedule.modeName,
                            style = MaterialTheme.typography.labelSmall,
                            color = SageDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (isOvernight) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TerracottaLight.copy(alpha = 0.4f)
                        ) {
                            Text(
                                text = "Overnight",
                                style = MaterialTheme.typography.labelSmall,
                                color = TerracottaClay,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
}

@Composable
fun CommitmentModeTab(
    isActive: Boolean,
    endTimeMillis: Long,
    onActivate: (Int) -> Unit
) {
    var selectedHours by remember { mutableIntStateOf(4) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = SanctuarySurfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = SageDark)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Strict Commitment Mode",
                        style = MaterialTheme.typography.titleMedium,
                        color = CharcoalDeep
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Commitment Mode guarantees zero impulsive cancellations. Once engaged, restrictions remain strictly active across phone restarts until the chosen duration lapses. An emergency 5-minute override with a reflective delay is preserved for safety.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CharcoalMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isActive && endTimeMillis > System.currentTimeMillis()) {
            val remainingMins = (endTimeMillis - System.currentTimeMillis()) / (60 * 1000)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SagePrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "COMMITMENT ACTIVE",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${remainingMins / 60}h ${remainingMins % 60}m remaining",
                        fontFamily = FontFamily.Serif,
                        fontSize = 28.sp,
                        color = Color.White
                    )
                }
            }
        } else {
            Text(
                text = "SELECT COMMITMENT DURATION",
                style = MaterialTheme.typography.labelSmall,
                color = SageDark,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(2 to "2h", 4 to "4h", 8 to "8h", 12 to "12h", 24 to "24h").forEach { (hrs, label) ->
                    SoulFilterChip(
                        selected = selectedHours == hrs,
                        label = label,
                        onClick = { selectedHours = hrs },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = { showConfirmDialog = true },
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SageDark, contentColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Rounded.Lock, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lock In $selectedHours Hours", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        }

        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                containerColor = SanctuarySurface,
                titleContentColor = CharcoalDeep,
                textContentColor = CharcoalMuted,
                title = { Text("Confirm Commitment", fontFamily = FontFamily.Serif) },
                text = {
                    Text(
                        "Are you ready to commit to $selectedHours hours of uninterrupted presence? Selected applications will be strictly inaccessible until this duration concludes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CharcoalMuted
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmDialog = false
                            onActivate(selectedHours)
                        },
                        shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = SagePrimary, contentColor = Color.White)
                    ) {
                        Text("I Commit", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("Cancel", color = CharcoalMuted)
                    }
                }
            )
        }
    }
}

@Composable
fun AddScheduleDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int, Int, Int, Int, String) -> Unit
) {
    var title by remember { mutableStateOf("Deep Work Schedule") }
    var startHour by remember { mutableIntStateOf(9) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(17) }
    var endMinute by remember { mutableIntStateOf(0) }
    var mode by remember { mutableStateOf("Work") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SanctuarySurface,
        titleContentColor = CharcoalDeep,
        textContentColor = CharcoalMuted,
        title = { Text("Create Block Schedule", fontFamily = FontFamily.Serif) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Schedule Title") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = soulTextFieldColors()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = String.format("%02d:%02d", startHour, startMinute),
                        onValueChange = {},
                        label = { Text("Start Time") },
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                    colors = soulTextFieldColors()
                )
                    OutlinedTextField(
                        value = String.format("%02d:%02d", endHour, endMinute),
                        onValueChange = {},
                        label = { Text("End Time") },
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                    colors = soulTextFieldColors()
                )
                }
                Text("Mode: $mode", style = MaterialTheme.typography.labelMedium, color = SageDark)
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(title, startHour, startMinute, endHour, endMinute, mode) },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = SagePrimary, contentColor = Color.White)
            ) {
                Text("Save Schedule", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CharcoalMuted)
            }
        }
    )
}


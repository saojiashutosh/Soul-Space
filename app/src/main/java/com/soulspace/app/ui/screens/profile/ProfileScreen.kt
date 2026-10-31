package com.soulspace.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulspace.app.ui.components.ZenMeditatingIcon
import com.soulspace.app.ui.theme.*
import com.soulspace.app.util.PermissionManager

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.checkPermissions()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SanctuaryCream)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Profile Avatar & Mindful Stats Card
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(SageLight)
                    ) {
                        ZenMeditatingIcon(modifier = Modifier.size(46.dp), color = SageDark)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Soul Traveler",
                        fontFamily = FontFamily.Serif,
                        fontSize = 24.sp,
                        color = CharcoalDeep
                    )
                    Text(
                        text = "Intentional Living • Offline Sanctuary",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalMuted
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${uiState.totalPoints}", fontFamily = FontFamily.Serif, fontSize = 22.sp, color = CharcoalDeep)
                            Text(text = "Mindful Pts", style = MaterialTheme.typography.labelSmall, color = CharcoalMuted)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${uiState.totalFocusHours}h", fontFamily = FontFamily.Serif, fontSize = 22.sp, color = CharcoalDeep)
                            Text(text = "Deep Focus", style = MaterialTheme.typography.labelSmall, color = CharcoalMuted)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${uiState.totalCheckIns}", fontFamily = FontFamily.Serif, fontSize = 22.sp, color = CharcoalDeep)
                            Text(text = "Check-ins", style = MaterialTheme.typography.labelSmall, color = CharcoalMuted)
                        }
                    }
                }
            }
        }

        // System Permissions Dashboard
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "SYSTEM PERMISSIONS STATUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = SageDark,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    PermissionRow(
                        title = "App Blocking (Accessibility)",
                        isGranted = uiState.isAccessibilityGranted,
                        onFix = { PermissionManager.openAccessibilitySettings(context) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PermissionRow(
                        title = "Screen Time (Usage Access)",
                        isGranted = uiState.isUsageStatsGranted,
                        onFix = { PermissionManager.openUsageStatsSettings(context) }
                    )
                }
            }
        }

        // Mindful Achievements & Badges
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "MINDFUL MILESTONES",
                        style = MaterialTheme.typography.labelSmall,
                        color = SageDark,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    uiState.badges.forEach { badge ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (badge.isUnlocked) SageLight else SanctuarySurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (badge.isUnlocked) Icons.Rounded.EmojiEvents else Icons.Rounded.Lock,
                                        contentDescription = null,
                                        tint = if (badge.isUnlocked) SageDark else CharcoalMuted
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = badge.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (badge.isUnlocked) CharcoalDeep else CharcoalMuted
                                )
                                Text(
                                    text = badge.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CharcoalMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Privacy & Reset
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SanctuarySurfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.PrivacyTip, contentDescription = null, tint = SageDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "100% Privacy & Zero Data Tracking",
                            style = MaterialTheme.typography.titleSmall,
                            color = CharcoalDeep
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "All personal routines, habits, app blocks, and usage statistics are stored exclusively in your device's local database. No data is ever transmitted to remote servers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TerracottaClay)
                    ) {
                        Text("Reset All Local Data")
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = SanctuarySurface,
            title = { Text("Reset Local Sanctuary", fontFamily = FontFamily.Serif) },
            text = { Text("Are you sure you want to clear all habits, timetables, and logs? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaClay)
                ) {
                    Text("Clear All Data", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = CharcoalMuted)
                }
            }
        )
    }
}

@Composable
fun PermissionRow(
    title: String,
    isGranted: Boolean,
    onFix: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isGranted) Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber,
                contentDescription = null,
                tint = if (isGranted) SagePrimary else TerracottaClay,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = CharcoalDeep)
        }
        if (!isGranted) {
            TextButton(onClick = onFix) {
                Text("Fix", style = MaterialTheme.typography.labelSmall, color = TerracottaClay)
            }
        }
    }
}

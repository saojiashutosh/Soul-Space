package com.soulspace.app.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulspace.app.ui.components.ScreenTimeBarChart
import com.soulspace.app.ui.theme.*
import com.soulspace.app.util.PermissionManager

@Composable
fun AnalyticsScreen(
    onBack: () -> Unit = {},
    viewModel: AnalyticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.refreshAnalytics()
    }

    Scaffold(
        containerColor = SanctuaryCream,
        topBar = {
            TopAppBar(
                title = { Text("Screen Time Sanctuary", fontFamily = FontFamily.Serif, color = CharcoalDeep) },
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
            // Permission Banner if missing
            if (!uiState.isPermissionGranted) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = TerracottaLight.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.DataUsage, contentDescription = null, tint = TerracottaClay)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Usage Access Required", style = MaterialTheme.typography.titleSmall, color = CharcoalDeep)
                                Text("Needed to compile on-device screen-time reports.", style = MaterialTheme.typography.bodySmall, color = CharcoalMuted)
                            }
                            Button(
                                onClick = { PermissionManager.openUsageStatsSettings(context) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaClay)
                            ) {
                                Text("Grant", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Screen-Time Overview Card
            item {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Text(
                            text = "TODAY'S SCREEN DURATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = SageDark,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val hrs = uiState.totalScreenTimeMinutes / 60
                        val mins = uiState.totalScreenTimeMinutes % 60
                        Text(
                            text = "${hrs}h ${mins}m",
                            fontFamily = FontFamily.Serif,
                            fontSize = 36.sp,
                            color = CharcoalDeep
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Estimated ${uiState.timeSavedMinutes}m preserved by active app blocking.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SageDark
                        )
                    }
                }
            }

            // Weekly Trend Canvas Chart
            item {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "7-Day Usage Rhythm",
                            style = MaterialTheme.typography.titleMedium,
                            color = CharcoalDeep
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ScreenTimeBarChart(
                            hoursPerDay = uiState.weeklyHoursList,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            barColor = SagePrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                                Text(text = day, style = MaterialTheme.typography.labelSmall, color = CharcoalMuted)
                            }
                        }
                    }
                }
            }

            // Category Distribution
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CategoryPill(title = "Social", mins = uiState.socialMinutes, color = TerracottaClay, modifier = Modifier.weight(1f))
                    CategoryPill(title = "Media", mins = uiState.entertainmentMinutes, color = OliveMuted, modifier = Modifier.weight(1f))
                    CategoryPill(title = "Productive", mins = uiState.productivityMinutes, color = SagePrimary, modifier = Modifier.weight(1f))
                }
            }

            // Most Used Applications Header
            item {
                Text(
                    text = "MOST USED APPLICATIONS TODAY",
                    style = MaterialTheme.typography.labelSmall,
                    color = SageDark,
                    letterSpacing = 1.sp
                )
            }

            // App list
            items(uiState.topApps) { appInfo ->
                val appMins = appInfo.totalTimeInForegroundMillis / (60 * 1000)
                val appHours = appMins / 60
                val appRemMins = appMins % 60
                val usageText = if (appHours > 0) "${appHours}h ${appRemMins}m" else "${appRemMins}m"

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = appInfo.appName, style = MaterialTheme.typography.bodyLarge, color = CharcoalDeep)
                            Text(text = appInfo.category, style = MaterialTheme.typography.labelSmall, color = CharcoalMuted)
                        }
                        Text(text = usageText, fontFamily = FontFamily.Serif, fontSize = 16.sp, color = CharcoalDeep)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryPill(title: String, mins: Long, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "${mins}m", fontFamily = FontFamily.Serif, fontSize = 18.sp, color = CharcoalDeep)
        }
    }
}

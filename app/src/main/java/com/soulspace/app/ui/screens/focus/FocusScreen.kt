package com.soulspace.app.ui.screens.focus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulspace.app.ui.theme.*

@Composable
fun FocusScreen(
    onNavigateToBlocker: () -> Unit = {},
    viewModel: FocusViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val progress = if (uiState.totalSeconds > 0) {
        uiState.secondsRemaining.toFloat() / uiState.totalSeconds.toFloat()
    } else {
        0f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "progress"
    )

    val minutes = uiState.secondsRemaining / 60
    val seconds = uiState.secondsRemaining % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SanctuaryCream)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "DEEP FOCUS SANCTUARY",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp,
                color = SageDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Single-Tasking Flow",
                fontFamily = FontFamily.Serif,
                fontSize = 28.sp,
                color = CharcoalDeep
            )
        }

        // Preset Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            listOf(
                Triple("25/5", "25/5 Standard", 25),
                Triple("50/10", "50/10 Flow", 50),
                Triple("90m", "90m Deep Work", 90),
                Triple("15m", "15m Sprint", 15)
            ).forEach { (shortLabel, fullLabel, mins) ->
                FilterChip(
                    selected = uiState.currentPreset == fullLabel,
                    onClick = { viewModel.selectPreset(fullLabel, mins) },
                    label = { Text(shortLabel) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(horizontal = 4.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SagePrimary,
                        selectedLabelColor = Color.White,
                        containerColor = SanctuarySurface,
                        labelColor = CharcoalDeep
                    )
                )
            }
        }

        // Circular Timer Display
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(280.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()

                // Background track
                drawCircle(
                    color = SanctuarySurfaceVariant,
                    style = Stroke(width = strokeWidth)
                )

                // Active progress arc
                drawArc(
                    color = SagePrimary,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formattedTime,
                    fontFamily = FontFamily.Serif,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Normal,
                    color = CharcoalDeep
                )
                Text(
                    text = if (uiState.isRunning) "In Deep Presence" else "Ready to Focus",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CharcoalMuted
                )
            }
        }

        // Auto-Block Toggle Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Shield, contentDescription = null, tint = SageDark)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Auto-Block Distractions",
                            style = MaterialTheme.typography.titleSmall,
                            color = CharcoalDeep
                        )
                        Text(
                            text = "Silence restricted apps during timer",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalMuted
                        )
                    }
                }
                Switch(
                    checked = uiState.autoBlockEnabled,
                    onCheckedChange = { viewModel.toggleAutoBlock(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SagePrimary
                    )
                )
            }
        }

        // Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.resetTimer() },
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(SanctuarySurface)
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Reset", tint = CharcoalDeep)
            }

            Spacer(modifier = Modifier.width(28.dp))

            Button(
                onClick = {
                    if (uiState.isRunning) viewModel.pauseTimer() else viewModel.startTimer()
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                modifier = Modifier.size(76.dp)
            ) {
                Icon(
                    imageVector = if (uiState.isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (uiState.isRunning) "Pause" else "Start",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(28.dp))

            IconButton(
                onClick = onNavigateToBlocker,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(SanctuarySurface)
            ) {
                Icon(Icons.Rounded.AppSettingsAlt, contentDescription = "Apps", tint = CharcoalDeep)
            }
        }
    }
}


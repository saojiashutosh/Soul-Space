package com.soulspace.app.ui.screens.sounds

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulspace.app.ui.theme.*

@Composable
fun SoundsScreen(
    viewModel: SoundsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories = listOf("Nature", "Focus", "Sleep", "Meditation")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SanctuaryCream)
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp)
    ) {
        // Header
        Text(
            text = "AUDIO SANCTUARY",
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 2.sp,
            color = SageDark
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Ambient Soundscapes",
            fontFamily = FontFamily.Serif,
            fontSize = 30.sp,
            color = CharcoalDeep
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = uiState.currentCategory == cat,
                    onClick = { viewModel.setCategory(cat) },
                    label = { Text(cat) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SagePrimary,
                        selectedLabelColor = Color.White,
                        containerColor = SanctuarySurface,
                        labelColor = CharcoalDeep
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sleep Timer Quick Strip
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Bedtime, contentDescription = null, tint = SageDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sleep Timer",
                        style = MaterialTheme.typography.labelMedium,
                        color = CharcoalDeep
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(15, 30, 45, 60).forEach { mins ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (uiState.sleepTimerMinutes == mins) SagePrimary else SanctuarySurfaceVariant,
                            modifier = Modifier.clip(RoundedCornerShape(10.dp))
                        ) {
                            TextButton(
                                onClick = { viewModel.setSleepTimer(mins) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${mins}m",
                                    fontSize = 12.sp,
                                    color = if (uiState.sleepTimerMinutes == mins) Color.White else CharcoalDeep
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sound Grid
        val filteredSounds = uiState.sounds.filter { it.category == uiState.currentCategory }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredSounds, key = { it.id }) { sound ->
                val isPlaying = uiState.currentPlayingId == sound.id

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPlaying) SageLight.copy(alpha = 0.6f) else SanctuarySurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = { viewModel.togglePlay(sound) },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) SagePrimary else SanctuaryCream)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = if (isPlaying) Color.White else SageDark
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = sound.title,
                            fontFamily = FontFamily.Serif,
                            fontSize = 16.sp,
                            color = CharcoalDeep
                        )
                        Text(
                            text = if (isPlaying) "Playing now" else "Tap to play",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isPlaying) SageDark else CharcoalMuted
                        )
                    }
                }
            }
        }
    }
}

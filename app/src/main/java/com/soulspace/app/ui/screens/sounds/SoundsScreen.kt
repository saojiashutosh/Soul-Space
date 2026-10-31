package com.soulspace.app.ui.screens.sounds

import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulspace.app.ui.components.SoulFilterChip
import com.soulspace.app.ui.theme.*

private const val MIX_SLOTS = 4

@Composable
fun SoundsScreen(
    viewModel: SoundsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories = listOf("Nature", "Focus", "Sleep", "Meditation")
    val titleById = remember(uiState.sounds) { uiState.sounds.associate { it.id to it.title } }
    val filteredSounds = uiState.sounds.filter { it.category == uiState.currentCategory }

    // Leaving this tab stops the sounds. Rotation recreates the screen, so it is excluded.
    val activity = LocalContext.current as? Activity
    DisposableEffect(Unit) {
        onDispose {
            if (activity?.isChangingConfigurations != true) viewModel.stopAll()
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 100.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(SanctuaryCream)
    ) {
        // Header
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
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
                    lineHeight = 36.sp,
                    color = CharcoalDeep
                )
            }
        }

        // Mix: one vertical volume channel per playing sound
        item(span = { GridItemSpan(maxLineSpan) }) {
            MixPanel(
                playing = uiState.playing,
                titleById = titleById,
                onVolume = viewModel::setVolume,
                onRemove = { id -> uiState.sounds.firstOrNull { it.id == id }?.let(viewModel::togglePlay) },
                onReset = viewModel::stopAll
            )
        }

        // Sleep timer
        item(span = { GridItemSpan(maxLineSpan) }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Bedtime, contentDescription = null, tint = SageDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sleep Timer",
                            style = MaterialTheme.typography.labelLarge,
                            color = CharcoalDeep
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15, 30, 45, 60).forEach { mins ->
                            SoulFilterChip(
                                selected = uiState.sleepTimerMinutes == mins,
                                label = "${mins}m",
                                onClick = { viewModel.setSleepTimer(mins) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Category chips
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    SoulFilterChip(
                        selected = uiState.currentCategory == cat,
                        label = cat,
                        onClick = { viewModel.setCategory(cat) },
                        horizontalPadding = 10.dp
                    )
                }
            }
        }

        // Sounds
        items(filteredSounds, key = { it.id }) { sound ->
            SoundTile(
                title = sound.title,
                isPlaying = uiState.playing.containsKey(sound.id),
                onClick = { viewModel.togglePlay(sound) }
            )
        }
    }
}

@Composable
private fun MixPanel(
    playing: Map<String, Float>,
    titleById: Map<String, String>,
    onVolume: (String, Float) -> Unit,
    onRemove: (String) -> Unit,
    onReset: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Your mix",
                        fontFamily = FontFamily.Serif,
                        fontSize = 18.sp,
                        color = CharcoalDeep
                    )
                    Text(
                        text = if (playing.isEmpty()) "Pick sounds below to start" else "Drag a channel up or down for volume",
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalMuted,
                        maxLines = 1
                    )
                }
                TextButton(onClick = onReset, enabled = playing.isNotEmpty()) {
                    Text(
                        text = "Reset",
                        color = if (playing.isNotEmpty()) TerracottaClay else CharcoalMuted,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                playing.forEach { (id, volume) ->
                    MixChannel(
                        title = titleById[id] ?: id,
                        volume = volume,
                        onVolume = { onVolume(id, it) },
                        onRemove = { onRemove(id) }
                    )
                }
                repeat((MIX_SLOTS - playing.size).coerceAtLeast(0)) { EmptyChannel() }
            }
        }
    }
}

private val ChannelWidth = 62.dp
private val ChannelHeight = 150.dp

/** A vertical volume bar: tap or drag anywhere on it to set the level. */
@Composable
private fun MixChannel(
    title: String,
    volume: Float,
    onVolume: (Float) -> Unit,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(ChannelWidth, ChannelHeight)
            .clip(RoundedCornerShape(18.dp))
            .background(SanctuaryCardBg)
            .pointerInput(Unit) {
                detectTapGestures { offset -> onVolume(1f - offset.y / size.height) }
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, _ ->
                    change.consume()
                    onVolume(1f - change.position.y / size.height)
                }
            }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(volume.coerceIn(0f, 1f))
                .background(SagePrimary.copy(alpha = 0.45f))
        )
        Text(
            text = title,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.Medium,
            color = CharcoalDeep,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp)
        )
        Text(
            text = "${(volume * 100).toInt()}%",
            fontSize = 11.sp,
            color = CharcoalDeep.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(32.dp)
        ) {
            Icon(
                Icons.Rounded.Close,
                contentDescription = "Remove $title",
                tint = CharcoalDeep.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun EmptyChannel() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(ChannelWidth, ChannelHeight)
            .clip(RoundedCornerShape(18.dp))
            .background(SanctuaryCardBg.copy(alpha = 0.5f))
    ) {
        Icon(Icons.Rounded.Add, contentDescription = null, tint = CharcoalDivider)
    }
}

@Composable
private fun SoundTile(
    title: String,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) SageLight.copy(alpha = 0.7f) else SanctuarySurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 118.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) SagePrimary else SanctuaryCream)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) "Pause $title" else "Play $title",
                    tint = if (isPlaying) Color.White else SageDark
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontFamily = FontFamily.Serif,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                color = CharcoalDeep,
                textAlign = TextAlign.Center,
                minLines = 2,
                maxLines = 2
            )
        }
    }
}

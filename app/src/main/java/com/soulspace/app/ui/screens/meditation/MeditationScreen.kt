package com.soulspace.app.ui.screens.meditation

import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import com.soulspace.app.service.SoundMixer
import kotlinx.coroutines.delay
import android.os.SystemClock
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulspace.app.ui.components.ZenMeditatingIcon
import com.soulspace.app.ui.theme.*

@Composable
fun MeditationScreen(
    onBack: () -> Unit = {},
    viewModel: MeditationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeSession by SoundMixer.session.collectAsState()

    // Leaving this screen ends a running session. Rotation recreates the screen, so it is excluded.
    val activity = LocalContext.current as? Activity
    DisposableEffect(Unit) {
        onDispose {
            if (activity?.isChangingConfigurations != true) viewModel.stopSession()
        }
    }
    var isBreathingActive by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = SanctuaryCream,
        topBar = {
            TopAppBar(
                title = {
                    Text("Meditation Sanctuary", fontFamily = FontFamily.Serif, color = CharcoalDeep)
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Running guided session
            activeSession?.let { running ->
                item { ActiveSessionCard(session = running, onStop = viewModel::stopSession) }
            }

            // Interactive Breathing Visualizer Card
            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "GUIDED BREATH PACER",
                            style = MaterialTheme.typography.labelSmall,
                            color = SageDark,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBreathingActive) uiState.breathingPhase else "4-4-6 Calming Breath",
                            fontFamily = FontFamily.Serif,
                            fontSize = 24.sp,
                            color = CharcoalDeep
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Animated Pacer
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(180.dp)
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "breath")
                            val scale by infiniteTransition.animateFloat(
                                initialValue = 0.65f,
                                targetValue = 1.15f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "scale"
                            )

                            Box(
                                modifier = Modifier
                                    .size(170.dp)
                                    .scale(if (isBreathingActive) scale else 0.85f)
                                    .clip(CircleShape)
                                    .background(SageLight.copy(alpha = 0.5f))
                            )
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .scale(if (isBreathingActive) scale * 0.9f else 0.75f)
                                    .clip(CircleShape)
                                    .background(SagePrimary)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    if (isBreathingActive) {
                                        Text(
                                            text = "${uiState.breathingSecondsLeftInPhase}s",
                                            fontFamily = FontFamily.Serif,
                                            fontSize = 26.sp,
                                            color = Color.White
                                        )
                                    } else {
                                        ZenMeditatingIcon(
                                            modifier = Modifier.size(54.dp),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (isBreathingActive) {
                                    viewModel.stopBreathingExercise()
                                    isBreathingActive = false
                                } else {
                                    viewModel.startBreathingExercise()
                                    isBreathingActive = true
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SagePrimary, contentColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 50.dp)
                        ) {
                            Icon(
                                if (isBreathingActive) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBreathingActive) "End Breathing" else "Begin 3-Min Breathing",
                                style = MaterialTheme.typography.labelLarge,
                                fontSize = 15.sp,
                                maxLines = 1,
                                softWrap = false,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Section Header: Guided Sessions
            item {
                Text(
                    text = "SANCTUARY SESSIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = SageDark,
                    letterSpacing = 1.sp
                )
            }

            // Meditation Sessions List
            items(uiState.sessions) { session ->
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(session.themeColorHex).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${session.durationMinutes} MIN • ${session.category.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(session.themeColorHex),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = session.title,
                                fontFamily = FontFamily.Serif,
                                fontSize = 18.sp,
                                color = CharcoalDeep
                            )
                            Text(
                                text = session.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = CharcoalMuted
                            )
                        }

                        val isRunning = activeSession?.id == session.id
                        IconButton(
                            onClick = {
                                if (isRunning) viewModel.stopSession() else viewModel.startSession(session)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isRunning) TerracottaClay else SagePrimary)
                        ) {
                            Icon(
                                if (isRunning) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                contentDescription = if (isRunning) "Stop session" else "Start session",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ActiveSessionCard(
    session: SoundMixer.Session,
    onStop: () -> Unit
) {
    var now by remember(session) { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(session) {
        while (true) {
            now = SystemClock.elapsedRealtime()
            delay(1000)
        }
    }
    val remaining = (session.endsAtElapsedMillis - now).coerceIn(0L, session.totalMillis)
    val seconds = (remaining / 1000).toInt()
    val progress = 1f - remaining.toFloat() / session.totalMillis.toFloat()

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SageLight.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "SESSION IN PROGRESS",
                style = MaterialTheme.typography.labelSmall,
                color = SageDark,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = session.title,
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                color = CharcoalDeep
            )
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                color = SagePrimary,
                trackColor = CharcoalDivider,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "%d:%02d left".format(seconds / 60, seconds % 60),
                    fontFamily = FontFamily.Serif,
                    fontSize = 22.sp,
                    color = CharcoalDeep,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = onStop,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaClay, contentColor = Color.White)
                ) {
                    Text("End session", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                }
            }
        }
    }
}

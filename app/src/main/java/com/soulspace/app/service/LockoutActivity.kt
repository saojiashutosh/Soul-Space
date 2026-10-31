package com.soulspace.app.service

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soulspace.app.ui.components.ZenMeditatingIcon
import com.soulspace.app.ui.theme.*
import kotlinx.coroutines.delay

class LockoutActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Intercept back button to return user to home launcher safely
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goToHomeScreen()
            }
        })

        val packageName = intent.getStringExtra("PACKAGE_NAME") ?: ""
        val appName = intent.getStringExtra("APP_NAME") ?: "This Application"
        val reason = intent.getStringExtra("REASON") ?: "Mindful Focus"
        val remainingSeconds = intent.getLongExtra("REMAINING_SECONDS", 1800L)

        setContent {
            SoulSpaceTheme {
                LockoutScreen(
                    appName = appName,
                    reason = reason,
                    initialRemainingSeconds = remainingSeconds,
                    onReturnHome = { goToHomeScreen() },
                    onEmergencyUnlock = {
                        // Grant 5 minutes temporary grace for this app
                        AppBlockingAccessibilityService.temporaryAllowedPackage = packageName
                        AppBlockingAccessibilityService.temporaryAllowedUntil = System.currentTimeMillis() + 5 * 60 * 1000
                        finish()
                    }
                )
            }
        }
    }

    private fun goToHomeScreen() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }
}

@Composable
fun LockoutScreen(
    appName: String,
    reason: String,
    initialRemainingSeconds: Long,
    onReturnHome: () -> Unit,
    onEmergencyUnlock: () -> Unit
) {
    var secondsLeft by remember { mutableLongStateOf(initialRemainingSeconds) }
    var showBreathingExercise by remember { mutableStateOf(false) }
    var showEmergencyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
    }

    val hours = secondsLeft / 3600
    val minutes = (secondsLeft % 3600) / 60
    val seconds = secondsLeft % 60
    val timeFormatted = if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SanctuaryCream)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Sanctuary Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ZenMeditatingIcon(
                    modifier = Modifier.size(90.dp),
                    color = SagePrimary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "SOULSPACE",
                    style = MaterialTheme.typography.labelLarge,
                    letterSpacing = 3.sp,
                    color = SageDark
                )
            }

            // Center Calming Card
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Pause & Center",
                        fontFamily = FontFamily.Serif,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Normal,
                        color = CharcoalDeep
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You decided to take a break from $appName",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = CharcoalMuted
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Countdown Badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SageLight.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "AVAILABLE IN",
                                style = MaterialTheme.typography.labelSmall,
                                color = SageDark,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = timeFormatted,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalDeep
                            )
                            Text(
                                text = reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = CharcoalMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "\"You will never regret the moments you spent being present.\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        color = CharcoalMuted
                    )
                }
            }

            // Bottom Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showBreathingExercise = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Icon(Icons.Rounded.SelfImprovement, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "1-Minute Breathing Pacer",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = onReturnHome,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CharcoalDeep),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Icon(Icons.Rounded.Home, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Return to Home",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                TextButton(
                    onClick = { showEmergencyDialog = true }
                ) {
                    Text(
                        text = "Emergency 5-Minute Override",
                        style = MaterialTheme.typography.labelMedium,
                        color = CharcoalMuted
                    )
                }
            }
        }

        // In-line Breathing Exercise Sheet
        if (showBreathingExercise) {
            LockoutBreathingDialog(onDismiss = { showBreathingExercise = false })
        }

        // Emergency Override Confirmation Dialog
        if (showEmergencyDialog) {
            EmergencyDelayDialog(
                appName = appName,
                onDismiss = { showEmergencyDialog = false },
                onConfirmed = {
                    showEmergencyDialog = false
                    onEmergencyUnlock()
                }
            )
        }
    }
}

@Composable
fun LockoutBreathingDialog(onDismiss: () -> Unit) {
    var phase by remember { mutableStateOf("Inhale") }
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(scale) {
        phase = if (scale > 0.9f) "Hold & Exhale" else "Inhale Gently"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = SagePrimary)
            }
        },
        containerColor = SanctuarySurface,
        title = {
            Text(
                "Find Your Center",
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(160.dp)
                        .scale(scale)
                        .background(SageLight, shape = CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(SagePrimary.copy(alpha = 0.35f), shape = CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = phase,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = CharcoalDeep
                )
                Text(
                    text = "Breathe in through nose, release slowly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CharcoalMuted
                )
            }
        }
    )
}

@Composable
fun EmergencyDelayDialog(
    appName: String,
    onDismiss: () -> Unit,
    onConfirmed: () -> Unit
) {
    var delaySeconds by remember { mutableIntStateOf(10) }

    LaunchedEffect(Unit) {
        while (delaySeconds > 0) {
            delay(1000)
            delaySeconds--
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SanctuarySurface,
        icon = { Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = TerracottaClay) },
        title = {
            Text(
                "Mindful Reflection",
                fontFamily = FontFamily.Serif,
                color = CharcoalDeep
            )
        },
        text = {
            Column {
                Text(
                    text = "Is opening $appName truly necessary right now, or an impulsive reflex?\n\nIf needed for an emergency, you may unlock it for 5 minutes after this brief pause.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CharcoalMuted
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (delaySeconds > 0) {
                    Text(
                        text = "Take a breath: $delaySeconds seconds remaining",
                        style = MaterialTheme.typography.labelMedium,
                        color = SageDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmed,
                enabled = delaySeconds == 0,
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaClay)
            ) {
                Text("Unlock for 5 Mins", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Stay Focused", color = SagePrimary)
            }
        }
    )
}

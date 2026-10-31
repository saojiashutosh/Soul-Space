package com.soulspace.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulspace.app.ui.components.CardMotif
import com.soulspace.app.ui.components.SanctuaryCategoryCard
import com.soulspace.app.ui.components.ZenMeditatingIcon
import com.soulspace.app.ui.navigation.Screen
import com.soulspace.app.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateTo: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SanctuaryCream)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ZenMeditatingIcon(
                        modifier = Modifier.size(38.dp),
                        color = CharcoalDeep
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SOULSPACE",
                        style = MaterialTheme.typography.labelLarge,
                        letterSpacing = 2.sp,
                        color = SageDark
                    )
                }

                IconButton(
                    onClick = { onNavigateTo(Screen.Profile.route) },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SanctuarySurface)
                ) {
                    Icon(
                        Icons.Rounded.PersonOutline,
                        contentDescription = "Profile",
                        tint = CharcoalDeep
                    )
                }
            }
        }

        // Greeting
        item {
            Column {
                Text(
                    text = "${uiState.greeting}, Soul",
                    fontFamily = FontFamily.Serif,
                    fontSize = 28.sp,
                    color = CharcoalDeep
                )
                Text(
                    text = "Calm your senses",
                    style = MaterialTheme.typography.labelSmall,
                    color = CharcoalMuted
                )
            }
        }

        // Category cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SanctuaryCategoryCard(
                    title = "Sleep",
                    subtitle = "Restorative sessions",
                    backgroundColor = IllusMint,
                    accentColor = SageDark,
                    motif = CardMotif.Moon,
                    onClick = { onNavigateTo(Screen.Sleep.route) }
                )
                SanctuaryCategoryCard(
                    title = "Soothe",
                    subtitle = "Breathing & stillness",
                    backgroundColor = WarmSand,
                    accentColor = TerracottaClay,
                    motif = CardMotif.Stones,
                    hillColor = IllusGrey,
                    onClick = { onNavigateTo(Screen.Meditation.route) }
                )
                SanctuaryCategoryCard(
                    title = "Unwind",
                    subtitle = "Ambient nature sounds",
                    backgroundColor = SoftBlush,
                    accentColor = TerracottaClay,
                    motif = CardMotif.Sun,
                    hillColor = IllusGrey,
                    onClick = { onNavigateTo(Screen.Sounds.route) }
                )
            }
        }

        // Smart App Blocker Quick Status Banner
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isAppBlockerActive) SagePrimary else SanctuarySurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateTo(Screen.Blocker.route) }
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (uiState.isAppBlockerActive) Color.White.copy(alpha = 0.2f) else SageLight,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.Shield,
                                    contentDescription = null,
                                    tint = if (uiState.isAppBlockerActive) Color.White else SageDark
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "App Blocking Shield",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (uiState.isAppBlockerActive) Color.White else CharcoalDeep
                            )
                            Text(
                                text = if (uiState.isAppBlockerActive)
                                    "${uiState.activeBlockedAppCount} apps currently restricted"
                                else
                                    "Tap to configure focus restrictions",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (uiState.isAppBlockerActive) Color.White.copy(alpha = 0.8f) else CharcoalMuted
                            )
                        }
                    }
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = if (uiState.isAppBlockerActive) Color.White else CharcoalMuted
                    )
                }
            }
        }

        // Screen-Time Sanctuary Snapshot
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateTo(Screen.Analytics.route) }
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Screen Time Today",
                            style = MaterialTheme.typography.titleMedium,
                            color = CharcoalDeep
                        )
                        Text(
                            text = "View Analytics →",
                            style = MaterialTheme.typography.labelSmall,
                            color = SageDark
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            val hours = uiState.dailyScreenTimeMinutes / 60
                            val mins = uiState.dailyScreenTimeMinutes % 60
                            Text(
                                text = "${hours}h ${mins}m",
                                fontFamily = FontFamily.Serif,
                                fontSize = 30.sp,
                                color = CharcoalDeep
                            )
                            Text(
                                text = "Total device screen on-time",
                                style = MaterialTheme.typography.bodySmall,
                                color = CharcoalMuted
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SageLight.copy(alpha = 0.5f),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Text(
                                text = "+${uiState.timeSavedMinutes}m Saved",
                                style = MaterialTheme.typography.labelMedium,
                                color = SageDark,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Sanctuary Grid: Focus Timer & Water Tracker
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Focus Pomodoro Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTo(Screen.Focus.route) }
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Icon(
                            Icons.Rounded.Timer,
                            contentDescription = null,
                            tint = TerracottaClay,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Focus Timer",
                            style = MaterialTheme.typography.titleMedium,
                            color = CharcoalDeep
                        )
                        Text(
                            text = "Pomodoro & Flow",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalMuted
                        )
                    }
                }

                // Water Tracker Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTo(Screen.WaterTracker.route) }
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.WaterDrop,
                                contentDescription = null,
                                tint = SagePrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Surface(
                                shape = CircleShape,
                                color = SagePrimary,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable { viewModel.quickAddWater(250) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Rounded.Add,
                                        contentDescription = "+250ml",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${uiState.waterIntakeMl} / ${uiState.waterGoalMl} ml",
                            style = MaterialTheme.typography.titleMedium,
                            color = CharcoalDeep
                        )
                        Text(
                            text = "Hydration Log",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalMuted
                        )
                    }
                }
            }
        }

        // Today's Mindful Habits Preview
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = SanctuarySurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Rituals (${uiState.completedHabitsCount}/${uiState.totalHabitsCount})",
                            style = MaterialTheme.typography.titleMedium,
                            color = CharcoalDeep
                        )
                        Text(
                            text = "All Habits →",
                            style = MaterialTheme.typography.labelSmall,
                            color = SageDark,
                            modifier = Modifier.clickable { onNavigateTo(Screen.Habits.route) }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.habits.isEmpty()) {
                        Text(
                            text = "No rituals planned yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalMuted
                        )
                    } else {
                        uiState.habits.take(3).forEach { (habit, isDone) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Checkbox(
                                        checked = isDone,
                                        onCheckedChange = { checked ->
                                            viewModel.toggleHabit(habit.id, checked)
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = SagePrimary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = habit.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isDone) CharcoalMuted else CharcoalDeep
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Daily Motivational Wisdom
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = SanctuarySurfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SANCTUARY REFLECTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = SageDark,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\"${uiState.quote}\"",
                        fontFamily = FontFamily.Serif,
                        fontSize = 17.sp,
                        color = CharcoalDeep,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}


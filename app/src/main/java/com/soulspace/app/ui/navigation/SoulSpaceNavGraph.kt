package com.soulspace.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.soulspace.app.ui.screens.analytics.AnalyticsScreen
import com.soulspace.app.ui.screens.blocker.BlockerScreen
import com.soulspace.app.ui.screens.focus.FocusScreen
import com.soulspace.app.ui.screens.habits.HabitsScreen
import com.soulspace.app.ui.screens.home.HomeScreen
import com.soulspace.app.ui.screens.meditation.MeditationScreen
import com.soulspace.app.ui.screens.planner.PlannerScreen
import com.soulspace.app.ui.screens.profile.ProfileScreen
import com.soulspace.app.ui.screens.sounds.SoundsScreen
import com.soulspace.app.ui.screens.wellness.SleepScreen
import com.soulspace.app.ui.screens.wellness.WaterTrackerScreen
import com.soulspace.app.ui.theme.*

@Composable
fun SoulSpaceAppShell(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Focus,
        Screen.Habits,
        Screen.Sounds,
        Screen.Profile
    )

    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = SanctuaryCream,
                    tonalElevation = NavigationBarDefaults.Elevation
                ) {
                    bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                screen.icon?.let {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = screen.title,
                                        tint = if (isSelected) SagePrimary else CharcoalMuted
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) SageDark else CharcoalMuted
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = SageLight.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Main Bottom Tabs
            composable(Screen.Home.route) {
                HomeScreen(onNavigateTo = { route -> navController.navigate(route) })
            }
            composable(Screen.Focus.route) {
                FocusScreen(onNavigateToBlocker = { navController.navigate(Screen.Blocker.route) })
            }
            composable(Screen.Habits.route) {
                HabitsScreen()
            }
            composable(Screen.Sounds.route) {
                SoundsScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
            }

            // Sub-modules
            composable(Screen.Blocker.route) {
                BlockerScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Meditation.route) {
                MeditationScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Planner.route) {
                PlannerScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.WaterTracker.route) {
                WaterTrackerScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Sleep.route) {
                SleepScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToBlocker = { navController.navigate(Screen.Blocker.route) }
                )
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}


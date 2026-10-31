package com.soulspace.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    // Bottom Nav Tabs
    object Home : Screen("home", "Home", Icons.Rounded.Home)
    object Focus : Screen("focus", "Focus", Icons.Rounded.Timer)
    object Habits : Screen("habits", "Habits", Icons.Rounded.CheckCircleOutline)
    object Sounds : Screen("sounds", "Sounds", Icons.Rounded.GraphicEq)
    object Profile : Screen("profile", "Profile", Icons.Rounded.PersonOutline)

    // Sub-screens
    object Blocker : Screen("blocker", "App Blocker")
    object Meditation : Screen("meditation", "Meditation")
    object Planner : Screen("planner", "Timetable")
    object WaterTracker : Screen("water_tracker", "Hydration")
    object Sleep : Screen("sleep", "Sleep Schedule")
    object Analytics : Screen("analytics", "Screen Time")
}

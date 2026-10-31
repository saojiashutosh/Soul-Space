package com.soulspace.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import com.soulspace.app.data.preferences.UserPreferencesRepository
import com.soulspace.app.ui.navigation.SoulSpaceAppShell
import com.soulspace.app.ui.theme.SoulSpaceTheme

class MainActivity : ComponentActivity() {

    private lateinit var preferencesRepo: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferencesRepo = UserPreferencesRepository(applicationContext)

        setContent {
            val userPrefs by preferencesRepo.userPreferencesFlow.collectAsState(
                initial = com.soulspace.app.data.preferences.UserPreferences()
            )
            val isDark = userPrefs.darkTheme || isSystemInDarkTheme()

            SoulSpaceTheme(darkTheme = isDark) {
                SoulSpaceAppShell()
            }
        }
    }
}


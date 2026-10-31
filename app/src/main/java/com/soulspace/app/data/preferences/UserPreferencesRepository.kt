package com.soulspace.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "soulspace_user_preferences")

data class UserPreferences(
    val darkTheme: Boolean = false,
    val isCommitmentModeActive: Boolean = false,
    val commitmentEndTimeMillis: Long = 0L,
    val temporaryBlockEndTimeMillis: Long = 0L,
    val temporaryBlockLabel: String = "",
    val dailyWaterGoalMl: Int = 2000,
    val bedtimeHour: Int = 23,
    val bedtimeMinute: Int = 0,
    val wakeHour: Int = 7,
    val wakeMinute: Int = 0,
    val totalMindfulPoints: Int = 120,
    val hasCompletedOnboarding: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val COMMITMENT_ACTIVE = booleanPreferencesKey("commitment_active")
        val COMMITMENT_END_TIME = longPreferencesKey("commitment_end_time")
        val TEMP_BLOCK_END_TIME = longPreferencesKey("temp_block_end_time")
        val TEMP_BLOCK_LABEL = stringPreferencesKey("temp_block_label")
        val WATER_GOAL_ML = intPreferencesKey("water_goal_ml")
        val BEDTIME_HOUR = intPreferencesKey("bedtime_hour")
        val BEDTIME_MINUTE = intPreferencesKey("bedtime_minute")
        val WAKE_HOUR = intPreferencesKey("wake_hour")
        val WAKE_MINUTE = intPreferencesKey("wake_minute")
        val MINDFUL_POINTS = intPreferencesKey("mindful_points")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                darkTheme = preferences[PreferencesKeys.DARK_THEME] ?: false,
                isCommitmentModeActive = preferences[PreferencesKeys.COMMITMENT_ACTIVE] ?: false,
                commitmentEndTimeMillis = preferences[PreferencesKeys.COMMITMENT_END_TIME] ?: 0L,
                temporaryBlockEndTimeMillis = preferences[PreferencesKeys.TEMP_BLOCK_END_TIME] ?: 0L,
                temporaryBlockLabel = preferences[PreferencesKeys.TEMP_BLOCK_LABEL] ?: "",
                dailyWaterGoalMl = preferences[PreferencesKeys.WATER_GOAL_ML] ?: 2000,
                bedtimeHour = preferences[PreferencesKeys.BEDTIME_HOUR] ?: 23,
                bedtimeMinute = preferences[PreferencesKeys.BEDTIME_MINUTE] ?: 0,
                wakeHour = preferences[PreferencesKeys.WAKE_HOUR] ?: 7,
                wakeMinute = preferences[PreferencesKeys.WAKE_MINUTE] ?: 0,
                totalMindfulPoints = preferences[PreferencesKeys.MINDFUL_POINTS] ?: 120,
                hasCompletedOnboarding = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
            )
        }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DARK_THEME] = enabled }
    }

    suspend fun setTemporaryBlock(endTimeMillis: Long, label: String) {
        context.dataStore.edit {
            it[PreferencesKeys.TEMP_BLOCK_END_TIME] = endTimeMillis
            it[PreferencesKeys.TEMP_BLOCK_LABEL] = label
        }
    }

    suspend fun clearTemporaryBlock() {
        context.dataStore.edit {
            it[PreferencesKeys.TEMP_BLOCK_END_TIME] = 0L
            it[PreferencesKeys.TEMP_BLOCK_LABEL] = ""
        }
    }

    suspend fun setCommitmentMode(active: Boolean, endTimeMillis: Long) {
        context.dataStore.edit {
            it[PreferencesKeys.COMMITMENT_ACTIVE] = active
            it[PreferencesKeys.COMMITMENT_END_TIME] = endTimeMillis
        }
    }

    suspend fun setWaterGoal(goalMl: Int) {
        context.dataStore.edit { it[PreferencesKeys.WATER_GOAL_ML] = goalMl }
    }

    suspend fun setSleepSchedule(bedHour: Int, bedMin: Int, wakeH: Int, wakeM: Int) {
        context.dataStore.edit {
            it[PreferencesKeys.BEDTIME_HOUR] = bedHour
            it[PreferencesKeys.BEDTIME_MINUTE] = bedMin
            it[PreferencesKeys.WAKE_HOUR] = wakeH
            it[PreferencesKeys.WAKE_MINUTE] = wakeM
        }
    }

    suspend fun addMindfulPoints(points: Int) {
        context.dataStore.edit {
            val current = it[PreferencesKeys.MINDFUL_POINTS] ?: 120
            it[PreferencesKeys.MINDFUL_POINTS] = current + points
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETED] = completed }
    }
}


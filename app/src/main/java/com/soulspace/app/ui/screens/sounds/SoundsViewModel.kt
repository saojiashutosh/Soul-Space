package com.soulspace.app.ui.screens.sounds

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.soulspace.app.service.AudioPlaybackService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SoundscapeItem(
    val id: String,
    val title: String,
    val category: String, // Nature, Focus, Sleep, Meditation
    val iconName: String,
    val audioUri: String = "asset:///soundscapes/gentle_rain.mp3"
)

data class SoundsUiState(
    val currentPlayingId: String? = null,
    val currentCategory: String = "Nature",
    val sleepTimerMinutes: Int = 30,
    val isTimerActive: Boolean = false,
    val sounds: List<SoundscapeItem> = emptyList()
)

class SoundsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SoundsUiState())
    val uiState: StateFlow<SoundsUiState> = _uiState.asStateFlow()

    init {
        loadSoundscapes()
    }

    private fun loadSoundscapes() {
        val list = listOf(
            // Nature
            SoundscapeItem("s1", "Gentle Rain", "Nature", "rain"),
            SoundscapeItem("s2", "Ocean Tides", "Nature", "waves"),
            SoundscapeItem("s3", "Forest Ambience", "Nature", "forest"),
            SoundscapeItem("s4", "Mountain Stream", "Nature", "stream"),
            SoundscapeItem("s5", "Whispering Wind", "Nature", "wind"),
            // Focus
            SoundscapeItem("s6", "Brown Noise", "Focus", "noise"),
            SoundscapeItem("s7", "White Noise", "Focus", "noise"),
            SoundscapeItem("s8", "Ambient Focus Drone", "Focus", "ambient"),
            // Sleep
            SoundscapeItem("s9", "Midnight Rain", "Sleep", "rain"),
            SoundscapeItem("s10", "Distant Thunder", "Sleep", "thunder"),
            SoundscapeItem("s11", "Night Crickets", "Sleep", "crickets"),
            // Meditation
            SoundscapeItem("s12", "Tibetan Singing Bowl", "Meditation", "bowl"),
            SoundscapeItem("s13", "Zen Temple Bells", "Meditation", "bell"),
            SoundscapeItem("s14", "Deep Om Resonance", "Meditation", "om")
        )
        _uiState.update { it.copy(sounds = list) }
    }

    fun setCategory(category: String) {
        _uiState.update { it.copy(currentCategory = category) }
    }

    fun togglePlay(sound: SoundscapeItem) {
        val app = getApplication<Application>()
        if (_uiState.value.currentPlayingId == sound.id) {
            // Stop
            _uiState.update { it.copy(currentPlayingId = null) }
            val intent = Intent(app, AudioPlaybackService::class.java).apply {
                action = AudioPlaybackService.ACTION_STOP_SOUND
            }
            app.startService(intent)
        } else {
            // Play
            _uiState.update { it.copy(currentPlayingId = sound.id) }
            val intent = Intent(app, AudioPlaybackService::class.java).apply {
                action = AudioPlaybackService.ACTION_PLAY_SOUND
                putExtra(AudioPlaybackService.EXTRA_SOUND_URI, sound.audioUri)
                putExtra(AudioPlaybackService.EXTRA_SOUND_TITLE, sound.title)
            }
            ContextCompat.startForegroundService(app, intent)
        }
    }

    fun setSleepTimer(minutes: Int) {
        val app = getApplication<Application>()
        _uiState.update { it.copy(sleepTimerMinutes = minutes, isTimerActive = true) }
        val intent = Intent(app, AudioPlaybackService::class.java).apply {
            action = AudioPlaybackService.ACTION_SET_SLEEP_TIMER
            putExtra(AudioPlaybackService.EXTRA_SLEEP_MINUTES, minutes)
        }
        app.startService(intent)
    }
}


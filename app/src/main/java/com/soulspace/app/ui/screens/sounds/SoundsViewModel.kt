package com.soulspace.app.ui.screens.sounds

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soulspace.app.service.AudioPlaybackService
import com.soulspace.app.service.SoundMixer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SoundscapeItem(
    val id: String,
    val title: String,
    val category: String, // Nature, Focus, Sleep, Meditation
    val iconName: String,
    val audioUri: String = "asset:///soundscapes/$id.ogg"
)

data class SoundsUiState(
    /** Sound id -> volume (0..1) for every sound playing right now. */
    val playing: Map<String, Float> = emptyMap(),
    val currentCategory: String = "Nature",
    /** Selected sleep timer in minutes, or null when off. */
    val sleepTimerMinutes: Int? = null,
    val sounds: List<SoundscapeItem> = emptyList()
)

/** Every bundled soundscape. The ids match the real recordings in assets/soundscapes (see tools/fetch_real_sounds.py and CREDITS.md). */
object SoundCatalog {
    val all: List<SoundscapeItem> = listOf(
        // Nature
        SoundscapeItem("s15", "Gentle Rain", "Nature", "rain"),
        SoundscapeItem("s1", "Heavy Rain", "Nature", "rain"),
        SoundscapeItem("s2", "Ocean Tides", "Nature", "waves"),
        SoundscapeItem("s19", "Waterfall", "Nature", "stream"),
        SoundscapeItem("s3", "Forest Ambience", "Nature", "forest"),
        SoundscapeItem("s4", "Mountain Stream", "Nature", "stream"),
        SoundscapeItem("s5", "Whispering Wind", "Nature", "wind"),
        SoundscapeItem("s16", "Campfire", "Nature", "fire"),
        SoundscapeItem("s17", "Summer Cicadas", "Nature", "crickets"),
        SoundscapeItem("s20", "Dawn Birds", "Nature", "forest"),
        // Focus: steady, non-intrusive sounds with no voices or sudden events
        SoundscapeItem("s39", "Air Conditioner", "Focus", "noise"),
        SoundscapeItem("s9", "Rain on Window", "Focus", "rain"),
        SoundscapeItem("s8", "Trickling Water", "Focus", "stream"),
        // Sleep
        SoundscapeItem("s10", "Distant Thunder", "Sleep", "thunder"),
        SoundscapeItem("s11", "Night Pond", "Sleep", "crickets"),
        SoundscapeItem("s18", "Mars Wind", "Sleep", "wind"),
        SoundscapeItem("s23", "Heartbeat", "Sleep", "heart"),
        SoundscapeItem("s24", "Wind Chimes", "Sleep", "bell"),
        // Meditation
        SoundscapeItem("s12", "Singing Bowl", "Meditation", "bowl"),
        SoundscapeItem("s26", "Singing Bowl II", "Meditation", "bowl"),
        SoundscapeItem("s13", "Temple Bell", "Meditation", "bell"),
        SoundscapeItem("s27", "Temple Gong", "Meditation", "bell")
    )

    fun byId(id: String): SoundscapeItem? = all.firstOrNull { it.id == id }
}

class SoundsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SoundsUiState())
    val uiState: StateFlow<SoundsUiState> = _uiState.asStateFlow()

    init {
        loadSoundscapes()
        viewModelScope.launch {
            SoundMixer.active.collect { playing -> _uiState.update { it.copy(playing = playing) } }
        }
        viewModelScope.launch {
            SoundMixer.timerMinutes.collect { minutes -> _uiState.update { it.copy(sleepTimerMinutes = minutes) } }
        }
    }

    private fun loadSoundscapes() {
        _uiState.update { it.copy(sounds = SoundCatalog.all) }
    }

    fun setCategory(category: String) {
        _uiState.update { it.copy(currentCategory = category) }
    }

    /** Starts the sound alongside any others that are playing, or stops just this one. */
    fun togglePlay(sound: SoundscapeItem) {
        val app = getApplication<Application>()
        if (_uiState.value.playing.containsKey(sound.id)) {
            SoundMixer.stop(sound.id)
        } else {
            SoundMixer.play(app, sound.id, sound.title, sound.audioUri)
            ContextCompat.startForegroundService(
                app,
                Intent(app, AudioPlaybackService::class.java).setAction(AudioPlaybackService.ACTION_START)
            )
        }
    }

    fun setVolume(soundId: String, volume: Float) {
        SoundMixer.setVolume(soundId, volume)
    }

    fun stopAll() {
        SoundMixer.stopAll()
    }

    /** Tapping the selected duration again turns the timer off. */
    fun setSleepTimer(minutes: Int) {
        SoundMixer.setSleepTimer(if (SoundMixer.timerMinutes.value == minutes) null else minutes)
    }
}

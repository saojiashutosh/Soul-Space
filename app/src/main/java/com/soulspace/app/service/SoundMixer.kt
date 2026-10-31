package com.soulspace.app.service

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Plays any number of looping soundscapes at once, each with its own volume.
 * Everything runs on the main thread (ExoPlayer's application looper).
 */
object SoundMixer {

    const val DEFAULT_VOLUME = 0.6f

    private val players = LinkedHashMap<String, ExoPlayer>()
    private val volumes = LinkedHashMap<String, Float>()
    private val titles = LinkedHashMap<String, String>()

    private val _active = MutableStateFlow<Map<String, Float>>(emptyMap())
    /** Sound id -> volume (0..1) for every sound currently playing, in start order. */
    val active: StateFlow<Map<String, Float>> = _active.asStateFlow()

    private val _timerMinutes = MutableStateFlow<Int?>(null)
    /** Selected sleep timer; counts down while at least one sound is playing. */
    val timerMinutes: StateFlow<Int?> = _timerMinutes.asStateFlow()

    /** A running guided meditation: background sound + countdown, ended by the sleep-timer fade. */
    data class Session(val id: String, val title: String, val totalMillis: Long, val endsAtElapsedMillis: Long)

    private val _session = MutableStateFlow<Session?>(null)
    val session: StateFlow<Session?> = _session.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private var timerJob: Job? = null
    private var master = 1f
    private var appContext: Context? = null

    fun playingTitles(): List<String> = titles.values.toList()

    fun play(context: Context, id: String, title: String, uri: String, volume: Float = DEFAULT_VOLUME) {
        if (players.containsKey(id)) return
        appContext = context.applicationContext
        val player = ExoPlayer.Builder(context.applicationContext).build().apply {
            // No audio focus handling: each player would otherwise steal focus from the others.
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                false
            )
            repeatMode = Player.REPEAT_MODE_ONE
            this.volume = volume * master
            addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    stop(id)
                }
            })
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
        }
        players[id] = player
        volumes[id] = volume
        titles[id] = title
        publish()
        startTimerIfSelected()
    }

    fun stop(id: String) {
        players.remove(id)?.release()
        volumes.remove(id)
        titles.remove(id)
        if (players.isEmpty()) clearTimer()
        publish()
    }

    fun stopAll() {
        players.values.forEach { it.release() }
        players.clear()
        volumes.clear()
        titles.clear()
        clearTimer()
        publish()
    }

    /** Replaces the current mix with one background sound for [minutes], chiming at the start and the end. */
    fun startSession(
        context: Context,
        sessionId: String,
        sessionTitle: String,
        soundId: String,
        soundTitle: String,
        soundUri: String,
        minutes: Int
    ) {
        stopAll()
        play(context, soundId, soundTitle, soundUri, volume = 0.5f)
        setSleepTimer(minutes)
        val total = minutes * 60_000L
        _session.value = Session(sessionId, sessionTitle, total, android.os.SystemClock.elapsedRealtime() + total)
        playBell(context)
    }

    /** One bell strike on its own player; never shows up in the mix. */
    fun playBell(context: Context) {
        val bell = ExoPlayer.Builder(context.applicationContext).build()
        bell.setAudioAttributes(
            AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(),
            false
        )
        bell.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) bell.release()
            }

            override fun onPlayerError(error: PlaybackException) {
                bell.release()
            }
        })
        bell.setMediaItem(MediaItem.fromUri("asset:///soundscapes/bell.ogg"))
        bell.prepare()
        bell.playWhenReady = true
    }

    fun setVolume(id: String, volume: Float) {
        val v = volume.coerceIn(0f, 1f)
        if (!players.containsKey(id)) return
        volumes[id] = v
        players[id]?.volume = v * master
        publish()
    }

    /** Pass null to cancel. The countdown starts now if something is playing, else on the next play. */
    fun setSleepTimer(minutes: Int?) {
        timerJob?.cancel()
        timerJob = null
        master = 1f
        applyMaster()
        _session.value = null
        _timerMinutes.value = minutes
        startTimerIfSelected()
    }

    private fun startTimerIfSelected() {
        val minutes = _timerMinutes.value ?: return
        if (timerJob?.isActive == true || players.isEmpty()) return
        timerJob = scope.launch {
            val total = minutes * 60_000L
            val fade = minOf(60_000L, total)
            delay(total - fade)
            val steps = 20
            repeat(steps) { i ->
                master = 1f - (i + 1) / steps.toFloat()
                applyMaster()
                delay(fade / steps)
            }
            val sessionFinished = _session.value != null
            stopAll()
            if (sessionFinished) appContext?.let { playBell(it) }
        }
    }

    private fun clearTimer() {
        timerJob?.cancel()
        timerJob = null
        master = 1f
        _timerMinutes.value = null
        _session.value = null
    }

    private fun applyMaster() {
        players.forEach { (id, player) -> player.volume = (volumes[id] ?: DEFAULT_VOLUME) * master }
    }

    private fun publish() {
        _active.value = LinkedHashMap(volumes)
    }
}

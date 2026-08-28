package com.mentricstudios.cidquest.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.mentricstudios.cidquest.R

/**
 * App-wide audio: a small [SoundPool] for short one-shot effects (button
 * clicks, level-complete, guard-catch) and a single looping [MediaPlayer]
 * for the gameplay background track. Both are lazily created once and
 * reused for the life of the process instead of per-screen, so tapping
 * buttons rapidly never stutters waiting on a fresh player to spin up.
 *
 * SFX always play; the extra strong vibration on a guard-catch is the only
 * part gated on [SettingsPrefs.isVibrationEnabled], matching every other
 * haptic already in the game.
 *
 * [SoundPool.load] decodes on a background thread — calling [SoundPool.play]
 * before that finishes is a silent no-op, which is exactly the "the catch
 * sound doesn't play" bug this used to have the first time a sound was
 * needed. [onLoadComplete] tracks which sound IDs have actually finished
 * decoding and replays anything requested too early once it's ready.
 */
object SoundManager {

    private var soundPool: SoundPool? = null
    private var clickSoundId: Int = 0
    private var rewardSoundId: Int = 0
    private var wrongSoundId: Int = 0
    private var gameOverSoundId: Int = 0
    // Wide-radius "something's nearby" ambient cue — no AI change, just
    // atmosphere. Kept separate from the chase/evaded sounds below so an
    // ambient ping never gets confused for the real chase trigger.
    private val ambientSoundIds = IntArray(2)
    private var chaseStartSoundId: Int = 0
    private var evadedSoundId: Int = 0
    private var poolInitialized = false

    private val loadedSoundIds = mutableSetOf<Int>()
    private val pendingPlays = mutableSetOf<Int>()

    private var bgPlayer: MediaPlayer? = null

    /**
     * Monotonically increasing "ownership" token for the background track.
     *
     * Every call to [startBackgroundMusic] bumps this, even when an already-
     * playing track is left alone. That second part matters: when the user
     * moves from one enemy/ice level straight into another, Compose doesn't
     * guarantee whether the outgoing screen's `onDispose` or the incoming
     * screen's `DisposableEffect` runs first.
     *
     * If the incoming screen starts first, it sees the track already
     * playing and (correctly) does nothing to the [MediaPlayer] itself, but
     * it still grabs a fresh token. When the outgoing screen's `onDispose`
     * runs afterward and calls [stopBackgroundMusic] with its *old* token,
     * that token no longer matches [playToken], so the stop is ignored —
     * the still-current owner's music survives. Without this, the old
     * screen's dispose would kill the shared player out from under the new
     * screen, which is exactly the "heartbeat sometimes doesn't play" bug:
     * it depended on transition timing, so it only happened sometimes.
     */
    private var playToken = 0

    private fun ensurePool(context: Context) {
        if (poolInitialized) return
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val pool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attributes)
            .build()
        pool.setOnLoadCompleteListener { sp, sampleId, status ->
            if (status == 0) {
                loadedSoundIds.add(sampleId)
                if (pendingPlays.remove(sampleId)) {
                    sp.play(sampleId, 1f, 1f, 1, 0, 1f)
                }
            }
        }
        soundPool = pool
        val appContext = context.applicationContext
        clickSoundId = pool.load(appContext, R.raw.sfx_click, 1)
        rewardSoundId = pool.load(appContext, R.raw.sfx_reward, 1)
        wrongSoundId = pool.load(appContext, R.raw.sfx_wrong, 1)
        gameOverSoundId = pool.load(appContext, R.raw.sfx_game_over, 1)
        ambientSoundIds[0] = pool.load(appContext, R.raw.sfx_spotted_1, 1)
        ambientSoundIds[1] = pool.load(appContext, R.raw.sfx_spotted_3, 1)
        chaseStartSoundId = pool.load(appContext, R.raw.sfx_spotted_2, 1)
        evadedSoundId = pool.load(appContext, R.raw.sfx_evaded, 1)
        poolInitialized = true
    }

    /** Plays [soundId] now if it's already decoded; otherwise marks it to
     * play the instant its load finishes, instead of silently dropping it. */
    private fun playWhenReady(soundId: Int, volume: Float) {
        val pool = soundPool ?: return
        if (soundId in loadedSoundIds) {
            pool.play(soundId, volume, volume, 1, 0, 1f)
        } else {
            pendingPlays.add(soundId)
        }
    }

    fun playClick(context: Context) {
        if (!SettingsPrefs.isSoundEnabled(context)) return
        ensurePool(context)
        playWhenReady(clickSoundId, 0.6f)
    }

    fun playReward(context: Context) {
        if (!SettingsPrefs.isSoundEnabled(context)) return
        ensurePool(context)
        playWhenReady(rewardSoundId, 1f)
    }

    /** Plays the "caught" sting and, if the player hasn't disabled vibration
     * in Settings, a strong jolt distinct from the game's normal light taps. */
    fun playWrong(context: Context) {
        if (SettingsPrefs.isSoundEnabled(context)) {
            ensurePool(context)
            playWhenReady(wrongSoundId, 1f)
        }
        if (SettingsPrefs.isVibrationEnabled(context)) {
            strongVibrate(context)
        }
    }

    /** Short musical stinger that follows [playWrong] once the Caught overlay appears. */
    fun playGameOver(context: Context) {
        if (!SettingsPrefs.isSoundEnabled(context)) return
        ensurePool(context)
        playWhenReady(gameOverSoundId, 1f)
    }

    /** Wide-radius "something's nearby" ambient cue — a guard is in the
     * general area but hasn't actually detected the player yet. One of 2
     * recorded variants, chosen at random so it doesn't feel identical
     * every time. No AI behavior change accompanies this one. */
    fun playSpotted(context: Context) {
        if (!SettingsPrefs.isSoundEnabled(context)) return
        ensurePool(context)
        playWhenReady(ambientSoundIds[kotlin.random.Random.nextInt(ambientSoundIds.size)], 0.85f)
    }

    /** A guard has actually detected the player and is now actively chasing. */
    fun playChaseStart(context: Context) {
        if (!SettingsPrefs.isSoundEnabled(context)) return
        ensurePool(context)
        playWhenReady(chaseStartSoundId, 1f)
    }

    /** The player stayed clear of a chasing guard long enough that it gave up. */
    fun playEvaded(context: Context) {
        if (!SettingsPrefs.isSoundEnabled(context)) return
        ensurePool(context)
        playWhenReady(evadedSoundId, 0.9f)
    }

    private var loadingPlayer: MediaPlayer? = null

    /** One-shot jingle for the Loading screen — not looped, plays once per app launch. */
    fun playLoadingMusic(context: Context) {
        if (!SettingsPrefs.isSoundEnabled(context)) return
        loadingPlayer?.release()
        loadingPlayer = MediaPlayer.create(context.applicationContext, R.raw.music_loading)
        loadingPlayer?.setVolume(0.5f, 0.5f)
        loadingPlayer?.setOnCompletionListener {
            it.release()
            loadingPlayer = null
        }
        loadingPlayer?.start()
    }

    private fun strongVibrate(context: Context) {
        val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vibrator == null || !vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Two sharp, near-max-amplitude pulses read as "caution/failure"
            // much more clearly than a single flat buzz.
            val pattern = longArrayOf(0, 140, 80, 220)
            val amplitudes = intArrayOf(0, 255, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 140, 80, 220), -1)
        }
    }

    /** Starts the looping gameplay background track at low volume — it's
     * meant to sit under the SFX as tension, not compete with them. Safe to
     * call repeatedly; a track already playing is left alone rather than
     * restarted.
     *
     * Returns a token identifying this call as the current "owner" of the
     * background track. Callers that later want to stop the music (e.g. a
     * screen's `onDispose`) should pass that token back into
     * [stopBackgroundMusic] so a stale caller can't silence a track that a
     * newer screen has since taken ownership of — see [playToken]. */
    fun startBackgroundMusic(context: Context, volume: Float = 0.22f): Int {
        playToken++
        val token = playToken
        if (!SettingsPrefs.isSoundEnabled(context)) return token
        if (bgPlayer?.isPlaying == true) return token
        stopBackgroundMusicInternal()
        val player = MediaPlayer.create(context.applicationContext, R.raw.bg_heartbeat) ?: return token
        player.isLooping = true
        player.setVolume(volume, volume)
        player.start()
        bgPlayer = player
        return token
    }

    /**
     * Stops the background track, but only if [token] still matches the
     * most recent [startBackgroundMusic] call (or [token] is null, for call
     * sites that intentionally want an unconditional stop, e.g. app
     * backgrounding). Pass the token you got back from [startBackgroundMusic]
     * from a `DisposableEffect`'s `onDispose` so an outgoing screen can never
     * cut off music that a newer, still-active screen owns.
     */
    fun stopBackgroundMusic(token: Int? = null) {
        if (token != null && token != playToken) return
        stopBackgroundMusicInternal()
    }

    private fun stopBackgroundMusicInternal() {
        bgPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        bgPlayer = null
    }

    fun pauseBackgroundMusic() {
        bgPlayer?.let { if (it.isPlaying) it.pause() }
    }

    fun resumeBackgroundMusic() {
        bgPlayer?.let { if (!it.isPlaying) it.start() }
    }
}

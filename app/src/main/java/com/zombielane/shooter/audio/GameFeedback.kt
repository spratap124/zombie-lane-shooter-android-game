package com.zombielane.shooter.audio

import android.content.Context
import com.zombielane.shooter.R
import com.zombielane.shooter.data.SettingsManager
import com.zombielane.shooter.objects.PowerUpType

/**
 * Single entry point for gameplay SFX + haptics. Respects [SettingsManager] flags.
 *
 * Combo pitch: [comboPitch] = `1f + combo * 0.05f` clamped for hit/kill.
 */
class GameFeedback(
    context: Context,
    private val settings: SettingsManager
) {

    private val vibrator = VibrationManager(context.applicationContext, settings)

    private var lastShootSoundMs: Long = 0L

    init {
        val app = context.applicationContext
        SoundManager.init(app, settings)
        MusicManager.init(app, settings)
    }

    companion object {
        private const val SHOOT_DEBOUNCE_MS = 45L
        private const val COMBO_PITCH_STEP = 0.05f
        private const val HIT_PITCH_MAX = 1.5f
        private const val KILL_PITCH_MAX = 2f
    }

    fun comboPitch(combo: Int): Float =
        (1f + combo * COMBO_PITCH_STEP).coerceIn(1f, KILL_PITCH_MAX)

    private fun hitPitch(combo: Int): Float =
        (1f + combo * COMBO_PITCH_STEP).coerceIn(1f, HIT_PITCH_MAX)

    // ── Hooks ─────────────────────────────────────────────

    fun onShoot() {
        val now = android.os.SystemClock.uptimeMillis()
        if (now - lastShootSoundMs < SHOOT_DEBOUNCE_MS) return
        lastShootSoundMs = now
        if (settings.soundEnabled) {
            SoundManager.play(SoundManager.SHOOT, volume = 0.55f, pitch = 1f)
        }
        vibrator.light()
    }

    fun onEnemyHit(combo: Int = 0) {
        if (settings.soundEnabled) {
            SoundManager.play(SoundManager.HIT, volume = 0.7f, pitch = hitPitch(combo))
        }
        vibrator.light()
    }

    fun onEnemyKill(combo: Int) {
        if (settings.soundEnabled) {
            SoundManager.play(SoundManager.KILL, volume = 0.85f, pitch = comboPitch(combo))
        }
        vibrator.medium()
    }

    fun onCoinCollect() {
        if (settings.soundEnabled) {
            SoundManager.play(SoundManager.COIN, volume = 0.75f, pitch = 1f)
        }
        vibrator.light()
    }

    fun onPowerUp(type: PowerUpType) {
        if (settings.soundEnabled) {
            SoundManager.play(SoundManager.POWERUP, volume = 0.9f, pitch = 1f)
        }
        vibrator.medium()
        if (type == PowerUpType.BOMB) {
            if (settings.soundEnabled) {
                SoundManager.play(SoundManager.EXPLOSION, volume = 1f, pitch = 1f)
            }
            vibrator.heavy()
        }
    }

    fun onBossHit(combo: Int = 0) {
        if (settings.soundEnabled) {
            SoundManager.play(SoundManager.HIT, volume = 0.9f, pitch = (hitPitch(combo) * 0.85f).coerceIn(0.5f, 1.4f))
        }
        vibrator.medium()
    }

    fun onGameOver() {
        if (settings.soundEnabled) {
            SoundManager.play(SoundManager.GAMEOVER, volume = 1f, pitch = 1f)
        }
        vibrator.heavy()
    }

    // ── Music ─────────────────────────────────────────────

    fun startMenuMusic(context: Context) {
        MusicManager.play(context, R.raw.bgm_game, loop = true)
    }

    /** Starts BGM if music is enabled and nothing is already playing (e.g. returning to menu). */
    fun ensureMenuMusic(context: Context) {
        if (!MusicManager.isMusicEnabled) return
        if (MusicManager.isPlaying()) return
        MusicManager.play(context, R.raw.bgm_game, loop = true)
    }

    fun pauseMusic() = MusicManager.pause()

    fun resumeMusic() = MusicManager.resume()

    fun syncMusicWithSettings() = MusicManager.syncWithSettings()

    /** Chest / UI pulse (optional). */
    fun onUiRewardPulse() {
        vibrator.medium()
    }

    fun release() {
        SoundManager.release()
        MusicManager.release()
    }
}

package com.zombielane.shooter.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import androidx.annotation.RawRes
import com.zombielane.shooter.R
import com.zombielane.shooter.data.SettingsManager

/**
 * Low-latency SFX via [SoundPool]. Preload at startup; call [release] from [android.app.Activity.onDestroy].
 */
object SoundManager {

    private const val TAG = "SoundManager"

    const val SHOOT = "shoot"
    const val HIT = "hit"
    const val KILL = "kill"
    const val COIN = "coin"
    const val POWERUP = "powerup"
    const val EXPLOSION = "explosion"
    const val GAMEOVER = "gameover"

    private val keyToRes: Map<String, Int> = mapOf(
        SHOOT to R.raw.sfx_shoot,
        HIT to R.raw.sfx_hit,
        KILL to R.raw.sfx_kill,
        COIN to R.raw.sfx_coin,
        POWERUP to R.raw.sfx_powerup,
        EXPLOSION to R.raw.sfx_explosion,
        GAMEOVER to R.raw.sfx_gameover
    )

    @Volatile
    private var appContext: Context? = null

    @Volatile
    private var settings: SettingsManager? = null

    private var pool: SoundPool? = null
    private val sampleIds = HashMap<String, Int>(keyToRes.size)
    private val loaded = HashMap<String, Boolean>(keyToRes.size)
    private val sampleIdToKey = HashMap<Int, String>(keyToRes.size)

    @Volatile
    private var released = false

    fun init(context: Context, settingsManager: SettingsManager) {
        if (released) released = false
        if (pool != null) {
            settings = settingsManager
            return
        }
        appContext = context.applicationContext
        settings = settingsManager

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val p = SoundPool.Builder()
            .setMaxStreams(16)
            .setAudioAttributes(attrs)
            .build()

        p.setOnLoadCompleteListener { _, sampleId, status ->
            if (status != 0) {
                Log.w(TAG, "Sound load failed status=$status id=$sampleId")
                return@setOnLoadCompleteListener
            }
            synchronized(sampleIds) {
                sampleIdToKey[sampleId]?.let { key -> loaded[key] = true }
            }
        }

        val ctx = appContext!!
        for ((key, resId) in keyToRes) {
            try {
                val id = p.load(ctx, resId, 1)
                if (id > 0) {
                    sampleIds[key] = id
                    sampleIdToKey[id] = key
                    loaded[key] = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load $key", e)
            }
        }
        pool = p
    }

    /** Whether user allows SFX (live read from [SettingsManager]). */
    val isSoundEnabled: Boolean
        get() = settings?.soundEnabled != false

    /**
     * Plays a preloaded sample. [pitch] is SoundPool rate in [0.5f, 2f] (1 = normal).
     */
    fun play(soundKey: String, volume: Float = 1f, pitch: Float = 1f) {
        if (!isSoundEnabled) return
        val p = pool ?: return
        val sid = synchronized(sampleIds) { sampleIds[soundKey] } ?: return
        if (loaded[soundKey] != true) return
        val v = volume.coerceIn(0f, 1f)
        val rate = pitch.coerceIn(0.5f, 2f)
        p.play(sid, v, v, 1, 0, rate)
    }

    fun playRaw(@RawRes resId: Int, volume: Float = 1f, pitch: Float = 1f) {
        val entry = keyToRes.entries.find { it.value == resId } ?: return
        play(entry.key, volume, pitch)
    }

    fun release() {
        synchronized(sampleIds) {
            pool?.release()
            pool = null
            sampleIds.clear()
            sampleIdToKey.clear()
            loaded.clear()
            appContext = null
            settings = null
            released = true
        }
    }
}

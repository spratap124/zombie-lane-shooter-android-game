package com.zombielane.shooter.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.annotation.RawRes
import com.zombielane.shooter.data.SettingsManager

/**
 * Background music via [MediaPlayer]. Pause/resume with activity lifecycle.
 */
object MusicManager {

    private const val TAG = "MusicManager"

    @Volatile
    private var appContext: Context? = null

    @Volatile
    private var settings: SettingsManager? = null

    private var player: MediaPlayer? = null

    @Volatile
    private var currentResId: Int = 0

    @Volatile
    private var wasPlayingBeforePause: Boolean = false

    fun init(context: Context, settingsManager: SettingsManager) {
        appContext = context.applicationContext
        settings = settingsManager
    }

    val isMusicEnabled: Boolean
        get() = settings?.musicEnabled != false

    /**
     * Starts (or restarts) playback from [resId]. Stops any current track.
     */
    fun play(context: Context, @RawRes resId: Int, loop: Boolean = true) {
        val sm = settings ?: return
        appContext = context.applicationContext
        stop()
        if (!sm.musicEnabled) {
            currentResId = resId
            return
        }
        try {
            val ctx = appContext!!
            val mp = MediaPlayer.create(ctx, resId) ?: run {
                Log.e(TAG, "MediaPlayer.create failed for resId=$resId")
                return
            }
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            mp.isLooping = loop
            mp.setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error what=$what extra=$extra")
                true
            }
            player = mp
            currentResId = resId
            mp.start()
            wasPlayingBeforePause = true
        } catch (e: Exception) {
            Log.e(TAG, "play failed", e)
        }
    }

    fun pause() {
        val mp = player ?: return
        try {
            if (mp.isPlaying) {
                mp.pause()
                wasPlayingBeforePause = true
            } else {
                wasPlayingBeforePause = false
            }
        } catch (e: Exception) {
            Log.w(TAG, "pause", e)
        }
    }

    fun resume() {
        if (!isMusicEnabled) return
        val mp = player ?: return
        try {
            if (wasPlayingBeforePause && !mp.isPlaying) {
                mp.start()
            }
        } catch (e: Exception) {
            Log.w(TAG, "resume", e)
        }
    }

    fun isPlaying(): Boolean = player?.isPlaying == true

    fun stop() {
        val mp = player ?: return
        try {
            mp.stop()
        } catch (_: Exception) { }
        try {
            mp.reset()
            mp.release()
        } catch (_: Exception) { }
        player = null
        wasPlayingBeforePause = false
    }

    /** Call when the user toggles music in settings. */
    fun syncWithSettings() {
        if (!isMusicEnabled) {
            wasPlayingBeforePause = false
            stop()
            return
        }
        val res = currentResId
        val ctx = appContext ?: return
        if (res != 0 && player == null) {
            play(ctx, res, loop = true)
        } else {
            resume()
        }
    }

    fun release() {
        stop()
        currentResId = 0
        settings = null
        appContext = null
    }
}

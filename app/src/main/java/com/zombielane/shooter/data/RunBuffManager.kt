package com.zombielane.shooter.data

import android.content.Context
import android.content.SharedPreferences
import com.zombielane.shooter.objects.Player

class RunBuffManager(context: Context) {

    companion object {
        private const val PREFS = "zombie_lane_run_buffs"
        private const val KEY_SHIELD = "next_shield"
        private const val KEY_RAPID_MS = "next_rapid_ms"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun addNextRunShield() {
        prefs.edit().putBoolean(KEY_SHIELD, true).apply()
    }

    fun addNextRunRapid(ms: Long) {
        if (ms <= 0L) return
        val cur = prefs.getLong(KEY_RAPID_MS, 0L)
        prefs.edit().putLong(KEY_RAPID_MS, maxOf(cur, ms)).apply()
    }

    fun applyToPlayer(player: Player) {
        val now = System.currentTimeMillis()
        if (prefs.getBoolean(KEY_SHIELD, false)) {
            player.shielded = true
            prefs.edit().putBoolean(KEY_SHIELD, false).apply()
        }
        val rapid = prefs.getLong(KEY_RAPID_MS, 0L)
        if (rapid > 0L) {
            player.rapidFireUntilMs = now + rapid
            prefs.edit().putLong(KEY_RAPID_MS, 0L).apply()
        }
    }

    fun resetProgress() {
        prefs.edit().clear().apply()
    }
}

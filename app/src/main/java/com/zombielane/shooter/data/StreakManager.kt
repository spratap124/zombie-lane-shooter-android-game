package com.zombielane.shooter.data

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar

/**
 * Tracks consecutive calendar-day logins and grants milestone chests (day 1 / 3 / 7).
 */
class StreakManager(context: Context) {

    companion object {
        private const val PREFS = "zombie_lane_streak"
        private const val KEY_LAST_DAY_START = "last_login_day_start_ms"
        private const val KEY_STREAK = "streak_count"
        private const val KEY_CLAIMED_MASK = "claimed_milestone_mask"
        private const val DAY_MS = 24L * 60 * 60 * 1000
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    data class StreakPopup(
        val title: String,
        val message: String,
        val streak: Int
    )

    private fun startOfTodayMs(): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun onMenuEnter(chestManager: ChestManager): StreakPopup? {
        val todayStart = startOfTodayMs()
        val lastStart = prefs.getLong(KEY_LAST_DAY_START, 0L)
        if (lastStart == todayStart) return null

        var streak = prefs.getInt(KEY_STREAK, 0)
        var claimed = prefs.getInt(KEY_CLAIMED_MASK, 0)

        when {
            lastStart == 0L -> streak = 1
            todayStart - lastStart == DAY_MS -> streak++
            todayStart > lastStart + DAY_MS -> {
                streak = 1
                claimed = 0
            }
            else -> streak = 1
        }

        prefs.edit()
            .putLong(KEY_LAST_DAY_START, todayStart)
            .putInt(KEY_STREAK, streak)
            .apply()

        var popup: StreakPopup? = null

        fun grantMilestone(bitDay: Int, type: ChestType, title: String, msg: String) {
            val bit = 1 shl bitDay
            if (claimed and bit != 0) return
            when (chestManager.tryAddChest(type)) {
                ChestGrantResult.ADDED -> {
                    claimed = claimed or bit
                    prefs.edit().putInt(KEY_CLAIMED_MASK, claimed).apply()
                    popup = StreakPopup(title, msg, streak)
                }
                ChestGrantResult.SLOTS_FULL -> {
                    popup = StreakPopup(
                        "Streak $streak days!",
                        "Chest slots full — open room for streak rewards!",
                        streak
                    )
                }
            }
        }

        when (streak) {
            1 -> grantMilestone(1, ChestType.COMMON, "Day 1 streak!", "Daily bonus: Common chest added.")
            3 -> grantMilestone(3, ChestType.RARE, "3-day streak!", "Rare chest added to your inventory.")
            7 -> grantMilestone(7, ChestType.EPIC, "7-day streak!", "Epic chest added — legendary dedication!")
        }

        return popup
    }

    fun currentStreak(): Int = prefs.getInt(KEY_STREAK, 0)

    fun resetProgress() {
        prefs.edit().clear().apply()
    }
}

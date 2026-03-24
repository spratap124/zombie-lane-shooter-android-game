package com.zombielane.shooter.data

import android.content.Context
import android.content.SharedPreferences

class UpgradeManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "zombie_lane_prefs"
        private const val KEY_COINS = "coins"
        private const val KEY_DAMAGE_LEVEL = "upgrade_damage"
        private const val KEY_FIRE_RATE_LEVEL = "upgrade_fire_rate"
        private const val KEY_HEALTH_LEVEL = "upgrade_health"
        private const val KEY_HIGH_SCORE = "high_score"

        private const val BASE_DAMAGE = 1
        private const val BASE_FIRE_INTERVAL_MS = 130L
        private const val BASE_MAX_HEALTH = 3

        private const val DAMAGE_PER_LEVEL = 1
        private const val FIRE_RATE_REDUCTION_MS = 10L
        private const val MIN_FIRE_INTERVAL_MS = 50L
        private const val HEALTH_PER_LEVEL = 1
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var totalCoins: Int
        get() = prefs.getInt(KEY_COINS, 0)
        set(value) = prefs.edit().putInt(KEY_COINS, value).apply()

    var highScore: Int
        get() = prefs.getInt(KEY_HIGH_SCORE, 0)
        set(value) = prefs.edit().putInt(KEY_HIGH_SCORE, value).apply()

    val damageLevel: Int get() = prefs.getInt(KEY_DAMAGE_LEVEL, 0)
    val fireRateLevel: Int get() = prefs.getInt(KEY_FIRE_RATE_LEVEL, 0)
    val healthLevel: Int get() = prefs.getInt(KEY_HEALTH_LEVEL, 0)

    val damage: Int get() = BASE_DAMAGE + damageLevel * DAMAGE_PER_LEVEL

    val fireIntervalMs: Long
        get() = (BASE_FIRE_INTERVAL_MS - fireRateLevel * FIRE_RATE_REDUCTION_MS)
            .coerceAtLeast(MIN_FIRE_INTERVAL_MS)

    val maxHealth: Int get() = BASE_MAX_HEALTH + healthLevel * HEALTH_PER_LEVEL

    fun upgradeCost(type: UpgradeType): Int {
        val level = when (type) {
            UpgradeType.DAMAGE -> damageLevel
            UpgradeType.FIRE_RATE -> fireRateLevel
            UpgradeType.HEALTH -> healthLevel
        }
        return 20 + level * 15
    }

    fun canAfford(type: UpgradeType): Boolean = totalCoins >= upgradeCost(type)

    fun purchase(type: UpgradeType): Boolean {
        if (!canAfford(type)) return false
        val cost = upgradeCost(type)
        totalCoins -= cost

        val key = when (type) {
            UpgradeType.DAMAGE -> KEY_DAMAGE_LEVEL
            UpgradeType.FIRE_RATE -> KEY_FIRE_RATE_LEVEL
            UpgradeType.HEALTH -> KEY_HEALTH_LEVEL
        }
        prefs.edit().putInt(key, prefs.getInt(key, 0) + 1).apply()
        return true
    }

    fun statSummary(type: UpgradeType): String = when (type) {
        UpgradeType.DAMAGE -> "DMG ${damage} → ${damage + DAMAGE_PER_LEVEL}"
        UpgradeType.FIRE_RATE -> {
            val currentMs = fireIntervalMs
            val nextMs = (currentMs - FIRE_RATE_REDUCTION_MS).coerceAtLeast(MIN_FIRE_INTERVAL_MS)
            "${currentMs}ms → ${nextMs}ms"
        }
        UpgradeType.HEALTH -> "HP ${maxHealth} → ${maxHealth + HEALTH_PER_LEVEL}"
    }

    fun resetProgress() {
        prefs.edit().clear().apply()
    }

    enum class UpgradeType { DAMAGE, FIRE_RATE, HEALTH }
}

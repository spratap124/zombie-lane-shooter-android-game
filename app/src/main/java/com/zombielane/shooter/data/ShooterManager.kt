package com.zombielane.shooter.data

import android.content.Context
import android.content.SharedPreferences
import com.zombielane.shooter.objects.Bullet
import kotlin.math.cos
import kotlin.math.sin

class ShooterManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "zombie_lane_shooters"
        private const val KEY_EQUIPPED = "equipped_shooter"
        private const val KEY_UNLOCKED = "unlocked_shooters"
        private const val TEMP_KEY_PREFIX = "temp_expiry_"
        const val TEMP_DURATION_MS = 300_000L
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var equipped: ShooterType
        get() = try {
            ShooterType.valueOf(prefs.getString(KEY_EQUIPPED, ShooterType.BASIC.name)!!)
        } catch (_: Exception) {
            ShooterType.BASIC
        }
        set(value) = prefs.edit().putString(KEY_EQUIPPED, value.name).apply()

    fun getEquipped(): Shooter = Shooter.get(equipped)

    fun isUnlocked(type: ShooterType): Boolean {
        if (type == ShooterType.BASIC) return true
        val unlocked = prefs.getStringSet(KEY_UNLOCKED, emptySet()) ?: emptySet()
        return type.name in unlocked
    }

    fun isTemporaryActive(type: ShooterType): Boolean {
        val expiry = prefs.getLong(TEMP_KEY_PREFIX + type.name, 0L)
        return expiry > System.currentTimeMillis()
    }

    fun getRemainingTempMs(type: ShooterType): Long {
        val expiry = prefs.getLong(TEMP_KEY_PREFIX + type.name, 0L)
        return (expiry - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    fun isAvailable(type: ShooterType): Boolean = isUnlocked(type) || isTemporaryActive(type)

    fun unlock(type: ShooterType, upgradeManager: UpgradeManager): Boolean {
        val shooter = Shooter.get(type)
        if (upgradeManager.totalCoins < shooter.unlockCost) return false
        upgradeManager.totalCoins -= shooter.unlockCost
        val current = prefs.getStringSet(KEY_UNLOCKED, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(type.name)
        prefs.edit().putStringSet(KEY_UNLOCKED, current).apply()
        return true
    }

    fun unlockTemporarily(type: ShooterType, durationMs: Long = TEMP_DURATION_MS) {
        val expiry = System.currentTimeMillis() + durationMs
        prefs.edit().putLong(TEMP_KEY_PREFIX + type.name, expiry).apply()
    }

    fun equip(type: ShooterType) {
        if (isAvailable(type)) equipped = type
    }

    fun checkExpiry() {
        if (equipped != ShooterType.BASIC && !isAvailable(equipped)) {
            equipped = ShooterType.BASIC
        }
    }

    fun spawnBullets(originX: Float, originY: Float, baseDamage: Int): List<Bullet> {
        val shooter = getEquipped()
        val dmg = (baseDamage * shooter.damageMultiplier).toInt().coerceAtLeast(1)
        val col = shooter.bulletColor
        val glow = shooter.glowColor

        return when (shooter.type) {
            ShooterType.BASIC -> listOf(
                Bullet(originX, originY, dmg, 0f, -Bullet.SPEED, col, glow)
            )

            ShooterType.DOUBLE -> listOf(
                Bullet(originX - 12f, originY, dmg, 0f, -Bullet.SPEED, col, glow),
                Bullet(originX + 12f, originY, dmg, 0f, -Bullet.SPEED, col, glow)
            )

            ShooterType.SPREAD -> {
                val angle = Math.toRadians(15.0)
                val speed = Bullet.SPEED.toDouble()
                listOf(
                    Bullet(originX, originY, dmg,
                        (-sin(angle) * speed).toFloat(), (-cos(angle) * speed).toFloat(), col, glow),
                    Bullet(originX, originY, dmg, 0f, -Bullet.SPEED, col, glow),
                    Bullet(originX, originY, dmg,
                        (sin(angle) * speed).toFloat(), (-cos(angle) * speed).toFloat(), col, glow)
                )
            }

            ShooterType.RAPID -> listOf(
                Bullet(originX, originY, dmg, 0f, -Bullet.SPEED * 1.2f, col, glow)
            )

            ShooterType.LASER -> listOf(
                Bullet(originX, originY, dmg, 0f, -Bullet.SPEED * 1.8f, col, glow,
                    bulletWidth = 3f, bulletHeight = 28f)
            )
        }
    }

    fun resetProgress() {
        prefs.edit().clear().apply()
    }
}

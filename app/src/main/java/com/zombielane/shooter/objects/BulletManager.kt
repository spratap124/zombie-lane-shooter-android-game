package com.zombielane.shooter.objects

import com.zombielane.shooter.data.Shooter
import com.zombielane.shooter.data.ShooterType
import kotlin.math.cos
import kotlin.math.sin

object BulletManager {

    private const val SPREAD_ANGLE_DEG = 15.0

    fun spawnPattern(shooter: Shooter, playerX: Float, playerY: Float, baseDamage: Int): List<Bullet> {
        val dmg = (baseDamage * shooter.damageMultiplier).toInt().coerceAtLeast(1)
        val col = shooter.bulletColor
        val glow = shooter.glowColor

        return when (shooter.type) {
            ShooterType.BASIC -> listOf(
                bullet(playerX, playerY, dmg, col, glow)
            )

            ShooterType.DOUBLE -> listOf(
                bullet(playerX - 12f, playerY, dmg, col, glow),
                bullet(playerX + 12f, playerY, dmg, col, glow)
            )

            ShooterType.SPREAD -> {
                val rad = Math.toRadians(SPREAD_ANGLE_DEG)
                val spd = Bullet.SPEED.toDouble()
                listOf(
                    bullet(playerX, playerY, dmg, col, glow,
                        vx = (-sin(rad) * spd).toFloat(),
                        vy = (-cos(rad) * spd).toFloat()),
                    bullet(playerX, playerY, dmg, col, glow),
                    bullet(playerX, playerY, dmg, col, glow,
                        vx = (sin(rad) * spd).toFloat(),
                        vy = (-cos(rad) * spd).toFloat())
                )
            }

            ShooterType.RAPID -> listOf(
                bullet(playerX, playerY, dmg, col, glow,
                    vy = -Bullet.SPEED * 1.2f,
                    bw = 4f, bh = 12f)
            )

            ShooterType.LASER -> listOf(
                bullet(playerX, playerY, dmg, col, glow,
                    vy = -Bullet.SPEED * 1.8f,
                    bw = 3f, bh = 28f)
            )
        }
    }

    private fun bullet(
        x: Float, y: Float, dmg: Int,
        col: Int, glow: Int,
        vx: Float = 0f,
        vy: Float = -Bullet.SPEED,
        bw: Float = Bullet.WIDTH,
        bh: Float = Bullet.HEIGHT
    ) = Bullet(x, y, dmg, vx, vy, col, glow, bw, bh)
}

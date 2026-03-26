package com.zombielane.shooter.objects

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object EnemyBulletManager {

    fun tryShoot(enemy: Enemy, playerX: Float, playerY: Float, now: Long): List<EnemyBullet> {
        if (!enemy.canShoot || !enemy.active) return emptyList()
        if (enemy.y < 0) return emptyList()
        if (enemy.lastShotTimeMs == 0L) {
            enemy.lastShotTimeMs = now
            return emptyList()
        }
        if (now - enemy.lastShotTimeMs < enemy.shootInterval) return emptyList()

        enemy.lastShotTimeMs = now

        val ex = enemy.x + enemy.width / 2f
        val ey = enemy.y + enemy.height

        return when (enemy.type) {
            EnemyType.BOSS -> spawnAimed(ex, ey, playerX, playerY, EnemyBullet.COLOR_BOSS)
            else -> spawnAimed(ex, ey, playerX, playerY, colorFor(enemy.type))
        }
    }

    private fun spawnAimed(ex: Float, ey: Float, px: Float, py: Float, color: Int): List<EnemyBullet> {
        val angle = atan2((py - ey).toDouble(), (px - ex).toDouble())
        val vx = (cos(angle) * EnemyBullet.SPEED).toFloat()
        val vy = (sin(angle) * EnemyBullet.SPEED).toFloat()
        return listOf(EnemyBullet(ex, ey, vx, vy, color))
    }

    private fun colorFor(type: EnemyType): Int = when (type) {
        EnemyType.BOSS -> EnemyBullet.COLOR_BOSS
        EnemyType.ZIGZAG -> EnemyBullet.COLOR_ZIGZAG
        else -> EnemyBullet.COLOR_NORMAL
    }
}

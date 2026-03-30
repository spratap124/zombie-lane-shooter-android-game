package com.zombielane.shooter.objects

import android.graphics.Color
import android.graphics.RectF
import com.zombielane.shooter.engine.Stage
import kotlin.random.Random

class EnemySpawner {

    companion object {
        private const val BASE_INTERVAL = 65
        private const val MIN_INTERVAL = 18
    }

    private var frameCounter = 0
    private var spawnInterval = BASE_INTERVAL

    var speedMultiplier = 1f
    var coinMultiplier = 1

    fun update(screenWidth: Int, score: Int, safeArea: RectF, stage: Stage): List<Enemy> {
        val result = mutableListOf<Enemy>()

        val stageAdjusted = (BASE_INTERVAL * stage.spawnRateMultiplier).toInt()
        spawnInterval = (stageAdjusted - score / 80).coerceAtLeast(MIN_INTERVAL)
        frameCounter++

        if (frameCounter >= spawnInterval) {
            frameCounter = 0
            result.add(createEnemy(score, safeArea, stage))
        }

        return result
    }

    fun spawnBoss(safeArea: RectF, score: Int, stage: Stage): Enemy {
        return createBoss(safeArea, score, stage)
    }

    fun spawnBurst(count: Int, safeArea: RectF, score: Int, stage: Stage): List<Enemy> {
        return List(count) { createEnemy(score, safeArea, stage) }
    }

    private fun createEnemy(score: Int, safeArea: RectF, stage: Stage): Enemy {
        val spawnWidth = safeArea.width() - Enemy.SIZE
        val x = safeArea.left + Random.nextFloat() * spawnWidth.coerceAtLeast(0f)

        val tier = when {
            score > 3000 && Random.nextFloat() < 0.20f -> 2
            score > 1200 && Random.nextFloat() < 0.25f -> 1
            else -> 0
        }

        val allowedTypes = stage.enemyTypes.filter { it != EnemyType.BOSS }
        val type = if (allowedTypes.size <= 1) {
            allowedTypes.firstOrNull() ?: EnemyType.NORMAL
        } else {
            val roll = Random.nextFloat()
            when {
                roll < 0.6f -> allowedTypes.first()
                roll < 0.85f && allowedTypes.size > 1 -> allowedTypes[1]
                allowedTypes.size > 2 -> allowedTypes[Random.nextInt(2, allowedTypes.size)]
                else -> allowedTypes.last()
            }
        }

        val speedProgression = (score / 15000f).coerceIn(0f, 1f)
        val baseSpeed = when (tier) {
            2 -> 1.2f + speedProgression * 1.8f + Random.nextFloat() * 0.5f
            1 -> 1.0f + speedProgression * 1.5f + Random.nextFloat() * 0.4f
            else -> 0.8f + speedProgression * 1.2f + Random.nextFloat() * 0.3f
        }

        val typeSpeedMod = when (type) {
            EnemyType.FAST -> 1.5f
            EnemyType.ZIGZAG -> 0.75f
            else -> 1f
        }

        val hp = when (type) {
            EnemyType.SPLITTER -> when (tier) { 2 -> 6; 1 -> 4; else -> 2 }
            EnemyType.FAST -> 1
            else -> when (tier) { 2 -> 5; 1 -> 3; else -> 1 }
        }

        val color = when (type) {
            EnemyType.ZIGZAG -> Color.parseColor("#00BCD4")
            EnemyType.FAST -> Color.parseColor("#FF5722")
            EnemyType.SPLITTER -> Color.parseColor("#9C27B0")
            else -> when (tier) {
                2 -> Color.parseColor("#D32F2F")
                1 -> Color.parseColor("#F57C00")
                else -> Color.parseColor("#7B1FA2")
            }
        }

        val scoreVal = when (tier) { 2 -> 30; 1 -> 20; else -> 10 }
        val coinVal = (when (tier) { 2 -> 3; 1 -> 2; else -> 1 }) * coinMultiplier

        val canShootInStage = stage.stageNumber > Stage.INTRO_STAGE_COUNT
        val shoots = canShootInStage && when (type) {
            EnemyType.ZIGZAG -> Random.nextFloat() < 0.08f
            else -> false
        }
        val interval = 5000L

        return Enemy(
            x, -Enemy.SIZE,
            speed = baseSpeed * typeSpeedMod * speedMultiplier * stage.speedMultiplier,
            scoreValue = scoreVal, coinValue = coinVal,
            bodyColor = color, health = hp, type = type,
            canShoot = shoots, shootInterval = interval
        )
    }

    private fun createBoss(safeArea: RectF, score: Int, stage: Stage): Enemy {
        val x = safeArea.left + (safeArea.width() - Enemy.BOSS_SIZE) / 2f
        val bossHp = 12 + stage.stageNumber * 5
        return Enemy(
            x, -Enemy.BOSS_SIZE,
            speed = 0.6f * speedMultiplier * stage.speedMultiplier,
            scoreValue = 200 + stage.stageNumber * 50,
            coinValue = (15 + stage.stageNumber * 5) * coinMultiplier,
            bodyColor = Color.parseColor("#B71C1C"),
            health = bossHp, type = EnemyType.BOSS,
            canShoot = stage.stageNumber > Stage.INTRO_STAGE_COUNT, shootInterval = 3600L
        )
    }

    fun reset() {
        frameCounter = 0
        spawnInterval = BASE_INTERVAL
        speedMultiplier = 1f
        coinMultiplier = 1
    }
}

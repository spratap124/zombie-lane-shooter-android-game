package com.zombielane.shooter.objects

import android.graphics.Color
import android.graphics.RectF
import kotlin.random.Random

class EnemySpawner {

    companion object {
        private const val BASE_INTERVAL = 65
        private const val MIN_INTERVAL = 18
        private const val BOSS_INTERVAL = 2000
    }

    private var frameCounter = 0
    private var spawnInterval = BASE_INTERVAL
    private var lastBossScore = 0

    var speedMultiplier = 1f
    var coinMultiplier = 1

    fun update(screenWidth: Int, score: Int, safeArea: RectF): List<Enemy> {
        val result = mutableListOf<Enemy>()

        // Gentle curve: drops fast early, then flattens out
        // score  0   → 65 frames (1.08s)
        // score  200 → 61 frames
        // score  500 → 55 frames
        // score  1000 → 45 frames
        // score  2000 → 35 frames
        // score  4000 → 25 frames
        // score  8000 → 18 frames (floor)
        spawnInterval = (BASE_INTERVAL - score / 50).coerceAtLeast(MIN_INTERVAL)
        frameCounter++

        if (frameCounter >= spawnInterval) {
            frameCounter = 0
            result.add(createEnemy(score, safeArea))
        }

        // Boss every 2000 points, first one at 2000
        val bossThreshold = (score / BOSS_INTERVAL) * BOSS_INTERVAL
        if (bossThreshold > 0 && bossThreshold > lastBossScore && score >= bossThreshold) {
            lastBossScore = bossThreshold
            result.add(createBoss(safeArea, score))
        }

        return result
    }

    fun spawnBurst(count: Int, safeArea: RectF, score: Int): List<Enemy> {
        return List(count) { createEnemy(score, safeArea) }
    }

    private fun createEnemy(score: Int, safeArea: RectF): Enemy {
        val spawnWidth = safeArea.width() - Enemy.SIZE
        val x = safeArea.left + Random.nextFloat() * spawnWidth.coerceAtLeast(0f)

        // Tiers unlock gradually
        val tier = when {
            score > 3000 && Random.nextFloat() < 0.20f -> 2
            score > 1200 && Random.nextFloat() < 0.25f -> 1
            else -> 0
        }

        // Special types unlock late, low probability
        val type = when {
            score > 800  && Random.nextFloat() < 0.12f -> EnemyType.ZIGZAG
            score > 2000 && Random.nextFloat() < 0.10f -> EnemyType.FAST
            score > 3500 && Random.nextFloat() < 0.08f -> EnemyType.SPLITTER
            else -> EnemyType.NORMAL
        }

        // Very slow start, ramps over a long period
        // score 0 → 0.0, score 5000 → 0.5, score 15000 → 1.0
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

        return Enemy(
            x, -Enemy.SIZE,
            speed = baseSpeed * typeSpeedMod * speedMultiplier,
            scoreValue = scoreVal, coinValue = coinVal,
            bodyColor = color, health = hp, type = type
        )
    }

    private fun createBoss(safeArea: RectF, score: Int): Enemy {
        val x = safeArea.left + (safeArea.width() - Enemy.BOSS_SIZE) / 2f
        val bossHp = 20 + (score / 2000) * 5
        return Enemy(
            x, -Enemy.BOSS_SIZE,
            speed = 0.6f * speedMultiplier,
            scoreValue = 200, coinValue = 15 * coinMultiplier,
            bodyColor = Color.parseColor("#B71C1C"),
            health = bossHp, type = EnemyType.BOSS
        )
    }

    fun reset() {
        frameCounter = 0
        spawnInterval = BASE_INTERVAL
        lastBossScore = 0
        speedMultiplier = 1f
        coinMultiplier = 1
    }
}

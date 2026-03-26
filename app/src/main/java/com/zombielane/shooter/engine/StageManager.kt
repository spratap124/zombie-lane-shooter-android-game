package com.zombielane.shooter.engine

import com.zombielane.shooter.objects.EnemyType

enum class BackgroundType {
    SPACE,
    CITY,
    LAVA,
    FOREST,
    ICE
}

data class Stage(
    val stageNumber: Int,
    val name: String,
    val killsRequired: Int,
    val enemyTypes: List<EnemyType>,
    val spawnRateMultiplier: Float,
    val speedMultiplier: Float,
    val backgroundType: BackgroundType,
    val hasBoss: Boolean
) {
    companion object {
        val ALL = listOf(
            Stage(1, "Outskirts", killsRequired = 10,
                enemyTypes = listOf(EnemyType.NORMAL),
                spawnRateMultiplier = 1.0f, speedMultiplier = 1.0f,
                backgroundType = BackgroundType.SPACE, hasBoss = false),

            Stage(2, "Neon District", killsRequired = 15,
                enemyTypes = listOf(EnemyType.NORMAL, EnemyType.ZIGZAG),
                spawnRateMultiplier = 0.9f, speedMultiplier = 1.05f,
                backgroundType = BackgroundType.CITY, hasBoss = true),

            Stage(3, "Frozen Reach", killsRequired = 20,
                enemyTypes = listOf(EnemyType.NORMAL, EnemyType.ZIGZAG, EnemyType.FAST),
                spawnRateMultiplier = 0.82f, speedMultiplier = 1.1f,
                backgroundType = BackgroundType.ICE, hasBoss = true),

            Stage(4, "Dark Forest", killsRequired = 25,
                enemyTypes = listOf(EnemyType.NORMAL, EnemyType.ZIGZAG, EnemyType.FAST, EnemyType.SPLITTER),
                spawnRateMultiplier = 0.75f, speedMultiplier = 1.2f,
                backgroundType = BackgroundType.FOREST, hasBoss = true),

            Stage(5, "Molten Core", killsRequired = 35,
                enemyTypes = listOf(EnemyType.ZIGZAG, EnemyType.FAST, EnemyType.SPLITTER),
                spawnRateMultiplier = 0.68f, speedMultiplier = 1.3f,
                backgroundType = BackgroundType.LAVA, hasBoss = true),

            Stage(6, "Final Orbit", killsRequired = 45,
                enemyTypes = listOf(EnemyType.FAST, EnemyType.SPLITTER, EnemyType.ZIGZAG),
                spawnRateMultiplier = 0.6f, speedMultiplier = 1.4f,
                backgroundType = BackgroundType.SPACE, hasBoss = true)
        )

        fun getEndless(stageNumber: Int): Stage {
            val scaling = 1 + (stageNumber - ALL.size) * 0.05f
            return Stage(
                stageNumber = stageNumber,
                name = "Zone $stageNumber",
                killsRequired = 40 + (stageNumber - ALL.size) * 8,
                enemyTypes = listOf(EnemyType.NORMAL, EnemyType.ZIGZAG, EnemyType.FAST, EnemyType.SPLITTER),
                spawnRateMultiplier = (0.55f / scaling).coerceAtLeast(0.3f),
                speedMultiplier = (1.4f * scaling).coerceAtMost(2.5f),
                backgroundType = BackgroundType.entries[(stageNumber - 1) % BackgroundType.entries.size],
                hasBoss = true
            )
        }
    }
}

class StageManager {

    var currentStage: Stage = Stage.ALL.first()
        private set

    var stageKills = 0
        private set

    var bossSpawned = false
        private set

    var bossDefeated = false
        private set

    var stageTransitionFrames = 0
        private set

    val isTransitioning: Boolean get() = stageTransitionFrames > 0

    val stageProgress: Float
        get() = if (currentStage.hasBoss && bossSpawned)
            1f
        else
            (stageKills.toFloat() / currentStage.killsRequired).coerceIn(0f, 1f)

    fun onEnemyKilled(isBoss: Boolean) {
        if (isBoss) {
            bossDefeated = true
            return
        }
        stageKills++
    }

    fun shouldSpawnBoss(): Boolean {
        if (!currentStage.hasBoss) return false
        if (bossSpawned) return false
        return stageKills >= currentStage.killsRequired
    }

    fun markBossSpawned() {
        bossSpawned = true
    }

    /** If the boss entity was lost without a kill (e.g. old despawn bug), allow spawns / boss respawn. */
    fun clearStuckBossFightIfNoBossOnField(hasActiveBoss: Boolean) {
        if (bossSpawned && !bossDefeated && !hasActiveBoss) {
            bossSpawned = false
        }
    }

    fun update(): Boolean {
        if (stageTransitionFrames > 0) {
            stageTransitionFrames--
            return stageTransitionFrames > 0
        }

        val readyToAdvance = if (currentStage.hasBoss) {
            bossDefeated
        } else {
            stageKills >= currentStage.killsRequired
        }

        if (readyToAdvance) {
            advanceStage()
            return true
        }
        return false
    }

    private fun advanceStage() {
        val nextNum = currentStage.stageNumber + 1
        currentStage = if (nextNum <= Stage.ALL.size) {
            Stage.ALL[nextNum - 1]
        } else {
            Stage.getEndless(nextNum)
        }
        stageKills = 0
        bossSpawned = false
        bossDefeated = false
        stageTransitionFrames = 120
    }

    fun reset() {
        currentStage = Stage.ALL.first()
        stageKills = 0
        bossSpawned = false
        bossDefeated = false
        stageTransitionFrames = 0
    }
}

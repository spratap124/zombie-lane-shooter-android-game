package com.zombielane.shooter.data

import android.content.Context
import android.content.SharedPreferences
import kotlin.random.Random

data class ChestSlot(
    val index: Int,
    val type: ChestType,
    val unlockStartMs: Long,
    val unlockDurationMs: Long
) {
    fun isReady(now: Long): Boolean = now >= unlockStartMs + unlockDurationMs

    fun remainingMs(now: Long): Long =
        (unlockStartMs + unlockDurationMs - now).coerceAtLeast(0L)
}

enum class ChestGrantResult { ADDED, SLOTS_FULL }

class ChestManager(context: Context) {

    companion object {
        const val MAX_SLOTS = 4
        private const val PREFS = "zombie_lane_chests_v3"

        private val SHOOTER_POOL = listOf(
            ShooterType.DOUBLE,
            ShooterType.RAPID,
            ShooterType.SPREAD,
            ShooterType.LASER
        )

        private const val SUPER_DROP_CHANCE = 0.035f
        private const val LUCKY_CHANCE = 0.10f

        private fun keyType(i: Int) = "slot_${i}_type"
        private fun keyStart(i: Int) = "slot_${i}_start"
        private fun keyDur(i: Int) = "slot_${i}_dur"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getSlots(now: Long): List<ChestSlot?> {
        return List(MAX_SLOTS) { i ->
            val typeName = prefs.getString(keyType(i), "") ?: ""
            if (typeName.isEmpty()) null
            else {
                val type = try {
                    ChestType.valueOf(typeName)
                } catch (_: Exception) {
                    ChestType.COMMON
                }
                ChestSlot(
                    index = i,
                    type = type,
                    unlockStartMs = prefs.getLong(keyStart(i), 0L),
                    unlockDurationMs = prefs.getLong(keyDur(i), type.unlockDurationMs)
                )
            }
        }
    }

    fun filledCount(): Int = (0 until MAX_SLOTS).count { i ->
        !(prefs.getString(keyType(i), "") ?: "").isEmpty()
    }

    fun hasAnyChest(): Boolean = filledCount() > 0

    fun isFull(): Boolean = filledCount() >= MAX_SLOTS

    private fun firstEmptySlot(): Int? =
        (0 until MAX_SLOTS).firstOrNull { i ->
            (prefs.getString(keyType(i), "") ?: "").isEmpty()
        }

    /** Places chest with timer starting now. */
    fun tryAddChest(type: ChestType, now: Long = System.currentTimeMillis()): ChestGrantResult {
        val slot = firstEmptySlot() ?: return ChestGrantResult.SLOTS_FULL
        prefs.edit()
            .putString(keyType(slot), type.name)
            .putLong(keyStart(slot), now)
            .putLong(keyDur(slot), type.unlockDurationMs)
            .apply()
        return ChestGrantResult.ADDED
    }

    fun grantRunChest(stageNumber: Int, score: Int, now: Long = System.currentTimeMillis()): ChestGrantResult {
        if (isFull()) return ChestGrantResult.SLOTS_FULL
        val type = if (Random.nextFloat() < SUPER_DROP_CHANCE) {
            ChestType.SUPER
        } else {
            rollNormalChestType(stageNumber, score)
        }
        return tryAddChest(type, now)
    }

    private fun rollNormalChestType(stageNumber: Int, score: Int): ChestType {
        val epicChance = (0.04f + stageNumber * 0.012f + (score / 12000f).coerceAtMost(0.12f)).coerceAtMost(0.22f)
        val rareChance = (0.22f + stageNumber * 0.015f + (score / 25000f).coerceAtMost(0.08f)).coerceAtMost(0.45f)
        val r = Random.nextFloat()
        return when {
            r < epicChance -> ChestType.EPIC
            r < epicChance + rareChance -> ChestType.RARE
            else -> ChestType.COMMON
        }
    }

    /** Rewarded ad: make slot openable immediately. */
    fun skipTimerForSlot(index: Int, now: Long = System.currentTimeMillis()) {
        if (index !in 0 until MAX_SLOTS) return
        val typeName = prefs.getString(keyType(index), "") ?: return
        if (typeName.isEmpty()) return
        val dur = prefs.getLong(keyDur(index), 0L)
        prefs.edit().putLong(keyStart(index), now - dur).apply()
    }

    fun clearSlot(index: Int) {
        if (index !in 0 until MAX_SLOTS) return
        prefs.edit()
            .remove(keyType(index))
            .remove(keyStart(index))
            .remove(keyDur(index))
            .apply()
    }

    /**
     * If ready, removes chest and returns open result (rewards + lucky).
     * Caller applies rewards after reveal / double-ad flow.
     */
    fun openReadySlot(index: Int, now: Long = System.currentTimeMillis()): ChestOpenResult? {
        val slot = getSlots(now)[index] ?: return null
        if (!slot.isReady(now)) return null
        val type = slot.type
        clearSlot(index)

        var rewardType = type
        var rewards = rollRewardsForType(rewardType)
        var lucky = false
        var luckyKind: LuckyBonusKind? = null

        if (Random.nextFloat() < LUCKY_CHANCE) {
            lucky = true
            if (Random.nextBoolean()) {
                luckyKind = LuckyBonusKind.DOUBLE_VALUES
                rewards = rewards.scaled(2f)
            } else {
                luckyKind = LuckyBonusKind.RARITY_UP
                val up = rewardType.mergeUpgrade() ?: rewardType
                rewardType = up
                rewards = rollRewardsForType(up)
            }
        }

        return ChestOpenResult(
            rewards = rewards,
            luckyBonus = lucky,
            luckyKind = luckyKind,
            baseType = type
        )
    }

    fun rollRewardsForType(type: ChestType): ChestRewards {
        val coins = when (type) {
            ChestType.COMMON -> Random.nextInt(22, 58)
            ChestType.RARE -> Random.nextInt(55, 145)
            ChestType.EPIC -> Random.nextInt(140, 380)
            ChestType.SUPER -> Random.nextInt(400, 900)
        }

        var temp: ShooterType? = null
        var tempMs = 0L
        var shield = false
        var rapidMs = 0L

        when (type) {
            ChestType.COMMON -> {
                if (Random.nextFloat() < 0.38f) {
                    temp = SHOOTER_POOL.random()
                    tempMs = 8L * 60 * 1000
                }
                if (Random.nextFloat() < 0.36f) shield = true
                if (Random.nextFloat() < 0.28f) rapidMs = 4500L
            }
            ChestType.RARE -> {
                if (Random.nextFloat() < 0.55f) {
                    temp = SHOOTER_POOL.random()
                    tempMs = 18L * 60 * 1000
                }
                if (Random.nextFloat() < 0.52f) shield = true
                if (Random.nextFloat() < 0.45f) rapidMs = 6500L
            }
            ChestType.EPIC -> {
                if (Random.nextFloat() < 0.75f) {
                    temp = SHOOTER_POOL.random()
                    tempMs = 35L * 60 * 1000
                }
                if (Random.nextFloat() < 0.65f) shield = true
                if (Random.nextFloat() < 0.55f) rapidMs = 9000L
            }
            ChestType.SUPER -> {
                temp = SHOOTER_POOL.random()
                tempMs = 45L * 60 * 1000
                shield = true
                rapidMs = 12000L
            }
        }

        return ChestRewards(coins, temp, tempMs, shield, rapidMs)
    }

    fun applyRewards(
        rewards: ChestRewards,
        upgradeManager: UpgradeManager,
        shooterManager: ShooterManager,
        runBuffManager: RunBuffManager
    ) {
        if (rewards.coins > 0) upgradeManager.totalCoins += rewards.coins
        rewards.tempShooter?.let { st ->
            if (rewards.tempShooterDurationMs > 0L) {
                shooterManager.unlockTemporarily(st, rewards.tempShooterDurationMs)
                shooterManager.equip(st)
            }
        }
        if (rewards.nextRunShield) runBuffManager.addNextRunShield()
        if (rewards.nextRunRapidMs > 0L) runBuffManager.addNextRunRapid(rewards.nextRunRapidMs)
    }

    /**
     * Merge two occupied slots of the same type into one upgraded chest (fresh timer).
     * Returns false if invalid.
     */
    fun tryMergeSlots(a: Int, b: Int, now: Long = System.currentTimeMillis()): Boolean {
        if (a == b || a !in 0 until MAX_SLOTS || b !in 0 until MAX_SLOTS) return false
        val slots = getSlots(now)
        val ca = slots[a] ?: return false
        val cb = slots[b] ?: return false
        if (ca.type != cb.type) return false
        val upgraded = ca.type.mergeUpgrade() ?: return false
        clearSlot(a)
        clearSlot(b)
        tryAddChest(upgraded, now)
        return true
    }

    fun resetProgress() {
        prefs.edit().clear().apply()
    }
}

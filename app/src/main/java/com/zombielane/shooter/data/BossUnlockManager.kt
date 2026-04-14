package com.zombielane.shooter.data

import android.content.Context
import android.content.SharedPreferences
import com.zombielane.shooter.objects.EnemyAssets
import com.zombielane.shooter.objects.EnemySpawner

/**
 * Boss Codex — **not** tied to best stage.
 *
 * - **Boss #1–#14** (skin indices **0..13**): always available for runs and the codex.
 * - **Boss #15+** (indices **14+**): **rewarded ad** (30 min) or **permanent coins**.
 *
 * State is stored in [SharedPreferences] (perm set + per-skin expiry timestamps).
 */
class BossUnlockManager(context: Context) {

    companion object {
        private const val PREFS = "zombie_lane_boss_unlocks_v1"
        private const val KEY_PERM = "perm_skins"
        private const val TEMP_PREFIX = "temp_expiry_"

        /** Last skin index that is free (Boss #14 → index 13). */
        const val LAST_FREE_BOSS_SKIN_INDEX = 13

        /** First skin index that uses ads/coins (Boss #15 → index 14). */
        const val FIRST_MONETIZED_BOSS_SKIN_INDEX = LAST_FREE_BOSS_SKIN_INDEX + 1

        /** Fallback skin when a higher skin is locked (always in free tier). */
        const val STARTER_SKIN_INDEX = 0

        fun isFreeBossTierSkin(skinIndex: Int): Boolean =
            skinIndex in 0..LAST_FREE_BOSS_SKIN_INDEX

        const val TEMP_DURATION_MS = 30L * 60 * 1000

        private const val BASE_PERM_COINS = 50_000
        private const val STEP_PERM_PER_SKIN = 7_000

        fun permanentUnlockCost(skinIndex: Int): Int {
            if (skinIndex < 0 || skinIndex >= EnemyAssets.BOSS_SKIN_COUNT) return 0
            return BASE_PERM_COINS + skinIndex * STEP_PERM_PER_SKIN
        }
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun resetProgress() {
        prefs.edit().clear().apply()
    }

    fun isGatingActive(lifetimeMaxStage: Int): Boolean =
        BossCodexUnlockRules.isGatingActive(lifetimeMaxStage)

    fun isPermanentUnlocked(skinIndex: Int): Boolean {
        val set = prefs.getStringSet(KEY_PERM, emptySet()) ?: emptySet()
        return skinIndex.toString() in set
    }

    fun isTemporaryActive(skinIndex: Int, nowMs: Long = System.currentTimeMillis()): Boolean {
        return prefs.getLong(tempKey(skinIndex), 0L) > nowMs
    }

    fun temporaryRemainingMs(skinIndex: Int, nowMs: Long = System.currentTimeMillis()): Long {
        return (prefs.getLong(tempKey(skinIndex), 0L) - nowMs).coerceAtLeast(0L)
    }

    /** Use in runs / HUD: true if this boss skin can appear in combat. */
    fun isAvailableForRun(skinIndex: Int, lifetimeMaxStage: Int, nowMs: Long = System.currentTimeMillis()): Boolean {
        if (skinIndex < 0 || skinIndex >= EnemyAssets.BOSS_SKIN_COUNT) return false
        if (isFreeBossTierSkin(skinIndex)) return true
        if (isPermanentUnlocked(skinIndex)) return true
        if (isTemporaryActive(skinIndex, nowMs)) return true
        return false
    }

    /**
     * Rich state for UI (matches requested shape: id, unlock flags, expiry).
     * [id] is **0-based** skin index (0 = Boss #1).
     * [lifetimeMaxStage] is unused; kept for call-site compatibility.
     */
    @Suppress("UNUSED_PARAMETER")
    fun getBossState(skinIndex: Int, lifetimeMaxStage: Int, nowMs: Long = System.currentTimeMillis()): BossUnlockState {
        val perm = isPermanentUnlocked(skinIndex)
        val temp = isTemporaryActive(skinIndex, nowMs)
        val expiry = prefs.getLong(tempKey(skinIndex), 0L).takeIf { it > nowMs }
        val unlocked = when {
            isFreeBossTierSkin(skinIndex) -> true
            perm -> true
            temp -> true
            else -> false
        }
        return BossUnlockState(
            id = skinIndex,
            isUnlocked = unlocked,
            isTemporaryUnlocked = temp && !perm,
            unlockExpiryTimeMs = expiry
        )
    }

    fun resolveSkinForStage(stageNumber: Int, lifetimeMaxStage: Int, nowMs: Long = System.currentTimeMillis()): Int {
        val want = EnemySpawner.bossSkinForStage(stageNumber).mod(EnemyAssets.BOSS_SKIN_COUNT)
        if (isAvailableForRun(want, lifetimeMaxStage, nowMs)) return want
        for (i in want downTo 0) {
            if (isAvailableForRun(i, lifetimeMaxStage, nowMs)) return i
        }
        return STARTER_SKIN_INDEX
    }

    @Suppress("UNUSED_PARAMETER")
    fun grantTemporaryFromAd(
        skinIndex: Int,
        lifetimeMaxStage: Int,
        nowMs: Long = System.currentTimeMillis()
    ): Boolean {
        if (skinIndex < 0 || skinIndex >= EnemyAssets.BOSS_SKIN_COUNT) return false
        if (isFreeBossTierSkin(skinIndex)) return false
        if (isPermanentUnlocked(skinIndex)) return false
        prefs.edit().putLong(tempKey(skinIndex), nowMs + TEMP_DURATION_MS).apply()
        return true
    }

    @Suppress("UNUSED_PARAMETER")
    fun unlockPermanent(skinIndex: Int, upgradeManager: UpgradeManager, lifetimeMaxStage: Int): Boolean {
        if (skinIndex < 0 || skinIndex >= EnemyAssets.BOSS_SKIN_COUNT) return false
        if (isFreeBossTierSkin(skinIndex)) return false
        if (isPermanentUnlocked(skinIndex)) return false
        val cost = permanentUnlockCost(skinIndex)
        if (upgradeManager.totalCoins < cost) return false
        upgradeManager.totalCoins -= cost
        val cur = prefs.getStringSet(KEY_PERM, emptySet())?.toMutableSet() ?: mutableSetOf()
        cur.add(skinIndex.toString())
        prefs.edit().putStringSet(KEY_PERM, cur).remove(tempKey(skinIndex)).apply()
        return true
    }

    private fun tempKey(skinIndex: Int) = TEMP_PREFIX + skinIndex
}

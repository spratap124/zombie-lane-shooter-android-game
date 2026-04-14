package com.zombielane.shooter.data

/**
 * Boss Codex: **Boss #1–#14** (indices 0–13) are free; **Boss #15+** use ads (30 min) or coins.
 * Not tied to best stage.
 */
object BossCodexUnlockRules {

    /** Boss #15+ require unlock; free tier is defined in [BossUnlockManager]. */
    const val CODEX_MONETIZATION_ALWAYS_ACTIVE: Boolean = true

    /**
     * Whether ads/coins gating applies. Always `true` — progression is not tied to stage anymore.
     */
    @Suppress("UNUSED_PARAMETER")
    fun isGatingActive(lifetimeMaxStage: Int): Boolean = true

    fun isBossUnlockedInCodex(
        skinIndex0Based: Int,
        lifetimeMaxStage: Int,
        manager: BossUnlockManager,
        nowMs: Long = System.currentTimeMillis()
    ): Boolean =
        manager.getBossState(skinIndex0Based, lifetimeMaxStage, nowMs).isUnlocked
}

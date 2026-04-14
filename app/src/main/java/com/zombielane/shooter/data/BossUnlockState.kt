package com.zombielane.shooter.data

/**
 * Snapshot for one boss entry in the Boss Codex (UI / persistence layer).
 *
 * [isUnlocked] — permanently owned or (when player level ≤ 14) free access to all bosses.
 * [isTemporaryUnlocked] — active rewarded-ad grant (see [unlockExpiryTimeMs]).
 */
data class BossUnlockState(
    val id: Int,
    val isUnlocked: Boolean,
    val isTemporaryUnlocked: Boolean,
    val unlockExpiryTimeMs: Long?
)

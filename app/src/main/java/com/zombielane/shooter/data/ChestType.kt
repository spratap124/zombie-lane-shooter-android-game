package com.zombielane.shooter.data

enum class ChestType {
    COMMON,
    RARE,
    EPIC,
    SUPER;

    val displayName: String
        get() = when (this) {
            COMMON -> "Common"
            RARE -> "Rare"
            EPIC -> "Epic"
            SUPER -> "Super"
        }

    /** Unlock timer from when the chest is placed. */
    val unlockDurationMs: Long
        get() = when (this) {
            COMMON -> 30L * 60 * 1000
            RARE -> 2L * 60 * 60 * 1000
            EPIC -> 6L * 60 * 60 * 1000
            SUPER -> 6L * 60 * 60 * 1000
        }

    /** Result of merging two of this type, or null if cannot merge further. */
    fun mergeUpgrade(): ChestType? = when (this) {
        COMMON -> RARE
        RARE -> EPIC
        EPIC -> SUPER
        SUPER -> null
    }
}

data class ChestRewards(
    val coins: Int,
    val tempShooter: ShooterType?,
    val tempShooterDurationMs: Long,
    val nextRunShield: Boolean,
    val nextRunRapidMs: Long
) {
    fun scaled(factor: Float): ChestRewards = ChestRewards(
        coins = (coins * factor).toInt().coerceAtLeast(0),
        tempShooter = tempShooter,
        tempShooterDurationMs = (tempShooterDurationMs * factor).toLong().coerceAtLeast(0L),
        nextRunShield = nextRunShield,
        nextRunRapidMs = (nextRunRapidMs * factor).toLong().coerceAtLeast(0L)
    )

    fun describe(): String {
        val parts = mutableListOf<String>()
        if (coins > 0) parts.add("+$coins coins")
        tempShooter?.let { st ->
            val m = (tempShooterDurationMs / 60000).coerceAtLeast(1)
            parts.add("${Shooter.get(st).name} (${m}m trial)")
        }
        if (nextRunShield) parts.add("Shield next run")
        if (nextRunRapidMs > 0) parts.add("Rapid fire start (${nextRunRapidMs / 1000}s)")
        return parts.joinToString(" · ")
    }
}

enum class LuckyBonusKind { DOUBLE_VALUES, RARITY_UP }

data class ChestOpenResult(
    val rewards: ChestRewards,
    val luckyBonus: Boolean,
    val luckyKind: LuckyBonusKind?,
    val baseType: ChestType
)

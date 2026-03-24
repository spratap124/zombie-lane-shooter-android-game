package com.zombielane.shooter.data

import android.graphics.Color

enum class ShooterType {
    BASIC, DOUBLE, SPREAD, RAPID, LASER
}

data class Shooter(
    val type: ShooterType,
    val name: String,
    val tagline: String,
    val baseFireRateMs: Long,
    val damageMultiplier: Float,
    val bulletColor: Int,
    val glowColor: Int,
    val unlockCost: Int
) {
    companion object {
        val ALL = listOf(
            Shooter(ShooterType.BASIC, "BASIC", "Balanced",
                baseFireRateMs = 130L, damageMultiplier = 1.0f,
                bulletColor = Color.parseColor("#FFEB3B"),
                glowColor = Color.parseColor("#80FFEB3B"),
                unlockCost = 0),

            Shooter(ShooterType.DOUBLE, "DOUBLE", "Power",
                baseFireRateMs = 170L, damageMultiplier = 0.8f,
                bulletColor = Color.parseColor("#2196F3"),
                glowColor = Color.parseColor("#802196F3"),
                unlockCost = 50000),

            Shooter(ShooterType.SPREAD, "SPREAD", "Control",
                baseFireRateMs = 250L, damageMultiplier = 0.65f,
                bulletColor = Color.parseColor("#AB47BC"),
                glowColor = Color.parseColor("#80AB47BC"),
                unlockCost = 100000),

            Shooter(ShooterType.RAPID, "RAPID", "Chaos",
                baseFireRateMs = 40L, damageMultiplier = 0.3f,
                bulletColor = Color.parseColor("#FFEB3B"),
                glowColor = Color.parseColor("#80FFEB3B"),
                unlockCost = 150000),

            Shooter(ShooterType.LASER, "LASER", "Precision",
                baseFireRateMs = 20L, damageMultiplier = 0.15f,
                bulletColor = Color.parseColor("#F44336"),
                glowColor = Color.parseColor("#80F44336"),
                unlockCost = 200000)
        )

        fun get(type: ShooterType): Shooter = ALL.first { it.type == type }
    }
}

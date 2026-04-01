package com.zombielane.shooter.data

/**
 * Chest opening sequence driven by [com.zombielane.shooter.ui.ChestOpeningAnimator].
 * Total scripted time is under two seconds before [DONE]; flow then continues to [ChestRevealPhase.REVEAL].
 */
enum class ChestAnimState {
    IDLE,
    MOVING_TO_CENTER,
    SHAKING,
    FLASH,
    OPENING,
    REWARD,
    DONE
}

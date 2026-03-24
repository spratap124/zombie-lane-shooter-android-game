package com.zombielane.shooter.engine

class ComboTracker {

    companion object {
        private const val COMBO_WINDOW_MS = 1500L
        private const val DECAY_DISPLAY_MS = 2000L
    }

    var combo = 0
        private set
    var displayCombo = 0
        private set
    private var lastKillTimeMs = 0L
    private var displayUntilMs = 0L

    val multiplier: Float get() = 1f + combo * 0.2f

    val isActive: Boolean get() = combo >= 2

    val isVisible: Boolean
        get() = System.currentTimeMillis() < displayUntilMs && displayCombo >= 2

    fun onKill() {
        val now = System.currentTimeMillis()
        if (now - lastKillTimeMs < COMBO_WINDOW_MS) {
            combo++
        } else {
            combo = 1
        }
        lastKillTimeMs = now
        displayCombo = combo
        displayUntilMs = now + DECAY_DISPLAY_MS
    }

    fun update() {
        val now = System.currentTimeMillis()
        if (now - lastKillTimeMs > COMBO_WINDOW_MS && combo > 0) {
            combo = 0
        }
    }

    fun reset() {
        combo = 0
        displayCombo = 0
        lastKillTimeMs = 0L
        displayUntilMs = 0L
    }
}

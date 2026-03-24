package com.zombielane.shooter.engine

import kotlin.random.Random

enum class GameEvent(val label: String, val durationMs: Long) {
    RUSH("ZOMBIE RUSH!", 5000),
    COIN_RAIN("COIN RAIN!", 8000),
    SWARM("SWARM INCOMING!", 0)
}

class GameEventManager {

    companion object {
        private const val FIRST_EVENT_DELAY_MS = 35000L
        private const val EVENT_COOLDOWN_MS = 22000L
    }

    var currentEvent: GameEvent? = null
        private set
    private var eventStartMs = 0L
    private var lastEventMs = 0L
    private var gameStartMs = 0L
    private var firstEventFired = false

    val bannerText: String? get() = currentEvent?.label

    val isActive: Boolean get() = currentEvent != null

    fun update(nowMs: Long): GameEvent? {
        if (gameStartMs == 0L) gameStartMs = nowMs

        currentEvent?.let { event ->
            if (event.durationMs > 0 && nowMs - eventStartMs > event.durationMs) {
                currentEvent = null
            }
        }

        // No events until player has had time to settle in
        if (!firstEventFired && nowMs - gameStartMs < FIRST_EVENT_DELAY_MS) return null

        if (currentEvent == null && nowMs - lastEventMs > EVENT_COOLDOWN_MS) {
            firstEventFired = true
            val triggered = triggerRandom()
            lastEventMs = nowMs
            eventStartMs = nowMs
            return triggered
        }

        return null
    }

    private fun triggerRandom(): GameEvent {
        val event = GameEvent.entries[Random.nextInt(GameEvent.entries.size)]
        currentEvent = if (event == GameEvent.SWARM) null else event
        return event
    }

    fun reset() {
        currentEvent = null
        eventStartMs = 0L
        lastEventMs = 0L
        gameStartMs = 0L
        firstEventFired = false
    }
}

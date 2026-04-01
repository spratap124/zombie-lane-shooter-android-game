package com.zombielane.shooter.data

/**
 * Per-slot presentation for chest art (type still comes from [ChestSlot] / [ChestType]).
 *
 * - [CLOSED] — closed bitmap ([MenuUiAssets.chest]).
 * - [OPENING] — tap started; fullscreen [com.zombielane.shooter.ui.ChestOpeningAnimator] runs on the
 *   SurfaceView; grid still draws the **closed** bitmap until the slot is cleared.
 * - [OPENED] — after flash swap ([ChestOpeningAnimator.T_FLASH_END_MS]); grid/detail use
 *   [MenuUiAssets.chestOpen] until the reveal flow resets the slot.
 *
 * Persisted chest data remains in [ChestManager]; this is UI-only.
 */
enum class ChestVisualState {
    CLOSED,
    OPENING,
    OPENED
}

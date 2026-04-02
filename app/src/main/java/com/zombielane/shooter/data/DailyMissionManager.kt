package com.zombielane.shooter.data

import android.content.Context
import android.content.SharedPreferences
import kotlin.random.Random

/**
 * Three daily missions with 24h reset, manual CLAIM for rewards, milestone bonuses, SharedPreferences.
 */
class DailyMissionManager(context: Context) {

    companion object {
        const val MISSION_COUNT = 3
        const val PERIOD_MS = 24L * 60L * 60L * 1000L

        private const val PREFS = "zombie_lane_daily_missions_v1"
        private const val KEY_PERIOD_START = "period_start_ms"
        private const val KEY_MAX_STAGE = "max_stage_reached"
        private const val KEY_POPUP_SUB = "popup_subtitle_pending"
        private const val KEY_MILESTONE_MASK = "milestone_grant_mask"

        private fun keyKind(i: Int) = "m${i}_kind"
        private fun keyP0(i: Int) = "m${i}_p0"
        private fun keyP1(i: Int) = "m${i}_p1"
        private fun keyProg(i: Int) = "m${i}_prog"
        /** @deprecated Legacy auto-claim; treated as claimed if [keyClaimed] missing */
        private fun keyDoneLegacy(i: Int) = "m${i}_done"
        private fun keyClaimed(i: Int) = "m${i}_claimed"
        private fun keyRCoins(i: Int) = "m${i}_rcoins"
        private fun keyRChest(i: Int) = "m${i}_rchest"

        private const val MS_BIT_1 = 1
        private const val MS_BIT_2 = 2
        private const val MS_BIT_3 = 4
    }

    enum class Kind { KILL, STAGE, SHOOTER }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private var pendingPopupSubtitle: String?
        get() = prefs.getString(KEY_POPUP_SUB, null)?.takeIf { it.isNotEmpty() }
        set(value) {
            prefs.edit().apply {
                if (value.isNullOrEmpty()) remove(KEY_POPUP_SUB) else putString(KEY_POPUP_SUB, value)
                apply()
            }
        }

    private fun isClaimed(i: Int): Boolean =
        prefs.getBoolean(keyClaimed(i), false) || prefs.getBoolean(keyDoneLegacy(i), false)

    fun periodStartMs(): Long = prefs.getLong(KEY_PERIOD_START, 0L)

    fun timeUntilResetMs(now: Long): Long {
        val start = periodStartMs()
        if (start <= 0L) return 0L
        val end = start + PERIOD_MS
        return (end - now).coerceAtLeast(0L)
    }

    fun ensurePeriod(now: Long) {
        val start = periodStartMs()
        if (start <= 0L) {
            startNewPeriod(now)
            return
        }
        if (now - start >= PERIOD_MS) {
            startNewPeriod(now)
        }
    }

    private fun startNewPeriod(now: Long) {
        prefs.edit().clear()
            .putLong(KEY_PERIOD_START, now)
            .putInt(KEY_MAX_STAGE, 0)
            .putInt(KEY_MILESTONE_MASK, 0)
            .apply()
        rollMissions(Random(now xor 0xC0FFEE7L xor MISSION_COUNT.toLong()))
    }

    private fun rollMissions(rng: Random) {
        data class Template(
            val kind: Kind,
            val p0: Int,
            val p1: Int,
            val coins: Int,
            val chest: ChestType?
        )

        val pool = mutableListOf(
            Template(Kind.KILL, 20, 0, 120, ChestType.COMMON),
            Template(Kind.KILL, 35, 0, 200, ChestType.COMMON),
            Template(Kind.KILL, 50, 0, 280, ChestType.RARE),
            Template(Kind.STAGE, 2, 0, 150, ChestType.COMMON),
            Template(Kind.STAGE, 3, 0, 220, ChestType.COMMON),
            Template(Kind.STAGE, 4, 0, 300, ChestType.RARE),
            Template(Kind.SHOOTER, ShooterType.LASER.ordinal, 12, 180, ChestType.COMMON),
            Template(Kind.SHOOTER, ShooterType.RAPID.ordinal, 15, 200, ChestType.COMMON),
            Template(Kind.SHOOTER, ShooterType.SPREAD.ordinal, 12, 190, ChestType.COMMON),
            Template(Kind.SHOOTER, ShooterType.DOUBLE.ordinal, 18, 210, ChestType.COMMON)
        )
        pool.shuffle(rng)

        val picked = mutableListOf<Template>()
        val usedKinds = mutableSetOf<Kind>()
        for (t in pool) {
            if (picked.size >= MISSION_COUNT) break
            if (t.kind in usedKinds) continue
            picked.add(t)
            usedKinds.add(t.kind)
        }
        while (picked.size < MISSION_COUNT) {
            picked.add(pool[rng.nextInt(pool.size)])
        }

        val e = prefs.edit()
        for (i in 0 until MISSION_COUNT) {
            val t = picked[i]
            e.putString(keyKind(i), t.kind.name)
            e.putInt(keyP0(i), t.p0)
            e.putInt(keyP1(i), t.p1)
            e.putInt(keyProg(i), 0)
            e.putBoolean(keyClaimed(i), false)
            e.remove(keyDoneLegacy(i))
            e.putInt(keyRCoins(i), t.coins)
            e.putString(keyRChest(i), t.chest?.name ?: "")
        }
        e.apply()
    }

    private fun maxStageReached(): Int = prefs.getInt(KEY_MAX_STAGE, 0)

    private fun setMaxStageReached(n: Int) {
        val cur = maxStageReached()
        if (n > cur) prefs.edit().putInt(KEY_MAX_STAGE, n).apply()
    }

    fun recordStageSnapshot(stageNumber: Int, upgrade: UpgradeManager, chest: ChestManager, now: Long) {
        ensurePeriod(now)
        if (stageNumber > maxStageReached()) setMaxStageReached(stageNumber)
    }

    fun recordEnemyKilled(equipped: ShooterType, upgrade: UpgradeManager, chest: ChestManager, now: Long) {
        recordEnemyKillsDelta(1, equipped, upgrade, chest, now)
    }

    fun recordEnemyKillsDelta(count: Int, equipped: ShooterType, upgrade: UpgradeManager, chest: ChestManager, now: Long) {
        if (count <= 0) return
        ensurePeriod(now)
        val ed = prefs.edit()
        for (i in 0 until MISSION_COUNT) {
            if (isClaimed(i)) continue
            when (prefs.getString(keyKind(i), "") ?: "") {
                Kind.KILL.name -> {
                    val target = prefs.getInt(keyP0(i), 1)
                    val p = prefs.getInt(keyProg(i), 0) + count
                    ed.putInt(keyProg(i), p.coerceAtMost(target))
                }
                Kind.SHOOTER.name -> {
                    val ord = prefs.getInt(keyP0(i), 0)
                    val target = prefs.getInt(keyP1(i), 1).coerceAtLeast(1)
                    if (equipped.ordinal == ord) {
                        val p = prefs.getInt(keyProg(i), 0) + count
                        ed.putInt(keyProg(i), p.coerceAtMost(target))
                    }
                }
            }
        }
        ed.apply()
    }

    private fun displayProgress(i: Int): Pair<Int, Int> {
        val kind = prefs.getString(keyKind(i), "") ?: ""
        return when (kind) {
            Kind.STAGE.name -> {
                val t = prefs.getInt(keyP0(i), 2)
                val p = maxStageReached().coerceAtMost(t)
                p to t
            }
            Kind.SHOOTER.name -> {
                val t = prefs.getInt(keyP1(i), 1).coerceAtLeast(1)
                val p = prefs.getInt(keyProg(i), 0).coerceAtMost(t)
                p to t
            }
            else -> {
                val t = prefs.getInt(keyP0(i), 1).coerceAtLeast(1)
                val p = prefs.getInt(keyProg(i), 0).coerceAtMost(t)
                p to t
            }
        }
    }

    private fun isObjectiveMet(i: Int): Boolean {
        val kind = prefs.getString(keyKind(i), "") ?: ""
        return when (kind) {
            Kind.STAGE.name -> maxStageReached() >= prefs.getInt(keyP0(i), 999)
            Kind.SHOOTER.name -> prefs.getInt(keyProg(i), 0) >= prefs.getInt(keyP1(i), 999)
            Kind.KILL.name -> prefs.getInt(keyProg(i), 0) >= prefs.getInt(keyP0(i), 999)
            else -> false
        }
    }

    fun claimedMissionCount(): Int = (0 until MISSION_COUNT).count { isClaimed(it) }

    private fun milestoneMask(): Int = prefs.getInt(KEY_MILESTONE_MASK, 0)

    private fun grantMilestones(upgrade: UpgradeManager, chest: ChestManager, now: Long, lines: MutableList<String>) {
        val cnt = claimedMissionCount()
        var mask = milestoneMask()
        val orig = mask
        if (cnt >= 1 && (mask and MS_BIT_1) == 0) {
            upgrade.totalCoins += 75
            lines.add("Milestone: +75 coins")
            mask = mask or MS_BIT_1
        }
        if (cnt >= 2 && (mask and MS_BIT_2) == 0) {
            upgrade.totalCoins += 125
            lines.add("Milestone: +125 coins")
            mask = mask or MS_BIT_2
        }
        if (cnt >= 3 && (mask and MS_BIT_3) == 0) {
            upgrade.totalCoins += 200
            lines.add("Milestone: +200 coins")
            when (chest.tryAddChest(ChestType.RARE, now)) {
                ChestGrantResult.ADDED -> lines.add("Milestone: Rare chest!")
                ChestGrantResult.SLOTS_FULL -> lines.add("Milestone: chest slots full")
            }
            mask = mask or MS_BIT_3
        }
        if (mask != orig) prefs.edit().putInt(KEY_MILESTONE_MASK, mask).apply()
    }

    /**
     * Claim rewards for mission [index]. Returns banner subtitle (mission + milestones), or null if invalid.
     */
    fun claimMission(index: Int, upgrade: UpgradeManager, chest: ChestManager, now: Long): String? {
        ensurePeriod(now)
        if (index !in 0 until MISSION_COUNT) return null
        if (isClaimed(index)) return null
        if (!isObjectiveMet(index)) return null

        val coins = prefs.getInt(keyRCoins(index), 0)
        val chestName = prefs.getString(keyRChest(index), "") ?: ""
        val chestType = if (chestName.isEmpty()) null else try {
            ChestType.valueOf(chestName)
        } catch (_: Exception) {
            null
        }

        val lines = mutableListOf<String>()
        if (coins > 0) {
            upgrade.totalCoins += coins
            lines.add("+${coins} coins")
        }
        if (chestType != null) {
            when (chest.tryAddChest(chestType, now)) {
                ChestGrantResult.ADDED -> lines.add("${chestType.displayName} chest")
                ChestGrantResult.SLOTS_FULL -> lines.add("Chest slots full")
            }
        }

        prefs.edit()
            .putBoolean(keyClaimed(index), true)
            .remove(keyDoneLegacy(index))
            .apply()

        grantMilestones(upgrade, chest, now, lines)

        return (listOf("Mission Complete!") + lines).joinToString("\n")
    }

    /** No-op auto-grant; kept for API compatibility. */
    fun syncCompletions(upgrade: UpgradeManager, chest: ChestManager, now: Long) {
        ensurePeriod(now)
    }

    fun consumeMissionCompletePopup(): String? {
        val s = pendingPopupSubtitle ?: return null
        pendingPopupSubtitle = null
        return s
    }

    fun milestoneBits(): Int = milestoneMask()

    fun missionRows(now: Long): List<DailyMissionRow> {
        ensurePeriod(now)
        val resetMs = timeUntilResetMs(now)
        return List(MISSION_COUNT) { i ->
            val kind = try {
                Kind.valueOf(prefs.getString(keyKind(i), Kind.KILL.name) ?: Kind.KILL.name)
            } catch (_: Exception) {
                Kind.KILL
            }
            val (prog, target) = displayProgress(i)
            val claimed = isClaimed(i)
            val objectiveDone = if (claimed) true else {
                val k = prefs.getString(keyKind(i), "") ?: ""
                when (k) {
                    Kind.STAGE.name -> maxStageReached() >= prefs.getInt(keyP0(i), 999)
                    Kind.SHOOTER.name -> prefs.getInt(keyProg(i), 0) >= prefs.getInt(keyP1(i), 999)
                    Kind.KILL.name -> prefs.getInt(keyProg(i), 0) >= prefs.getInt(keyP0(i), 999)
                    else -> false
                }
            }
            val coins = prefs.getInt(keyRCoins(i), 0)
            val chestName = prefs.getString(keyRChest(i), "") ?: ""
            val chestType = if (chestName.isEmpty()) null else try {
                ChestType.valueOf(chestName)
            } catch (_: Exception) {
                null
            }
            DailyMissionRow(
                kind = kind,
                title = titleFor(kind),
                subtitle = subtitleFor(kind, i),
                progress = prog,
                target = target,
                claimed = claimed,
                canClaim = objectiveDone && !claimed,
                rewardCoins = coins,
                rewardChest = chestType,
                timeUntilResetMs = resetMs
            )
        }
    }

    private fun titleFor(kind: Kind): String = when (kind) {
        Kind.KILL -> "Eliminate zombies"
        Kind.STAGE -> "Push forward"
        Kind.SHOOTER -> "Weapon trial"
    }

    private fun subtitleFor(kind: Kind, i: Int): String {
        return when (kind) {
            Kind.KILL -> "Kill ${prefs.getInt(keyP0(i), 0)} enemies"
            Kind.STAGE -> "Reach stage ${prefs.getInt(keyP0(i), 0)}"
            Kind.SHOOTER -> {
                val ord = prefs.getInt(keyP0(i), 0)
                val st = ShooterType.entries.getOrNull(ord) ?: ShooterType.BASIC
                val name = Shooter.get(st).name
                "Get ${prefs.getInt(keyP1(i), 0)} kills with $name"
            }
        }
    }

    data class DailyMissionRow(
        val kind: Kind,
        val title: String,
        val subtitle: String,
        val progress: Int,
        val target: Int,
        val claimed: Boolean,
        val canClaim: Boolean,
        val rewardCoins: Int,
        val rewardChest: ChestType?,
        val timeUntilResetMs: Long
    )
}

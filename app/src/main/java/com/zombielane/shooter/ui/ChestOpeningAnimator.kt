package com.zombielane.shooter.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.zombielane.shooter.data.ChestAnimState
import com.zombielane.shooter.data.ChestRewards
import com.zombielane.shooter.data.ChestType
import com.zombielane.shooter.data.Shooter
import com.zombielane.shooter.objects.CoinParticle
import com.zombielane.shooter.objects.Particle
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/**
 * Chest open sequence for SurfaceView: move (300ms) → shake (600ms) → flash (300ms) → open swap →
 * reward burst + coin flight (800ms). Optional scale bounce after swap. No per-frame allocations.
 */
class ChestOpeningAnimator {

    companion object {
        private const val T_MOVE_END = 300L
        private const val T_SHAKE_END = 900L
        private const val T_FLASH_START = 900L
        /** Closed → open swap; must match slot [ChestVisualState.OPENED] threshold in GameView. */
        const val T_FLASH_END_MS = 1200L
        private const val T_REWARD_START = 1200L
        private const val T_BOUNCE_END = 1700L
        const val T_DONE_MS = 2000L

        private const val BURST_COUNT = 32
        private const val MAX_REWARD_LINES = 6
        private const val MAX_COIN_FLIGHTS = 16
    }

    var animState: ChestAnimState = ChestAnimState.IDLE
        private set

    private var t0 = 0L
    private var startCx = 0f
    private var startCy = 0f
    private var startHalf = 1f
    private var targetCx = 0f
    private var targetCy = 0f
    private var targetCoinX = 0f
    private var targetCoinY = 0f
    private var chestType: ChestType = ChestType.COMMON

    private var rewardsCoins = 0
    private var rewardLineCount = 0
    private val lineText = arrayOfNulls<String>(MAX_REWARD_LINES)

    private var burstSpawned = false
    private var coinsSpawned = false

    private val scrimPaint = Paint().apply { style = Paint.Style.FILL }
    private val flashPaint = Paint().apply { style = Paint.Style.FILL }
    private val glowRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val glowStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
    private val rewardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val tmpDst = RectF()

    fun start(
        nowMs: Long,
        startCenterX: Float,
        startCenterY: Float,
        startHalfSize: Float,
        type: ChestType,
        rewards: ChestRewards,
        screenCenterX: Float,
        screenCenterY: Float,
        coinHudX: Float,
        coinHudY: Float
    ) {
        t0 = nowMs
        startCx = startCenterX
        startCy = startCenterY
        startHalf = startHalfSize.coerceAtLeast(24f)
        chestType = type
        targetCx = screenCenterX
        targetCy = screenCenterY
        targetCoinX = coinHudX
        targetCoinY = coinHudY
        rewardsCoins = rewards.coins
        fillRewardLines(rewards)
        burstSpawned = false
        coinsSpawned = false
        animState = ChestAnimState.MOVING_TO_CENTER
    }

    private fun fillRewardLines(r: ChestRewards) {
        var n = 0
        if (r.coins > 0 && n < MAX_REWARD_LINES) {
            lineText[n++] = "+${r.coins} coins"
        }
        r.tempShooter?.let { st ->
            if (n < MAX_REWARD_LINES) {
                val m = (r.tempShooterDurationMs / 60000).coerceAtLeast(1)
                lineText[n++] = "${Shooter.get(st).name} (${m}m trial)"
            }
        }
        if (r.nextRunShield && n < MAX_REWARD_LINES) lineText[n++] = "Shield next run"
        if (r.nextRunRapidMs > 0 && n < MAX_REWARD_LINES) {
            lineText[n++] = "Rapid ${r.nextRunRapidMs / 1000}s start"
        }
        rewardLineCount = n
    }

    fun reset() {
        animState = ChestAnimState.IDLE
        burstSpawned = false
        coinsSpawned = false
        rewardLineCount = 0
    }

    fun update(
        nowMs: Long,
        burstSink: MutableList<Particle>,
        coinSink: MutableList<CoinParticle>,
        screenW: Int,
        screenH: Int
    ) {
        if (animState == ChestAnimState.IDLE || animState == ChestAnimState.DONE) return
        val t = nowMs - t0
        animState = when {
            t >= T_DONE_MS -> ChestAnimState.DONE
            t >= T_REWARD_START -> ChestAnimState.REWARD
            t >= T_FLASH_START -> ChestAnimState.FLASH
            t >= T_MOVE_END -> ChestAnimState.SHAKING
            else -> ChestAnimState.MOVING_TO_CENTER
        }

        if (t >= T_REWARD_START && !burstSpawned) {
            burstSpawned = true
            spawnBurst(targetCx, targetCy, burstSink)
        }
        if (t >= T_REWARD_START && !coinsSpawned && rewardsCoins > 0) {
            coinsSpawned = true
            spawnCoinFlights(targetCx, targetCy - 50f, coinSink)
        }

        val laneR = screenW.toFloat()
        var i = 0
        while (i < burstSink.size) {
            burstSink[i].update(screenW, screenH, 0f, laneR)
            if (!burstSink[i].active) burstSink.removeAt(i) else i++
        }
        i = 0
        while (i < coinSink.size) {
            coinSink[i].update()
            if (!coinSink[i].active) coinSink.removeAt(i) else i++
        }
    }

    private fun spawnBurst(cx: Float, cy: Float, sink: MutableList<Particle>) {
        val colors = intArrayOf(
            Color.parseColor("#FFD54F"),
            Color.parseColor("#FFAB00"),
            Color.parseColor("#FFFFFF"),
            glowRgbForTier(chestType)
        )
        var c = 0
        var idx = 0
        while (idx < BURST_COUNT) {
            val ang = (idx / BURST_COUNT.toFloat()) * PI.toFloat() * 2f + (idx * 0.31f)
            val sp = 3.5f + (idx % 6) * 1.6f
            val vx = kotlin.math.cos(ang.toDouble()).toFloat() * sp
            val vy = kotlin.math.sin(ang.toDouble()).toFloat() * sp - 3.5f
            sink.add(Particle(cx, cy, vx, vy, colors[c and 3], life = 20 + (idx and 9)))
            c++
            idx++
        }
    }

    private fun glowRgbForTier(t: ChestType): Int = when (t) {
        ChestType.COMMON -> Color.parseColor("#ECEFF1")
        ChestType.RARE -> Color.parseColor("#42A5F5")
        ChestType.EPIC -> Color.parseColor("#AB47BC")
        ChestType.SUPER -> Color.parseColor("#FFD700")
    }

    private fun spawnCoinFlights(fromX: Float, fromY: Float, sink: MutableList<CoinParticle>) {
        val n = min(MAX_COIN_FLIGHTS, 4 + (rewardsCoins / 25).coerceAtMost(12))
        var i = 0
        while (i < n) {
            val ox = (i - n / 2f) * 18f
            val oy = (i % 3) * 10f
            sink.add(CoinParticle(fromX + ox, fromY + oy, targetCoinX, targetCoinY))
            i++
        }
    }

    fun draw(canvas: Canvas, w: Float, h: Float, nowMs: Long, menuUi: MenuUiAssets) {
        if (animState == ChestAnimState.IDLE || animState == ChestAnimState.DONE) return
        val t = (nowMs - t0).toFloat().coerceAtLeast(0f)

        scrimPaint.color = Color.argb(200, 0, 0, 0)
        canvas.drawRect(0f, 0f, w, h, scrimPaint)

        val moveT = smoothstep(0f, T_MOVE_END.toFloat(), t)
        val cx = lerp(startCx, targetCx, moveT)
        val cy = lerp(startCy, targetCy, moveT)

        val shakeActive = t >= T_MOVE_END && t < T_SHAKE_END
        val shakeDur = (T_SHAKE_END - T_MOVE_END).toFloat()
        val shakeDecay = if (shakeActive) 1f - (t - T_MOVE_END) / shakeDur else 0f
        val shakeX = if (shakeActive) sin(nowMs * 0.024).toFloat() * 16f * shakeDecay else 0f
        val lidBob = lidBounceOffset(t)

        val drawCx = cx + shakeX
        val drawCy = cy + lidBob

        val scaleTotal = chestScaleTotal(t)
        val side = startHalf * 2f * scaleTotal

        val useOpen = t >= T_FLASH_END_MS
        val bmp: Bitmap = if (useOpen) menuUi.chestOpen(chestType) else menuUi.chest(chestType)

        if (t >= T_MOVE_END) {
            drawRadialGlow(canvas, drawCx, drawCy, side, nowMs)
        }

        tmpDst.set(drawCx - side / 2f, drawCy - side / 2f, drawCx + side / 2f, drawCy + side / 2f)
        canvas.drawBitmap(bmp, null, tmpDst, bitmapPaint)

        if (t >= T_FLASH_START && t < T_FLASH_END_MS) {
            val u = (t - T_FLASH_START) / (T_FLASH_END_MS - T_FLASH_START).toFloat()
            val a = (sin(u * PI.toFloat()) * 230f).toInt().coerceIn(0, 255)
            flashPaint.color = Color.argb(a, 255, 255, 255)
            canvas.drawRect(0f, 0f, w, h, flashPaint)
        }

        if (t >= T_REWARD_START && rewardLineCount > 0) {
            val tr = smoothstep(T_REWARD_START.toFloat(), T_REWARD_START + 360f, t)
            rewardPaint.textSize = 28f * (w / 1080f).coerceIn(0.85f, 1.4f)
            var ly = h * 0.58f - 80f * (1f - tr)
            var i = 0
            while (i < rewardLineCount) {
                val line = lineText[i] ?: ""
                val stagger = smoothstep(T_REWARD_START + i * 40f, T_REWARD_START + 160f + i * 40f, t)
                rewardPaint.alpha = (255 * stagger).toInt().coerceIn(0, 255)
                val pop = 0.72f + 0.28f * stagger
                canvas.save()
                canvas.scale(pop, pop, w / 2f, ly)
                canvas.drawText(line, w / 2f, ly, rewardPaint)
                canvas.restore()
                ly += 40f * (w / 1080f).coerceIn(0.9f, 1.2f)
                i++
            }
            rewardPaint.alpha = 255
        }
    }

    /** Small upward bob after the sprite swap (optional lid polish). */
    private fun lidBounceOffset(t: Float): Float {
        if (t < T_FLASH_END_MS || t >= T_BOUNCE_END) return 0f
        val u = (t - T_FLASH_END_MS) / (T_BOUNCE_END - T_FLASH_END_MS).toFloat()
        val decay = 1f - u
        return sin(t * 0.055f) * 10f * decay * decay
    }

    private fun chestScaleTotal(t: Float): Float {
        if (t < T_MOVE_END) {
            return lerp(1f, 1.4f, smoothstep(0f, T_MOVE_END.toFloat(), t))
        }
        if (t < T_FLASH_END_MS) {
            return 1.4f
        }
        return postFlashBounceScale(t)
    }

    /** Lid-style settle: 1.4 → 1.2 → 1.3 → 1.0 by [T_BOUNCE_END]. */
    private fun postFlashBounceScale(t: Float): Float {
        if (t >= T_BOUNCE_END) return 1f
        val u = t - T_FLASH_END_MS
        val d850 = 100f
        val d950 = 100f
        val d1300 = T_BOUNCE_END - T_FLASH_END_MS
        return when {
            u < d850 -> lerp(1.4f, 1.2f, u / d850)
            u < d850 + d950 -> lerp(1.2f, 1.3f, (u - d850) / d950)
            else -> lerp(1.3f, 1.0f, (u - d850 - d950) / (d1300 - d850 - d950))
        }
    }

    private fun drawRadialGlow(canvas: Canvas, drawCx: Float, drawCy: Float, side: Float, nowMs: Long) {
        val rgb = glowRgbForTier(chestType)
        val r0 = Color.red(rgb)
        val g0 = Color.green(rgb)
        val b0 = Color.blue(rgb)
        val pulse = (sin(nowMs * 0.014) * 0.5 + 0.5).toFloat()
        var layer = 5
        while (layer >= 0) {
            val rr = side * 0.42f + layer * 22f
            val alpha = ((28 + layer * 18) * (0.55f + pulse * 0.45f)).toInt().coerceIn(0, 220)
            glowRingPaint.color = Color.argb(alpha, r0, g0, b0)
            canvas.drawCircle(drawCx, drawCy, rr, glowRingPaint)
            layer--
        }
        glowStrokePaint.color = Color.argb((140 + 80 * pulse).toInt(), r0, g0, b0)
        glowStrokePaint.strokeWidth = 5f + 3f * pulse
        tmpDst.set(drawCx - side / 2f - 14f, drawCy - side / 2f - 14f, drawCx + side / 2f + 14f, drawCy + side / 2f + 14f)
        canvas.drawRoundRect(tmpDst, 28f, 28f, glowStrokePaint)
    }

    fun drawParticles(canvas: Canvas, burst: List<Particle>) {
        var i = 0
        val n = burst.size
        while (i < n) {
            burst[i].draw(canvas)
            i++
        }
    }

    fun drawCoinParticles(canvas: Canvas, coins: List<CoinParticle>) {
        var i = 0
        val n = coins.size
        while (i < n) {
            coins[i].draw(canvas)
            i++
        }
    }

    private fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
        val tt = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        return tt * tt * (3f - 2f * tt)
    }

    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
}

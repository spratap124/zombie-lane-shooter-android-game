package com.zombielane.shooter.engine

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.zombielane.shooter.data.UpgradeManager
import com.zombielane.shooter.objects.*
import com.zombielane.shooter.ui.HUD
import kotlin.random.Random

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    companion object {
        private const val BG_COLOR = 0xFF1B1B2F.toInt()
        private const val GAME_PADDING = 24f
        private const val POWER_UP_DROP_CHANCE = 0.12f
        private const val RAPID_FIRE_DURATION_MS = 5000L
    }

    private var gameThread: GameThread? = null
    private lateinit var player: Player
    private val bullets = mutableListOf<Bullet>()
    private val enemies = mutableListOf<Enemy>()
    private val particles = mutableListOf<Particle>()
    private val powerUps = mutableListOf<PowerUp>()
    private val floatingTexts = mutableListOf<FloatingText>()
    private val coinParticles = mutableListOf<CoinParticle>()
    private val enemySpawner = EnemySpawner()
    private val comboTracker = ComboTracker()
    private val eventManager = GameEventManager()
    private val hud = HUD()
    private val upgradeManager = UpgradeManager(context)

    private var screenW = 0
    private var screenH = 0
    private var score = 0
    private var sessionCoins = 0
    private var gameOver = false
    private var lastFireTimeMs = 0L
    private var screenShakeFrames = 0
    private var screenShakeIntensity = 0f

    val safeArea = RectF()
    private var insetLeft = 0
    private var insetTop = 0
    private var insetRight = 0
    private var insetBottom = 0

    private val starPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private var stars = emptyList<Triple<Float, Float, Float>>()

    private val nearDeathOverlayPaint = Paint().apply {
        color = Color.parseColor("#18FF0000")
        style = Paint.Style.FILL
    }

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    fun setSystemInsets(left: Int, top: Int, right: Int, bottom: Int) {
        insetLeft = left
        insetTop = top
        insetRight = right
        insetBottom = bottom
        recalcSafeArea()
    }

    private fun recalcSafeArea() {
        safeArea.set(
            insetLeft + GAME_PADDING,
            insetTop + GAME_PADDING,
            screenW - insetRight - GAME_PADDING,
            screenH - insetBottom - GAME_PADDING
        )
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        screenW = width
        screenH = height
        recalcSafeArea()

        stars = List(80) {
            Triple(
                Random.nextFloat() * screenW,
                Random.nextFloat() * screenH,
                1f + Random.nextFloat() * 2.5f
            )
        }

        resetGame()
        startThread()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        screenW = width
        screenH = height
        recalcSafeArea()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopThread()
    }

    private fun startThread() {
        gameThread = GameThread(holder, this).apply {
            running = true
            start()
        }
    }

    private fun stopThread() {
        gameThread?.running = false
        var retry = true
        while (retry) {
            try {
                gameThread?.join()
                retry = false
            } catch (_: InterruptedException) {
            }
        }
        gameThread = null
    }

    fun pause() { gameThread?.pause() }
    fun resume() { gameThread?.unpause() }

    private fun resetGame() {
        player = Player(screenW, screenH, upgradeManager.maxHealth, safeArea)
        bullets.clear()
        enemies.clear()
        particles.clear()
        powerUps.clear()
        floatingTexts.clear()
        coinParticles.clear()
        enemySpawner.reset()
        comboTracker.reset()
        eventManager.reset()
        score = 0
        sessionCoins = 0
        lastFireTimeMs = System.currentTimeMillis()
        gameOver = false
        screenShakeFrames = 0
    }

    // ── UPDATE ──────────────────────────────────────────────

    fun update() {
        if (gameOver) return

        val now = System.currentTimeMillis()
        player.update(screenW, screenH, safeArea)
        comboTracker.update()

        // Events
        handleEvents(now)

        // Auto-fire
        val fireInterval = if (player.rapidFireUntilMs > now)
            (upgradeManager.fireIntervalMs / 3).coerceAtLeast(30L)
        else
            upgradeManager.fireIntervalMs

        if (now - lastFireTimeMs >= fireInterval) {
            lastFireTimeMs = now
            bullets.add(Bullet(player.gunTipX, player.gunTipY, upgradeManager.damage))
        }

        bullets.forEach { it.update(screenW, screenH) }
        enemies.forEach { it.update(screenW, screenH) }
        particles.forEach { it.update(screenW, screenH) }
        powerUps.forEach { it.update(screenW, screenH) }
        floatingTexts.forEach { it.update() }
        coinParticles.forEach { it.update() }

        val spawned = enemySpawner.update(screenW, score, safeArea)
        enemies.addAll(spawned)

        checkBulletEnemyCollisions()
        checkPowerUpCollisions()

        bullets.removeAll { !it.active }
        enemies.removeAll { !it.active }
        particles.removeAll { !it.active }
        powerUps.removeAll { !it.active }
        floatingTexts.removeAll { !it.active }
        coinParticles.removeAll { !it.active }

        // Enemies reaching the player
        val reached = enemies.filter { it.y + it.height > player.y }
        for (enemy in reached) {
            player.takeDamage()
            spawnDeathParticles(enemy)
            enemy.active = false
            screenShakeFrames = 12
            screenShakeIntensity = 10f
        }

        if (screenShakeFrames > 0) screenShakeFrames--

        if (player.isDead) onGameOver()
    }

    private fun handleEvents(now: Long) {
        val triggered = eventManager.update(now)

        when (eventManager.currentEvent) {
            GameEvent.RUSH -> {
                enemySpawner.speedMultiplier = 1.5f
                enemySpawner.coinMultiplier = 1
            }
            GameEvent.COIN_RAIN -> {
                enemySpawner.speedMultiplier = 0.8f
                enemySpawner.coinMultiplier = 3
            }
            else -> {
                enemySpawner.speedMultiplier = 1f
                enemySpawner.coinMultiplier = 1
            }
        }

        if (triggered == GameEvent.SWARM) {
            enemies.addAll(enemySpawner.spawnBurst(8, safeArea, score))
            screenShakeFrames = 20
            screenShakeIntensity = 6f
        }

        if (triggered != null) {
            floatingTexts.add(
                FloatingText(
                    screenW / 2f, screenH * 0.35f,
                    triggered.label,
                    when (triggered) {
                        GameEvent.RUSH -> Color.parseColor("#FF5722")
                        GameEvent.COIN_RAIN -> Color.parseColor("#FFD600")
                        GameEvent.SWARM -> Color.parseColor("#F44336")
                    },
                    size = 52f, life = 80
                )
            )
        }
    }

    private fun checkBulletEnemyCollisions() {
        val bulletsToRemove = mutableSetOf<Bullet>()

        for (bullet in bullets) {
            if (!bullet.active) continue
            for (enemy in enemies) {
                if (!enemy.active) continue
                if (bullet.collidesWith(enemy)) {
                    bulletsToRemove.add(bullet)
                    enemy.takeDamage(bullet.damage)

                    if (enemy.isDead) {
                        onEnemyKilled(enemy)
                    }
                    break
                }
            }
        }

        bulletsToRemove.forEach { it.active = false }
    }

    private fun onEnemyKilled(enemy: Enemy) {
        comboTracker.onKill()

        val comboScore = (enemy.scoreValue * comboTracker.multiplier).toInt()
        score += comboScore
        sessionCoins += enemy.coinValue
        spawnDeathParticles(enemy)

        // Coin fly animation
        val hudCoinX = safeArea.left + 32f
        val hudCoinY = safeArea.top + 84f
        repeat(enemy.coinValue.coerceAtMost(5)) { i ->
            coinParticles.add(
                CoinParticle(
                    enemy.x + enemy.width / 2f + Random.nextFloat() * 20f - 10f,
                    enemy.y + enemy.height / 2f + Random.nextFloat() * 20f - 10f,
                    hudCoinX, hudCoinY
                )
            )
        }

        // Floating score text
        val comboColor = when {
            comboTracker.combo >= 10 -> Color.parseColor("#FF5722")
            comboTracker.combo >= 5 -> Color.parseColor("#FFD600")
            comboTracker.combo >= 2 -> Color.parseColor("#4CAF50")
            else -> Color.WHITE
        }
        floatingTexts.add(
            FloatingText(
                enemy.x + enemy.width / 2f,
                enemy.y,
                if (comboTracker.isActive) "+$comboScore x${comboTracker.combo}" else "+$comboScore",
                comboColor
            )
        )

        // Boss kill: big shake + extra coins
        if (enemy.type == EnemyType.BOSS) {
            screenShakeFrames = 25
            screenShakeIntensity = 15f
            floatingTexts.add(
                FloatingText(
                    screenW / 2f, screenH * 0.4f,
                    "BOSS DESTROYED!",
                    Color.parseColor("#FFD600"),
                    size = 48f, life = 70
                )
            )
        }

        // Splitter: spawn 2 small fast enemies
        if (enemy.type == EnemyType.SPLITTER) {
            val offsets = listOf(-30f, 30f)
            for (off in offsets) {
                enemies.add(
                    Enemy(
                        (enemy.x + enemy.width / 2f + off).coerceIn(0f, screenW - Enemy.SIZE),
                        enemy.y,
                        speed = 1.5f + Random.nextFloat() * 0.8f,
                        scoreValue = 5, coinValue = 1,
                        bodyColor = Color.parseColor("#CE93D8"),
                        health = 1, type = EnemyType.FAST
                    )
                )
            }
        }

        // Power-up drop
        if (Random.nextFloat() < POWER_UP_DROP_CHANCE) {
            val type = PowerUpType.entries[Random.nextInt(PowerUpType.entries.size)]
            powerUps.add(PowerUp(enemy.x + enemy.width / 2f, enemy.y + enemy.height / 2f, type))
        }
    }

    private fun checkPowerUpCollisions() {
        for (pu in powerUps) {
            if (!pu.active) continue
            if (pu.collidesWith(player)) {
                pu.active = false
                applyPowerUp(pu.type)
            }
        }
    }

    private fun applyPowerUp(type: PowerUpType) {
        val label: String
        val color: Int

        when (type) {
            PowerUpType.RAPID_FIRE -> {
                player.rapidFireUntilMs = System.currentTimeMillis() + RAPID_FIRE_DURATION_MS
                label = "RAPID FIRE!"
                color = Color.parseColor("#FF9800")
            }
            PowerUpType.SHIELD -> {
                player.shielded = true
                label = "SHIELD!"
                color = Color.parseColor("#2196F3")
            }
            PowerUpType.BOMB -> {
                for (enemy in enemies) {
                    if (enemy.active) {
                        score += enemy.scoreValue
                        sessionCoins += enemy.coinValue
                        spawnDeathParticles(enemy)
                        enemy.active = false
                    }
                }
                screenShakeFrames = 20
                screenShakeIntensity = 12f
                label = "BOMB!"
                color = Color.parseColor("#F44336")
            }
        }

        floatingTexts.add(
            FloatingText(player.gunTipX, player.y - 30f, label, color, size = 42f, life = 55)
        )
    }

    private fun onGameOver() {
        gameOver = true
        upgradeManager.totalCoins += sessionCoins
        if (score > upgradeManager.highScore) {
            upgradeManager.highScore = score
        }
    }

    private fun spawnDeathParticles(enemy: Enemy) {
        val cx = enemy.x + enemy.width / 2f
        val cy = enemy.y + enemy.height / 2f
        val colors = intArrayOf(
            Color.parseColor("#FF5722"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#FFEB3B"),
            Color.WHITE
        )
        val count = if (enemy.type == EnemyType.BOSS) 24 else 12

        repeat(count) {
            val angle = Random.nextFloat() * Math.PI.toFloat() * 2f
            val speed = 2f + Random.nextFloat() * 5f
            particles.add(
                Particle(
                    cx, cy,
                    kotlin.math.cos(angle) * speed,
                    kotlin.math.sin(angle) * speed,
                    colors[Random.nextInt(colors.size)],
                    15 + Random.nextInt(10)
                )
            )
        }
    }

    // ── DRAW ────────────────────────────────────────────────

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        // Screen shake offset
        val shakeX = if (screenShakeFrames > 0) (Random.nextFloat() - 0.5f) * screenShakeIntensity * 2 else 0f
        val shakeY = if (screenShakeFrames > 0) (Random.nextFloat() - 0.5f) * screenShakeIntensity * 2 else 0f

        canvas.save()
        canvas.translate(shakeX, shakeY)

        canvas.drawColor(BG_COLOR)

        for ((sx, sy, sr) in stars) {
            starPaint.alpha = (100 + Random.nextInt(155))
            canvas.drawCircle(sx, sy, sr, starPaint)
        }

        bullets.forEach { it.draw(canvas) }
        powerUps.forEach { it.draw(canvas) }
        enemies.forEach { it.draw(canvas) }
        particles.forEach { it.draw(canvas) }
        coinParticles.forEach { it.draw(canvas) }
        player.draw(canvas)
        floatingTexts.forEach { it.draw(canvas) }

        // Near-death red pulse
        if (player.isNearDeath && !gameOver) {
            nearDeathOverlayPaint.alpha = (40 + (kotlin.math.sin(System.currentTimeMillis() * 0.006) * 30).toInt())
                .coerceIn(0, 255)
            canvas.drawRect(0f, 0f, screenW.toFloat(), screenH.toFloat(), nearDeathOverlayPaint)
        }

        canvas.restore()

        hud.draw(
            canvas, score, sessionCoins,
            if (gameOver) upgradeManager.totalCoins else sessionCoins,
            player.health, player.maxHealth,
            gameOver, if (gameOver) upgradeManager else null,
            safeArea, comboTracker, eventManager
        )
    }

    // ── TOUCH ───────────────────────────────────────────────

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (gameOver) {
                    handleGameOverTouch(event.x, event.y)
                } else {
                    player.targetX = event.x
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!gameOver) {
                    player.targetX = event.x
                }
            }
        }
        return true
    }

    private fun handleGameOverTouch(touchX: Float, touchY: Float) {
        val types = UpgradeManager.UpgradeType.entries
        for (i in hud.upgradeBtnRects.indices) {
            if (i < types.size && hud.upgradeBtnRects[i].contains(touchX, touchY)) {
                upgradeManager.purchase(types[i])
                return
            }
        }

        if (hud.restartBtnRect.contains(touchX, touchY)) {
            resetGame()
        }
    }
}

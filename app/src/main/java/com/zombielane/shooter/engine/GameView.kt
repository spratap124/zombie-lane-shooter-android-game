package com.zombielane.shooter.engine

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import com.zombielane.shooter.ads.AdManager
import com.zombielane.shooter.data.ChestGrantResult
import com.zombielane.shooter.data.ChestManager
import com.zombielane.shooter.data.ChestOpenResult
import com.zombielane.shooter.data.ChestRevealPhase
import com.zombielane.shooter.data.RunBuffManager
import com.zombielane.shooter.data.SettingsManager
import com.zombielane.shooter.data.StreakManager
import com.zombielane.shooter.data.ShooterManager
import com.zombielane.shooter.data.ShooterType
import com.zombielane.shooter.data.UpgradeManager
import com.zombielane.shooter.objects.*
import com.zombielane.shooter.ui.*
import kotlin.random.Random

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    companion object {
        private const val GAME_PADDING = 24f
        private const val SIDE_PADDING = 48f
        private const val POWER_UP_DROP_CHANCE = 0.12f
        private const val RAPID_FIRE_DURATION_MS = 5000L
    }

    private var gameThread: GameThread? = null
    private lateinit var player: Player
    private val bullets = mutableListOf<Bullet>()
    private val enemies = mutableListOf<Enemy>()
    private val enemyBullets = mutableListOf<EnemyBullet>()
    private val particles = mutableListOf<Particle>()
    private val powerUps = mutableListOf<PowerUp>()
    private val floatingTexts = mutableListOf<FloatingText>()
    private val coinParticles = mutableListOf<CoinParticle>()
    private val enemySpawner = EnemySpawner()
    private val comboTracker = ComboTracker()
    private val eventManager = GameEventManager()
    private val stageManager = StageManager()

    private val hud = HUD()
    private val menuScreen = MenuScreen()
    private val menuUiAssets = MenuUiAssets(resources)
    private val pauseScreen = PauseScreen()
    private val settingsScreen = SettingsScreen()
    private val gameOverScreen = GameOverScreen()
    private val shopScreen = ShopScreen()
    val upgradeManager = UpgradeManager(context)
    val settingsManager = SettingsManager(context)
    val shooterManager = ShooterManager(context)
    val chestManager = ChestManager(context)
    private val streakManager = StreakManager(context)
    private val runBuffManager = RunBuffManager(context)
    private val chestScreen = ChestScreen()
    private val chestRevealUI = ChestRevealUI()
    private val playerAssets = PlayerAssets(resources)
    private val enemyAssets = EnemyAssets(resources)
    var adManager: AdManager? = null
    private var pendingRewardShooterType: ShooterType? = null
    private var lastEquippedShooter: ShooterType = shooterManager.equipped
    private var chestToastUntilMs: Long = 0L
    private var chestToastText: String? = null
    private var gameOverChestBanner: String? = null
    private var gameOverChestBannerOk: Boolean = true
    private var streakPopup: StreakManager.StreakPopup? = null
    private var chestMergeMode: Boolean = false
    private var chestMergePick: Int = -1
    private var chestRevealPhase: ChestRevealPhase = ChestRevealPhase.HIDDEN
    private var chestRevealPhaseStartMs: Long = 0L
    private var chestRevealResult: ChestOpenResult? = null

    var state = GameState.MENU
        private set
    private var previousState = GameState.MENU

    private var screenW = 0
    private var screenH = 0
    private var score = 0
    private var sessionCoins = 0
    private var lastFireTimeMs = 0L
    private var screenShakeFrames = 0
    private var screenShakeIntensity = 0f
    private var gameStartTimeMs = 0L
    private var gameEndTimeMs = 0L
    private var enemiesKilled = 0
    private var maxCombo = 0

    // FPS counter
    private var frameCount = 0
    private var lastFpsTimeMs = 0L
    private var currentFps = 0

    val safeArea = RectF()
    private var insetLeft = 0
    private var insetTop = 0
    private var insetRight = 0
    private var insetBottom = 0

    private val backgroundManager = BackgroundManager()

    private val nearDeathOverlayPaint = Paint().apply {
        color = Color.parseColor("#18FF0000")
        style = Paint.Style.FILL
    }

    init {
        holder.addCallback(this)
        isFocusable = true
        Enemy.bindAssets(enemyAssets)
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
            insetLeft + SIDE_PADDING,
            insetTop + GAME_PADDING,
            screenW - insetRight - SIDE_PADDING,
            screenH - insetBottom - GAME_PADDING
        )
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        screenW = width
        screenH = height
        recalcSafeArea()
        backgroundManager.init(screenW, screenH)

        state = GameState.MENU
        startThread()
        post { onEnteredMainMenu() }
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

    fun onBackPressed(): Boolean {
        return when (state) {
            GameState.PLAYING -> { state = GameState.PAUSED; post { adManager?.showBanner() }; true }
            GameState.PAUSED -> { state = GameState.PLAYING; post { adManager?.hideBanner() }; true }
            GameState.SETTINGS -> { state = previousState; settingsScreen.confirmResetActive = false; true }
            GameState.SHOP -> { state = previousState; true }
            GameState.CHESTS -> {
                if (chestRevealPhase != ChestRevealPhase.HIDDEN && chestRevealPhase != ChestRevealPhase.DONE) {
                    /* Let reveal play; back still cancels double-offer to claim base */
                    if (chestRevealPhase == ChestRevealPhase.DOUBLE_OFFER) {
                        finishChestClaim(multiplyDouble = false)
                    }
                }
                chestMergeMode = false
                chestMergePick = -1
                state = GameState.MENU
                true
            }
            GameState.GAME_OVER -> {
                state = GameState.MENU
                onEnteredMainMenu()
                true
            }
            GameState.MENU -> false
        }
    }

    private fun resetGame() {
        player = Player(screenW, screenH, upgradeManager.maxHealth, safeArea)
        runBuffManager.applyToPlayer(player)
        syncPlayerBitmap()
        bullets.clear()
        enemies.clear()
        enemyBullets.clear()
        particles.clear()
        powerUps.clear()
        floatingTexts.clear()
        coinParticles.clear()
        enemySpawner.reset()
        comboTracker.reset()
        eventManager.reset()
        stageManager.reset()
        score = 0
        sessionCoins = 0
        enemiesKilled = 0
        maxCombo = 0
        lastFireTimeMs = System.currentTimeMillis()
        gameStartTimeMs = System.currentTimeMillis()
        gameEndTimeMs = 0L
        screenShakeFrames = 0
        state = GameState.PLAYING
        gameOverChestBanner = null
        post { adManager?.hideBanner() }
    }

    // ── UPDATE ──────────────────────────────────────────────

    fun update() {
        updateFps()

        when (state) {
            GameState.PLAYING -> updateGame()
            GameState.CHESTS -> updateChestRevealAnimation(System.currentTimeMillis())
            else -> {}
        }
    }

    private fun updateFps() {
        frameCount++
        val now = System.currentTimeMillis()
        if (now - lastFpsTimeMs >= 1000L) {
            currentFps = frameCount
            frameCount = 0
            lastFpsTimeMs = now
        }
    }

    private fun syncPlayerBitmap() {
        val current = shooterManager.equipped
        if (::player.isInitialized) {
            player.currentBitmap = playerAssets.get(current)
        }
        lastEquippedShooter = current
    }

    private fun updateGame() {
        val now = System.currentTimeMillis()
        player.update(screenW, screenH, safeArea)
        comboTracker.update()

        handleEvents(now)
        shooterManager.checkExpiry()

        if (shooterManager.equipped != lastEquippedShooter) {
            syncPlayerBitmap()
        }

        val shooter = shooterManager.getEquipped()
        val baseInterval = (shooter.baseFireRateMs - upgradeManager.fireRateReductionMs).coerceAtLeast(20L)
        val fireInterval = if (player.rapidFireUntilMs > now)
            (baseInterval / 3).coerceAtLeast(15L)
        else
            baseInterval

        if (now - lastFireTimeMs >= fireInterval) {
            lastFireTimeMs = now
            bullets.addAll(BulletManager.spawnPattern(shooter, player.gunTipX, player.gunTipY, upgradeManager.damage))
        }

        bullets.forEach { it.update(screenW, screenH) }
        enemies.forEach { it.update(screenW, screenH) }
        enemyBullets.forEach { it.update(screenW, screenH) }
        particles.forEach { it.update(screenW, screenH) }
        powerUps.forEach { it.update(screenW, screenH) }
        floatingTexts.forEach { it.update() }
        coinParticles.forEach { it.update() }

        for (enemy in enemies) {
            val spawned = EnemyBulletManager.tryShoot(enemy, player.x + player.width / 2f, player.y, now)
            enemyBullets.addAll(spawned)
        }

        val stage = stageManager.currentStage

        val bossActive = stageManager.bossSpawned && !stageManager.bossDefeated

        if (!stageManager.isTransitioning) {
            if (!bossActive) {
                val spawned = enemySpawner.update(screenW, score, safeArea, stage)
                enemies.addAll(spawned)
            }

            if (stageManager.shouldSpawnBoss()) {
                enemies.add(enemySpawner.spawnBoss(safeArea, score, stage))
                stageManager.markBossSpawned()
                floatingTexts.add(FloatingText(
                    screenW / 2f, screenH * 0.3f, "STAGE ${stage.stageNumber} BOSS!",
                    Color.parseColor("#F44336"), size = 52f, life = 90
                ))
                screenShakeFrames = 20
                screenShakeIntensity = 8f
            }
        }

        checkBulletEnemyCollisions()
        checkEnemyBulletPlayerCollisions()
        checkPowerUpCollisions()

        bullets.removeAll { !it.active }
        enemies.removeAll { !it.active }
        stageManager.clearStuckBossFightIfNoBossOnField(
            enemies.any { it.type == EnemyType.BOSS && it.active }
        )
        enemyBullets.removeAll { !it.active }
        particles.removeAll { !it.active }
        powerUps.removeAll { !it.active }
        floatingTexts.removeAll { !it.active }
        coinParticles.removeAll { !it.active }

        val reached = enemies.filter { it.y + it.height > player.y }
        for (enemy in reached) {
            if (enemy.type == EnemyType.BOSS) {
                player.takeDamage()
                enemy.y = player.y - enemy.height - 20f
                screenShakeFrames = 12
                screenShakeIntensity = 10f
            } else {
                player.takeDamage()
                spawnDeathParticles(enemy)
                enemy.active = false
                screenShakeFrames = 12
                screenShakeIntensity = 10f
            }
        }

        if (screenShakeFrames > 0) screenShakeFrames--

        val prevStageNum = stageManager.currentStage.stageNumber
        stageManager.update()
        val newStageNum = stageManager.currentStage.stageNumber
        if (newStageNum != prevStageNum) {
            val newStage = stageManager.currentStage
            val banner =
                if (newStage.isEndlessSector && prevStageNum <= Stage.INTRO_STAGE_COUNT)
                    "ENDLESS RUN · ${newStage.name.uppercase()}"
                else
                    "STAGE ${newStage.stageNumber}: ${newStage.name}"
            floatingTexts.add(FloatingText(
                screenW / 2f, screenH * 0.35f, banner,
                Color.parseColor("#4CAF50"), size = 50f, life = 120
            ))
        }

        if (player.isDead) onGameOver()
    }

    private fun handleEvents(now: Long) {
        val triggered = eventManager.update(now)

        when (eventManager.currentEvent) {
            GameEvent.RUSH -> { enemySpawner.speedMultiplier = 1.5f; enemySpawner.coinMultiplier = 1 }
            GameEvent.COIN_RAIN -> { enemySpawner.speedMultiplier = 0.8f; enemySpawner.coinMultiplier = 3 }
            else -> { enemySpawner.speedMultiplier = 1f; enemySpawner.coinMultiplier = 1 }
        }

        val bossUp = stageManager.bossSpawned && !stageManager.bossDefeated
        if (triggered == GameEvent.SWARM && !bossUp) {
            enemies.addAll(enemySpawner.spawnBurst(8, safeArea, score, stageManager.currentStage))
            screenShakeFrames = 20
            screenShakeIntensity = 6f
        }

        if (triggered != null) {
            floatingTexts.add(
                FloatingText(
                    screenW / 2f, screenH * 0.35f, triggered.label,
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
                    if (enemy.isDead) onEnemyKilled(enemy)
                    break
                }
            }
        }

        bulletsToRemove.forEach { it.active = false }
    }

    private fun checkEnemyBulletPlayerCollisions() {
        for (eb in enemyBullets) {
            if (!eb.active) continue
            if (eb.collidesWith(player)) {
                eb.active = false
                if (player.isInvincible) continue
                if (player.shielded) {
                    player.shielded = false
                    continue
                }
                player.takeDamage()
                screenShakeFrames = 8
                screenShakeIntensity = 6f
            }
        }
    }

    private fun onEnemyKilled(enemy: Enemy) {
        comboTracker.onKill()
        enemiesKilled++
        stageManager.onEnemyKilled(enemy.type == EnemyType.BOSS)
        if (comboTracker.combo > maxCombo) maxCombo = comboTracker.combo

        val comboScore = (enemy.scoreValue * comboTracker.multiplier).toInt()
        score += comboScore
        sessionCoins += enemy.coinValue
        spawnDeathParticles(enemy)

        val hudCoinX = safeArea.left + 32f
        val hudCoinY = safeArea.top + 84f
        repeat(enemy.coinValue.coerceAtMost(5)) {
            coinParticles.add(
                CoinParticle(
                    enemy.x + enemy.width / 2f + Random.nextFloat() * 20f - 10f,
                    enemy.y + enemy.height / 2f + Random.nextFloat() * 20f - 10f,
                    hudCoinX, hudCoinY
                )
            )
        }

        val comboColor = when {
            comboTracker.combo >= 10 -> Color.parseColor("#FF5722")
            comboTracker.combo >= 5 -> Color.parseColor("#FFD600")
            comboTracker.combo >= 2 -> Color.parseColor("#4CAF50")
            else -> Color.WHITE
        }
        floatingTexts.add(
            FloatingText(
                enemy.x + enemy.width / 2f, enemy.y,
                if (comboTracker.isActive) "+$comboScore x${comboTracker.combo}" else "+$comboScore",
                comboColor
            )
        )

        if (enemy.type == EnemyType.BOSS) {
            screenShakeFrames = 25
            screenShakeIntensity = 15f
            floatingTexts.add(FloatingText(screenW / 2f, screenH * 0.4f, "BOSS DESTROYED!", Color.parseColor("#FFD600"), size = 48f, life = 70))
        }

        if (enemy.type == EnemyType.SPLITTER) {
            for (off in listOf(-30f, 30f)) {
                enemies.add(
                    Enemy(
                        (enemy.x + enemy.width / 2f + off).coerceIn(0f, screenW - Enemy.SIZE),
                        enemy.y, speed = 1.5f + Random.nextFloat() * 0.8f,
                        scoreValue = 5, coinValue = 1,
                        bodyColor = Color.parseColor("#CE93D8"), health = 1, type = EnemyType.FAST
                    )
                )
            }
        }

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
            PowerUpType.RAPID_FIRE -> { player.rapidFireUntilMs = System.currentTimeMillis() + RAPID_FIRE_DURATION_MS; label = "RAPID FIRE!"; color = Color.parseColor("#FF9800") }
            PowerUpType.SHIELD -> { player.shielded = true; label = "SHIELD!"; color = Color.parseColor("#2196F3") }
            PowerUpType.BOMB -> {
                // Must update stage manager (boss defeated, kill counts) or boss fights desync from real enemies on screen.
                for (enemy in enemies.toList()) {
                    if (!enemy.active) continue
                    score += enemy.scoreValue
                    sessionCoins += enemy.coinValue
                    spawnDeathParticles(enemy)
                    when (enemy.type) {
                        EnemyType.BOSS -> stageManager.onEnemyKilled(true)
                        else -> stageManager.onEnemyKilled(false)
                    }
                    enemy.active = false
                }
                screenShakeFrames = 20; screenShakeIntensity = 12f; label = "BOMB!"; color = Color.parseColor("#F44336")
            }
        }
        floatingTexts.add(FloatingText(player.gunTipX, player.y - 30f, label, color, size = 42f, life = 55))
    }

    private fun onGameOver() {
        state = GameState.GAME_OVER
        gameEndTimeMs = System.currentTimeMillis()
        upgradeManager.totalCoins += sessionCoins
        if (score > upgradeManager.highScore) upgradeManager.highScore = score

        when (chestManager.grantRunChest(stageManager.currentStage.stageNumber, score)) {
            ChestGrantResult.ADDED -> {
                gameOverChestBanner = "Chest added — open CHESTS in menu!"
                gameOverChestBannerOk = true
            }
            ChestGrantResult.SLOTS_FULL -> {
                gameOverChestBanner = "Chest slots full!"
                gameOverChestBannerOk = false
            }
        }

        adManager?.onPlayerDeath()
        if (adManager?.shouldShowInterstitial() == true) {
            post { adManager?.showInterstitial() }
        }
        post { adManager?.showBanner() }
    }

    private fun spawnDeathParticles(enemy: Enemy) {
        val cx = enemy.x + enemy.width / 2f
        val cy = enemy.y + enemy.height / 2f
        val colors = intArrayOf(Color.parseColor("#FF5722"), Color.parseColor("#FF9800"), Color.parseColor("#FFEB3B"), Color.WHITE)
        val count = if (enemy.type == EnemyType.BOSS) 24 else 12
        repeat(count) {
            val angle = Random.nextFloat() * Math.PI.toFloat() * 2f
            val speed = 2f + Random.nextFloat() * 5f
            particles.add(Particle(cx, cy, kotlin.math.cos(angle) * speed, kotlin.math.sin(angle) * speed, colors[Random.nextInt(colors.size)], 15 + Random.nextInt(10)))
        }
    }

    // ── DRAW ────────────────────────────────────────────────

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        if (state != GameState.MENU) {
            drawBackground(canvas)
        }

        when (state) {
            GameState.MENU -> {
                val nowMs = System.currentTimeMillis()
                val toast = if (nowMs < chestToastUntilMs) chestToastText else null
                val pop = streakPopup
                menuScreen.draw(
                    canvas, safeArea, upgradeManager.highScore, upgradeManager.totalCoins, shooterManager,
                    playerAssets, menuUiAssets, chestManager.getSlots(nowMs), streakManager.currentStreak(), toast,
                    if (pop != null) pop.title else null,
                    if (pop != null) pop.message else null,
                    nowMs
                )
            }
            GameState.CHESTS -> {
                val now = System.currentTimeMillis()
                chestScreen.draw(
                    canvas, safeArea, chestManager.getSlots(now), now,
                    chestMergeMode, chestMergePick, streakManager.currentStreak()
                )
                val rr = chestRevealResult
                if (rr != null && chestRevealPhase != ChestRevealPhase.HIDDEN && chestRevealPhase != ChestRevealPhase.DONE) {
                    chestRevealUI.draw(canvas, safeArea, chestRevealPhase, chestRevealPhaseStartMs, now, rr)
                }
            }
            GameState.PLAYING -> drawGameplay(canvas)
            GameState.PAUSED -> { drawGameplay(canvas); pauseScreen.draw(canvas, safeArea, score, sessionCoins) }
            GameState.GAME_OVER -> { drawGameplay(canvas); drawGameOverOverlay(canvas) }
            GameState.SETTINGS -> settingsScreen.draw(canvas, safeArea, settingsManager)
            GameState.SHOP -> shopScreen.draw(canvas, safeArea, upgradeManager.totalCoins, shooterManager, playerAssets)
        }
    }

    private fun drawBackground(canvas: Canvas) {
        val bgType = if (state == GameState.PLAYING || state == GameState.PAUSED || state == GameState.GAME_OVER)
            stageManager.currentStage.backgroundType
        else
            BackgroundType.SPACE
        backgroundManager.draw(canvas, bgType)
    }

    private fun drawGameplay(canvas: Canvas) {
        val shakeX = if (screenShakeFrames > 0) (Random.nextFloat() - 0.5f) * screenShakeIntensity * 2 else 0f
        val shakeY = if (screenShakeFrames > 0) (Random.nextFloat() - 0.5f) * screenShakeIntensity * 2 else 0f

        canvas.save()
        canvas.translate(shakeX, shakeY)

        bullets.forEach { it.draw(canvas) }
        enemyBullets.forEach { it.draw(canvas) }
        powerUps.forEach { it.draw(canvas) }
        enemies.forEach { it.draw(canvas) }
        particles.forEach { it.draw(canvas) }
        coinParticles.forEach { it.draw(canvas) }
        player.draw(canvas)
        floatingTexts.forEach { it.draw(canvas) }

        if (player.isNearDeath && state == GameState.PLAYING) {
            nearDeathOverlayPaint.alpha = (40 + (kotlin.math.sin(System.currentTimeMillis() * 0.006) * 30).toInt()).coerceIn(0, 255)
            canvas.drawRect(0f, 0f, screenW.toFloat(), screenH.toFloat(), nearDeathOverlayPaint)
        }

        canvas.restore()

        if (state == GameState.PLAYING) {
            val shooter = shooterManager.getEquipped()
            val tempRemaining = shooterManager.getRemainingTempMs(shooterManager.equipped)
            val isTemp = !shooterManager.isUnlocked(shooterManager.equipped) && shooterManager.isTemporaryActive(shooterManager.equipped)
            val stage = stageManager.currentStage
            hud.drawGameHud(canvas, score, sessionCoins, player.health, player.maxHealth, safeArea, comboTracker, eventManager, settingsManager.showFps, currentFps, shooter.name, shooter.bulletColor, if (isTemp) tempRemaining else -1L, stage.stageNumber, stage.name, stageManager.stageProgress, stage.isEndlessSector)
        }
    }

    private fun drawGameOverOverlay(canvas: Canvas) {
        val survived = if (gameEndTimeMs > 0) gameEndTimeMs - gameStartTimeMs else 0L
        gameOverScreen.draw(
            canvas, safeArea, score, sessionCoins, upgradeManager.totalCoins, maxCombo, enemiesKilled, survived, upgradeManager,
            chestBanner = gameOverChestBanner,
            chestBannerOk = gameOverChestBannerOk
        )
    }

    // ── TOUCH ───────────────────────────────────────────────

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN && event.action != MotionEvent.ACTION_MOVE) return true
        val tx = event.x
        val ty = event.y

        when (state) {
            GameState.MENU -> if (event.action == MotionEvent.ACTION_DOWN) handleMenuTouch(tx, ty)
            GameState.PLAYING -> handlePlayingTouch(event.action, tx, ty)
            GameState.PAUSED -> if (event.action == MotionEvent.ACTION_DOWN) handlePauseTouch(tx, ty)
            GameState.GAME_OVER -> if (event.action == MotionEvent.ACTION_DOWN) handleGameOverTouch(tx, ty)
            GameState.SETTINGS -> if (event.action == MotionEvent.ACTION_DOWN) handleSettingsTouch(tx, ty)
            GameState.SHOP -> if (event.action == MotionEvent.ACTION_DOWN) handleShopTouch(tx, ty)
            GameState.CHESTS -> if (event.action == MotionEvent.ACTION_DOWN) handleChestScreenTouch(tx, ty)
        }
        return true
    }

    private fun handleMenuTouch(tx: Float, ty: Float) {
        if (streakPopup != null) {
            if (menuScreen.streakOkRect.contains(tx, ty)) streakPopup = null
            return
        }
        when {
            menuScreen.chestsNavRect.contains(tx, ty) -> {
                previousState = GameState.MENU
                chestMergeMode = false
                chestMergePick = -1
                state = GameState.CHESTS
            }
            menuScreen.playBtnRect.contains(tx, ty) -> resetGame()
            menuScreen.shopBtnRect.contains(tx, ty) -> {
                previousState = GameState.MENU
                state = GameState.SHOP
            }
            menuScreen.settingsBtnRect.contains(tx, ty) -> {
                previousState = GameState.MENU
                state = GameState.SETTINGS
                settingsScreen.confirmResetActive = false
            }
        }
    }

    private fun handlePlayingTouch(action: Int, tx: Float, ty: Float) {
        if (action == MotionEvent.ACTION_DOWN && hud.pauseBtnRect.contains(tx, ty)) {
            state = GameState.PAUSED
            return
        }
        player.targetX = tx
    }

    private fun handlePauseTouch(tx: Float, ty: Float) {
        when {
            pauseScreen.resumeBtnRect.contains(tx, ty) -> { state = GameState.PLAYING; post { adManager?.hideBanner() } }
            pauseScreen.settingsBtnRect.contains(tx, ty) -> {
                previousState = GameState.PAUSED
                state = GameState.SETTINGS
                settingsScreen.confirmResetActive = false
            }
            pauseScreen.quitBtnRect.contains(tx, ty) -> {
                state = GameState.MENU
                onEnteredMainMenu()
            }
        }
    }

    private fun handleGameOverTouch(tx: Float, ty: Float) {
        val types = UpgradeManager.UpgradeType.entries
        for (i in gameOverScreen.upgradeBtnRects.indices) {
            if (i < types.size && gameOverScreen.upgradeBtnRects[i].contains(tx, ty)) {
                upgradeManager.purchase(types[i])
                return
            }
        }
        when {
            gameOverScreen.shopBtnRect.contains(tx, ty) -> {
                previousState = GameState.GAME_OVER
                state = GameState.SHOP
            }
            gameOverScreen.playAgainBtnRect.contains(tx, ty) -> resetGame()
            gameOverScreen.menuBtnRect.contains(tx, ty) -> {
                state = GameState.MENU
                onEnteredMainMenu()
            }
        }
    }

    private fun handleSettingsTouch(tx: Float, ty: Float) {
        val toggles = settingsScreen.toggleRects
        if (toggles.size >= 1 && toggles[0].contains(tx, ty)) settingsManager.soundEnabled = !settingsManager.soundEnabled
        if (toggles.size >= 2 && toggles[1].contains(tx, ty)) settingsManager.musicEnabled = !settingsManager.musicEnabled
        if (toggles.size >= 3 && toggles[2].contains(tx, ty)) settingsManager.vibrationEnabled = !settingsManager.vibrationEnabled
        if (toggles.size >= 4 && toggles[3].contains(tx, ty)) settingsManager.showFps = !settingsManager.showFps

        if (settingsScreen.resetBtnRect.contains(tx, ty)) {
            if (settingsScreen.confirmResetActive) {
                upgradeManager.resetProgress()
                shooterManager.resetProgress()
                chestManager.resetProgress()
                streakManager.resetProgress()
                runBuffManager.resetProgress()
                settingsScreen.confirmResetActive = false
            } else {
                settingsScreen.confirmResetActive = true
            }
        } else {
            settingsScreen.confirmResetActive = false
        }

        if (settingsScreen.backBtnRect.contains(tx, ty)) {
            state = previousState
            settingsScreen.confirmResetActive = false
        }
    }

    private fun handleShopTouch(tx: Float, ty: Float) {
        val shooterTypes = ShooterType.entries
        for (i in shopScreen.shooterCardRects.indices) {
            if (i < shooterTypes.size && shopScreen.shooterCardRects[i].contains(tx, ty)) {
                val st = shooterTypes[i]
                when {
                    shooterManager.isAvailable(st) -> shooterManager.equip(st)
                    shooterManager.unlock(st, upgradeManager) -> shooterManager.equip(st)
                    else -> showRewardedForShooter(st)
                }
                return
            }
        }

        if (shopScreen.backBtnRect.contains(tx, ty)) {
            state = previousState
        }
    }

    // ── ADS ──────────────────────────────────────────────────

    private fun showRewardedForShooter(type: ShooterType) {
        val am = adManager ?: return
        if (!am.isRewardedReady()) return
        pendingRewardShooterType = type
        post {
            am.showRewarded(object : AdManager.RewardListener {
                override fun onRewardEarned() {
                    pendingRewardShooterType?.let { st ->
                        shooterManager.unlockTemporarily(st)
                        shooterManager.equip(st)
                        pendingRewardShooterType = null
                    }
                }
            })
        }
    }

    private fun onEnteredMainMenu() {
        streakManager.onMenuEnter(chestManager)?.let { streakPopup = it }
    }

    private fun updateChestRevealAnimation(now: Long) {
        val r = chestRevealResult ?: return
        val elapsed = now - chestRevealPhaseStartMs
        when (chestRevealPhase) {
            ChestRevealPhase.SPINNING -> if (elapsed >= 2800L) goChestPhase(ChestRevealPhase.REVEAL, now)
            ChestRevealPhase.REVEAL -> if (elapsed >= 2200L) {
                if (r.luckyBonus) goChestPhase(ChestRevealPhase.LUCKY, now)
                else goChestPhase(ChestRevealPhase.DOUBLE_OFFER, now)
            }
            ChestRevealPhase.LUCKY -> if (elapsed >= 1100L) goChestPhase(ChestRevealPhase.DOUBLE_OFFER, now)
            else -> {}
        }
    }

    private fun goChestPhase(phase: ChestRevealPhase, now: Long) {
        chestRevealPhase = phase
        chestRevealPhaseStartMs = now
        if (phase == ChestRevealPhase.REVEAL || phase == ChestRevealPhase.LUCKY) chestVibrateOpen()
    }

    private fun startChestReveal(slotIndex: Int) {
        val now = System.currentTimeMillis()
        val res = chestManager.openReadySlot(slotIndex, now) ?: return
        chestRevealResult = res
        chestRevealPhase = ChestRevealPhase.SPINNING
        chestRevealPhaseStartMs = now
        chestVibrateOpen()
    }

    private fun finishChestClaim(multiplyDouble: Boolean) {
        val r = chestRevealResult ?: return
        var rw = r.rewards
        if (multiplyDouble) rw = rw.scaled(2f)
        chestManager.applyRewards(rw, upgradeManager, shooterManager, runBuffManager)
        syncPlayerBitmap()
        chestToastText = rw.describe()
        chestToastUntilMs = System.currentTimeMillis() + 4500L
        chestRevealPhase = ChestRevealPhase.HIDDEN
        chestRevealResult = null
        chestVibrateOpen()
    }

    private fun showRewardedDoubleChest() {
        val am = adManager
        if (am == null || !am.isRewardedReady()) {
            finishChestClaim(multiplyDouble = true)
            return
        }
        post {
            am.showRewarded(object : AdManager.RewardListener {
                override fun onRewardEarned() {
                    finishChestClaim(multiplyDouble = true)
                }
            })
        }
    }

    private fun showRewardedSkipTimer(slotIndex: Int) {
        val am = adManager
        if (am == null || !am.isRewardedReady()) {
            chestManager.skipTimerForSlot(slotIndex)
            return
        }
        post {
            am.showRewarded(object : AdManager.RewardListener {
                override fun onRewardEarned() {
                    chestManager.skipTimerForSlot(slotIndex)
                }
            })
        }
    }

    private fun handleChestScreenTouch(tx: Float, ty: Float) {
        val rr = chestRevealResult
        if (rr != null && chestRevealPhase == ChestRevealPhase.DOUBLE_OFFER) {
            when {
                chestRevealUI.doubleAdRect.contains(tx, ty) -> showRewardedDoubleChest()
                chestRevealUI.claimRect.contains(tx, ty) -> finishChestClaim(multiplyDouble = false)
            }
            return
        }
        if (rr != null && chestRevealPhase != ChestRevealPhase.HIDDEN) return

        if (chestScreen.backBtnRect.contains(tx, ty)) {
            chestMergeMode = false
            chestMergePick = -1
            state = GameState.MENU
            return
        }
        if (chestScreen.mergeBtnRect.contains(tx, ty)) {
            chestMergeMode = !chestMergeMode
            chestMergePick = -1
            return
        }

        val now = System.currentTimeMillis()
        val slots = chestManager.getSlots(now)
        for (i in 0 until ChestManager.MAX_SLOTS) {
            if (slots[i] == null) continue
            if (chestScreen.slotSkipRects[i].contains(tx, ty) && !slots[i]!!.isReady(now)) {
                showRewardedSkipTimer(i)
                return
            }
            if (chestScreen.slotOpenRects[i].contains(tx, ty) && slots[i]!!.isReady(now)) {
                startChestReveal(i)
                return
            }
            if (chestMergeMode && chestScreen.slotCardRects[i].contains(tx, ty)) {
                if (chestMergePick < 0) chestMergePick = i
                else if (chestMergePick != i) {
                    if (chestManager.tryMergeSlots(chestMergePick, i)) {
                        chestMergeMode = false
                        chestMergePick = -1
                        chestVibrateOpen()
                    } else {
                        chestMergePick = i
                    }
                }
                return
            }
        }
    }

    private fun chestVibrateOpen() {
        if (!settingsManager.vibrationEnabled) return
        val v = ContextCompat.getSystemService(context, Vibrator::class.java) ?: return
        if (!v.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= 26) {
            v.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
        } else @Suppress("DEPRECATION") v.vibrate(40)
    }
}

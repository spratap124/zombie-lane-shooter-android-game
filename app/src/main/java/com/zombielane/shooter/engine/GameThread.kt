package com.zombielane.shooter.engine

import android.graphics.Canvas
import android.view.SurfaceHolder

class GameThread(
    private val surfaceHolder: SurfaceHolder,
    private val gameView: GameView
) : Thread() {

    companion object {
        const val TARGET_FPS = 60
    }

    @Volatile
    var running = false

    @Volatile
    var paused = false

    private val pauseLock = Object()

    fun pause() {
        paused = true
    }

    fun unpause() {
        synchronized(pauseLock) {
            paused = false
            pauseLock.notifyAll()
        }
    }

    override fun run() {
        var startTime: Long
        var elapsed: Long
        var sleepTime: Long

        while (running) {
            synchronized(pauseLock) {
                while (paused && running) {
                    try {
                        pauseLock.wait()
                    } catch (_: InterruptedException) {
                    }
                }
            }

            if (!running) break

            startTime = System.currentTimeMillis()
            var canvas: Canvas? = null

            try {
                canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    synchronized(surfaceHolder) {
                        gameView.update()
                        gameView.draw(canvas)
                    }
                }
            } finally {
                canvas?.let {
                    try {
                        surfaceHolder.unlockCanvasAndPost(it)
                    } catch (_: Exception) {
                    }
                }
            }

            elapsed = System.currentTimeMillis() - startTime
            val framePeriod = gameView.targetFramePeriodMs().toDouble().coerceIn(1.0, 500.0)
            sleepTime = (framePeriod - elapsed).toLong()

            if (sleepTime > 0) {
                try {
                    sleep(sleepTime)
                } catch (_: InterruptedException) {
                }
            }
        }
    }
}

package com.zombielane.shooter

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.zombielane.shooter.ads.AdManager
import com.zombielane.shooter.engine.GameView

class GameActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var adManager: AdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        goFullscreen()

        adManager = AdManager(this)
        adManager.initialize()

        gameView = GameView(this)
        gameView.adManager = adManager

        val root = FrameLayout(this)
        root.addView(gameView, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))

        val bannerView = adManager.createBannerAd()
        val bannerParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, Gravity.BOTTOM)
        root.addView(bannerView, bannerParams)

        setContentView(root)

        ViewCompat.setOnApplyWindowInsetsListener(gameView) { _, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            gameView.setSystemInsets(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!gameView.onBackPressed()) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
        adManager.onPause()
    }

    override fun onResume() {
        super.onResume()
        gameView.resume()
        adManager.onResume()
    }

    override fun onDestroy() {
        if (::gameView.isInitialized) {
            gameView.releaseAudio()
        }
        adManager.onDestroy()
        super.onDestroy()
    }

    private fun goFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

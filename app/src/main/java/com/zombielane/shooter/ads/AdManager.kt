package com.zombielane.shooter.ads

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.zombielane.shooter.BuildConfig

class AdManager(private val activity: Activity) {

    companion object {
        private const val TAG = "AdManager"

        private val BANNER_ID = BuildConfig.ADMOB_BANNER_ID
        private val INTERSTITIAL_ID = BuildConfig.ADMOB_INTERSTITIAL_ID
        private val REWARDED_ID = BuildConfig.ADMOB_REWARDED_ID

        /** Show an interstitial after every Nth completed game over (persists across app restarts). */
        private const val GAME_OVERS_PER_INTERSTITIAL = 4

        private const val PREFS = "zombie_lane_ad_manager"
        private const val KEY_GAME_OVER_COUNT = "game_over_count_interstitial"
    }

    interface RewardListener {
        fun onRewardEarned()
    }

    var bannerAdView: AdView? = null
        private set

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var rewardListener: RewardListener? = null

    private val rewardedLoadLock = Any()
    private var rewardedAdLoading = false
    private val rewardedLoadCallbacks = ArrayList<(Boolean) -> Unit>()
    private var pendingRewardType: String? = null

    private val prefs: SharedPreferences =
        activity.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private var isInitialized = false

    fun initialize() {
        MobileAds.initialize(activity) {
            isInitialized = true
            Log.d(TAG, "MobileAds SDK initialized")
            loadInterstitial()
            loadRewarded()
        }
    }

    // ── Banner ──────────────────────────────────────────────

    fun createBannerAd(): AdView {
        val adView = AdView(activity).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = BANNER_ID
        }
        bannerAdView = adView
        adView.loadAd(AdRequest.Builder().build())
        return adView
    }

    fun showBanner() {
        bannerAdView?.let { ad ->
            if (ad.visibility != View.VISIBLE) {
                ad.visibility = View.VISIBLE
            }
        }
    }

    fun hideBanner() {
        bannerAdView?.let { ad ->
            if (ad.visibility != View.GONE) {
                ad.visibility = View.GONE
            }
        }
    }

    // ── Interstitial ────────────────────────────────────────

    private fun loadInterstitial() {
        InterstitialAd.load(
            activity,
            INTERSTITIAL_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(TAG, "Interstitial loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    Log.w(TAG, "Interstitial failed to load: ${error.message}")
                }
            }
        )
    }

    /**
     * Call once when a run ends on the game-over screen (not per life lost mid-run).
     * Increments persisted count; every [GAME_OVERS_PER_INTERSTITIAL]th game over is eligible for an interstitial.
     */
    fun onPlayerDeath() {
        val next = prefs.getInt(KEY_GAME_OVER_COUNT, 0) + 1
        prefs.edit().putInt(KEY_GAME_OVER_COUNT, next).apply()
        // Next game over might be the 3rd — ensure we have an ad ready if the last load failed.
        if (next % GAME_OVERS_PER_INTERSTITIAL == GAME_OVERS_PER_INTERSTITIAL - 1 && interstitialAd == null) {
            loadInterstitial()
        }
    }

    fun shouldShowInterstitial(): Boolean {
        val c = prefs.getInt(KEY_GAME_OVER_COUNT, 0)
        return c > 0 &&
            c % GAME_OVERS_PER_INTERSTITIAL == 0 &&
            interstitialAd != null
    }

    fun showInterstitial(onDismissed: (() -> Unit)? = null) {
        val ad = interstitialAd ?: run {
            onDismissed?.invoke()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                loadInterstitial()
                onDismissed?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                loadInterstitial()
                onDismissed?.invoke()
            }
        }
        ad.show(activity)
    }

    // ── Rewarded ────────────────────────────────────────────

    /**
     * Invokes [callback] on the main thread when a rewarded ad is available ([true]) or load failed / SDK down ([false]).
     * Coalesces overlapping loads so only one [RewardedAd.load] runs; multiple waiters are all notified together.
     */
    fun ensureRewardedLoaded(callback: (Boolean) -> Unit) {
        if (!isInitialized) {
            activity.runOnUiThread { callback(false) }
            return
        }
        synchronized(rewardedLoadLock) {
            if (rewardedAd != null) {
                activity.runOnUiThread { callback(true) }
                return
            }
            rewardedLoadCallbacks.add(callback)
            if (rewardedAdLoading) {
                return
            }
            rewardedAdLoading = true
        }
        RewardedAd.load(
            activity,
            REWARDED_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    val toNotify = synchronized(rewardedLoadLock) {
                        rewardedAd = ad
                        rewardedAdLoading = false
                        rewardedLoadCallbacks.toList().also { rewardedLoadCallbacks.clear() }
                    }
                    Log.d(TAG, "Rewarded ad loaded")
                    activity.runOnUiThread {
                        toNotify.forEach { it(true) }
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    val toNotify = synchronized(rewardedLoadLock) {
                        rewardedAd = null
                        rewardedAdLoading = false
                        rewardedLoadCallbacks.toList().also { rewardedLoadCallbacks.clear() }
                    }
                    Log.w(TAG, "Rewarded ad failed to load: ${error.message}")
                    activity.runOnUiThread {
                        toNotify.forEach { it(false) }
                    }
                }
            }
        )
    }

    private fun loadRewarded() {
        ensureRewardedLoaded { }
    }

    fun isRewardedReady(): Boolean = rewardedAd != null

    /** Loads a rewarded ad if the SDK is up and the cache is empty (menu, chests, after a failed show). */
    fun preloadRewarded() {
        if (isInitialized && rewardedAd == null) ensureRewardedLoaded { }
    }

    fun showRewarded(listener: RewardListener) {
        val ad = rewardedAd ?: return
        rewardListener = listener

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewarded()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                loadRewarded()
            }
        }

        ad.show(activity) {
            rewardListener?.onRewardEarned()
            rewardListener = null
        }
    }

    /**
     * Shows rewarded ad on the UI thread.
     *
     * [onComplete] runs on the main thread: [earned] is true if the user received the reward;
     * [failedToShow] is true if the loaded ad failed to present (distinct from closing without earning).
     * If no ad was loaded, both are false.
     */
    fun showRewardedAd(onComplete: (earned: Boolean, failedToShow: Boolean) -> Unit) {
        val ad = rewardedAd ?: run {
            activity.runOnUiThread { onComplete(false, false) }
            return
        }
        var finished = false
        var earnedReward = false
        fun finish(earned: Boolean, failedToShow: Boolean) {
            if (finished) return
            finished = true
            activity.runOnUiThread { onComplete(earned, failedToShow) }
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewarded()
                finish(earnedReward, false)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "Rewarded failed to show: ${error.message}")
                rewardedAd = null
                loadRewarded()
                finish(false, true)
            }
        }

        try {
            ad.show(activity) {
                earnedReward = true
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Rewarded show threw", t)
            rewardedAd = null
            loadRewarded()
            finish(false, true)
        }
    }

    // ── Lifecycle ───────────────────────────────────────────

    fun onPause() { bannerAdView?.pause() }

    fun onResume() {
        bannerAdView?.resume()
        if (isInitialized && rewardedAd == null) loadRewarded()
    }
    fun onDestroy() { bannerAdView?.destroy() }
}

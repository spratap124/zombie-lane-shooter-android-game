package com.zombielane.shooter.ads

import android.app.Activity
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

        private const val DEATHS_PER_INTERSTITIAL = 3
    }

    interface RewardListener {
        fun onRewardEarned()
    }

    var bannerAdView: AdView? = null
        private set

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var rewardListener: RewardListener? = null
    private var pendingRewardType: String? = null

    private var deathCount = 0
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

    fun onPlayerDeath() {
        deathCount++
    }

    fun shouldShowInterstitial(): Boolean =
        deathCount > 0 && deathCount % DEATHS_PER_INTERSTITIAL == 0 && interstitialAd != null

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

    private fun loadRewarded() {
        RewardedAd.load(
            activity,
            REWARDED_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Log.d(TAG, "Rewarded ad loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    Log.w(TAG, "Rewarded ad failed to load: ${error.message}")
                }
            }
        )
    }

    fun isRewardedReady(): Boolean = rewardedAd != null

    /** Loads a rewarded ad if the SDK is up and the cache is empty (menu, chests, after a failed show). */
    fun preloadRewarded() {
        if (isInitialized && rewardedAd == null) loadRewarded()
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

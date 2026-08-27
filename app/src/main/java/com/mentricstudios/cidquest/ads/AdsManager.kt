package com.mentricstudios.cidquest.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Single place that owns every non-banner ad format: init, preloading the
 * next ad the moment one is shown/fails, and showing.
 *
 * Banners are simple `AdView`s hosted directly in Compose (see
 * [com.mentricstudios.cidquest.ads.BannerAd]) and don't need any of this, but
 * interstitial / rewarded / rewarded-interstitial ads are one-shot objects
 * in the Google Mobile Ads SDK — each one has to be loaded fresh, shown at
 * most once, then thrown away and reloaded for next time. Centralizing that
 * here means every screen that wants to show one of these just calls
 * `AdsManager.showInterstitial(activity) { ... }` instead of re-implementing
 * load/retry/callback bookkeeping locally.
 */
object AdsManager {

    private const val TAG = "AdsManager"
    private const val RETRY_DELAY_MS = 20_000L
    private val retryHandler = Handler(Looper.getMainLooper())

    @Volatile private var initialized = false

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private var interstitialLoading = false
    private var rewardedLoading = false

    /** Call once, e.g. from Application.onCreate or MainActivity.onCreate. */
    fun initialize(context: Context) {
        if (initialized) return
        initialized = true
        MobileAds.initialize(context.applicationContext) {
            Log.d(TAG, "Mobile Ads SDK initialized")
        }
        preloadInterstitial(context)
        preloadRewarded(context)
    }

    // ---------------------------------------------------------------- //
    // Interstitial
    // ---------------------------------------------------------------- //

    /** True when a preloaded interstitial is sitting ready to show right now —
     * lets a call site decide whether "eligible" (per [AdFrequency]) also
     * means "actually able to show something" before consuming the slot. */
    fun isInterstitialReady(): Boolean = interstitialAd != null

    fun preloadInterstitial(context: Context) {
        if (interstitialAd != null || interstitialLoading) return
        interstitialLoading = true
        InterstitialAd.load(
            context.applicationContext,
            AdIds.INTERSTITIAL,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialLoading = false
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialLoading = false
                    interstitialAd = null
                    Log.d(TAG, "Interstitial failed to load: ${error.message}")
                    // Don't just give up — a failed load (bad connection blip,
                    // no fill for that request, etc.) used to leave nothing
                    // preloaded until the next incidental preload call, so a
                    // whole eligible ad slot could pass with nothing to show.
                    // Retrying after a short delay keeps an interstitial ready
                    // far more of the time.
                    retryHandler.postDelayed({ preloadInterstitial(context) }, RETRY_DELAY_MS)
                }
            }
        )
    }

    /**
     * Shows a preloaded interstitial if one is ready; otherwise just calls
     * [onDismissed] immediately (never blocks gameplay/navigation waiting on
     * an ad) and kicks off a fresh load for next time.
     */
    fun showInterstitial(activity: Activity, onDismissed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad == null) {
            preloadInterstitial(activity)
            onDismissed()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                preloadInterstitial(activity)
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                preloadInterstitial(activity)
                onDismissed()
            }
        }
        ad.show(activity)
    }

    // ---------------------------------------------------------------- //
    // Rewarded
    // ---------------------------------------------------------------- //

    fun preloadRewarded(context: Context) {
        if (rewardedAd != null || rewardedLoading) return
        rewardedLoading = true
        RewardedAd.load(
            context.applicationContext,
            AdIds.REWARDED,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedLoading = false
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedLoading = false
                    rewardedAd = null
                    Log.d(TAG, "Rewarded failed to load: ${error.message}")
                    retryHandler.postDelayed({ preloadRewarded(context) }, RETRY_DELAY_MS)
                }
            }
        )
    }

    /** True when a rewarded ad is preloaded and ready to show right now —
     * lets a screen only show its "Watch Ad" button once there's actually
     * something to watch. */
    fun isRewardedReady(): Boolean = rewardedAd != null

    /**
     * Shows a rewarded ad if ready. [onReward] fires only if the user
     * actually earned the reward (watched to completion); [onClosed] always
     * fires once the ad flow is fully done, whether rewarded or not — good
     * place to reload the next one / resume game state.
     */
    fun showRewarded(
        activity: Activity,
        onReward: () -> Unit,
        onClosed: () -> Unit = {},
        onNotReady: () -> Unit = {}
    ) {
        val ad = rewardedAd
        if (ad == null) {
            preloadRewarded(activity)
            onNotReady()
            return
        }
        var earnedReward = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                preloadRewarded(activity)
                onClosed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                preloadRewarded(activity)
                onNotReady()
            }
        }
        ad.show(activity) { rewardItem ->
            earnedReward = true
            onReward()
        }
    }
}

package com.example.whatsappcleaner.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdManager(private val context: Context) {

    companion object {
        private const val TAG = "AdManager"
        // Google test ad units
        private const val INTERSTITIAL_TEST_AD_UNIT = "ca-app-pub-3940256099942544/1033173712"
        private const val REWARDED_TEST_AD_UNIT = "ca-app-pub-3940256099942544/5224354917"
    }

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    fun initialize() {
        MobileAds.initialize(context) { }
        preloadInterstitial()
        preloadRewarded()
    }

    fun preloadInterstitial() {
        if (interstitialAd != null) return
        InterstitialAd.load(
            context,
            INTERSTITIAL_TEST_AD_UNIT,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    Log.w(TAG, "Interstitial failed to load: ${error.message}")
                }
            }
        )
    }

    fun preloadRewarded() {
        if (rewardedAd != null) return
        RewardedAd.load(
            context,
            REWARDED_TEST_AD_UNIT,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    Log.w(TAG, "Rewarded failed to load: ${error.message}")
                }
            }
        )
    }

    fun showInterstitial(activity: Activity, onDismissed: () -> Unit) {
        val ad = interstitialAd
        if (ad == null) {
            preloadInterstitial()
            onDismissed()
            return
        }
        interstitialAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preloadInterstitial()
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                preloadInterstitial()
                onDismissed()
            }
        }
        ad.show(activity)
    }

    fun showRewarded(
        activity: Activity,
        onRewarded: (RewardItem) -> Unit,
        onDismissed: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad == null) {
            preloadRewarded()
            onDismissed()
            return
        }
        rewardedAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preloadRewarded()
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                preloadRewarded()
                onDismissed()
            }
        }
        ad.show(activity) { rewardItem -> onRewarded(rewardItem) }
    }
}

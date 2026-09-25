package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdMobConfig {
    const val APP_ID = "ca-app-pub-2495210736619569~2199272945"
    const val BANNER_AD_ID = "ca-app-pub-2495210736619569/8315967037"
    const val INTERSTITIAL_AD_ID = "ca-app-pub-2495210736619569/1558986999"
    const val REWARDED_INTERSTITIAL_AD_ID = "ca-app-pub-2495210736619569/8754950734"
}

class AdMobManager(private val context: Context) {
    private val TAG = "AdMobManager"

    private var interstitialAd: InterstitialAd? = null
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null

    private val _isRewardedAdReady = MutableStateFlow(false)
    val isRewardedAdReady: StateFlow<Boolean> = _isRewardedAdReady.asStateFlow()

    private var lastInterstitialShownTime = 0L

    init {
        try {
            Thread {
                try {
                    MobileAds.initialize(context) { status ->
                        Log.d(TAG, "AdMob Initialized: ${status.adapterStatusMap}")
                        loadInterstitialAd()
                        loadRewardedInterstitialAd()
                    }
                } catch (e: Throwable) {
                    Log.w(TAG, "MobileAds background init warning: ${e.message}")
                }
            }.start()
        } catch (e: Throwable) {
            Log.w(TAG, "AdMob initialization warning: ${e.message}")
        }
    }

    fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            AdMobConfig.INTERSTITIAL_AD_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(TAG, "Interstitial Ad loaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    Log.w(TAG, "Interstitial failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    fun showInterstitialAd(activity: Activity, onDismissed: () -> Unit = {}) {
        val now = System.currentTimeMillis()
        // Rate limit: show at most once every 45 seconds to avoid intrusive UX
        if (now - lastInterstitialShownTime < 45_000) {
            onDismissed()
            return
        }

        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    lastInterstitialShownTime = System.currentTimeMillis()
                    loadInterstitialAd()
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    onDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    lastInterstitialShownTime = System.currentTimeMillis()
                }
            }
            ad.show(activity)
        } ?: run {
            loadInterstitialAd()
            onDismissed()
        }
    }

    fun loadRewardedInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedInterstitialAd.load(
            context,
            AdMobConfig.REWARDED_INTERSTITIAL_AD_ID,
            adRequest,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitialAd = ad
                    _isRewardedAdReady.value = true
                    Log.d(TAG, "Rewarded Interstitial loaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedInterstitialAd = null
                    _isRewardedAdReady.value = false
                    Log.w(TAG, "Rewarded Interstitial failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    fun showRewardedInterstitialAd(
        activity: Activity,
        onUserEarnedReward: (amount: Int, type: String) -> Unit,
        onAdClosedOrFailed: () -> Unit
    ) {
        rewardedInterstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedInterstitialAd = null
                    _isRewardedAdReady.value = false
                    loadRewardedInterstitialAd()
                    onAdClosedOrFailed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedInterstitialAd = null
                    _isRewardedAdReady.value = false
                    loadRewardedInterstitialAd()
                    onAdClosedOrFailed()
                }
            }
            ad.show(activity) { rewardItem ->
                onUserEarnedReward(rewardItem.amount, rewardItem.type)
            }
        } ?: run {
            // Not ready yet, retry load and notify caller
            loadRewardedInterstitialAd()
            onAdClosedOrFailed()
        }
    }
}

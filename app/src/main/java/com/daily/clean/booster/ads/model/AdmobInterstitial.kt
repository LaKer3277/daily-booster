package com.daily.clean.booster.ads.model

import android.app.Activity
import com.daily.clean.booster.ads.AdsListener
import com.daily.clean.booster.ads.conf.AdConf
import com.daily.clean.booster.ads.conf.AdPos
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.interstitial.InterstitialAd

class AdmobInterstitial(val adPos: AdPos, adCong: AdConf): BaseIns(adPos, adCong), OnPaidEventListener {

    private var interstitialAd: InterstitialAd? = null

    override fun loaded(ad: Any, adsListener: AdsListener) {
        super.loaded(ad, adsListener)
        if (ad is InterstitialAd) {
            interstitialAd = ad
            ad.onPaidEventListener = this
        }
    }

    override fun onPaidEvent(p0: AdValue) {
        //taichiEvent(p0, interstitialAd?.responseInfo)
    }

    override fun show(activity: Activity): Boolean {
        if (interstitialAd == null) return false
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                unitAdShown.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                unitAdClose.invoke()
            }

            override fun onAdDismissedFullScreenContent() {
                unitAdClose.invoke()
            }

            override fun onAdClicked() {
                unitAdClick.invoke()
            }
        }

        interstitialAd?.show(activity)
        return true
    }

}
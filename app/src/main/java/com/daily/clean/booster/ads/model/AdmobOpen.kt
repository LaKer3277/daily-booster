package com.daily.clean.booster.ads.model

import android.app.Activity
import com.daily.clean.booster.ads.AdsListener
import com.daily.clean.booster.ads.conf.AdConf
import com.daily.clean.booster.ads.conf.AdPos
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.appopen.AppOpenAd

class AdmobOpen(val adPos: AdPos, adCong: AdConf): BaseIns(adPos, adCong), OnPaidEventListener {

    private var openAd: AppOpenAd? = null

    override fun loaded(ad: Any, adsListener: AdsListener) {
        super.loaded(ad, adsListener)
        if (ad is AppOpenAd) {
            openAd = ad
            ad.onPaidEventListener = this
        }
    }

    override fun show(activity: Activity): Boolean {
        if (openAd == null) return false
        openAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                unitAdClick.invoke()
            }

            override fun onAdShowedFullScreenContent() {
                unitAdShown.invoke()
            }

            override fun onAdDismissedFullScreenContent() {
                unitAdClose.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                unitAdClose.invoke()
            }
        }

        openAd?.show(activity)
        return true
    }

    override fun onPaidEvent(p0: AdValue) {
        //taichiEvent(p0, openAd?.responseInfo)
    }

}
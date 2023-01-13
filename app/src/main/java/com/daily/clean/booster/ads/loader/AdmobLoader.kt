package com.daily.clean.booster.ads.loader

import android.content.Context
import com.daily.clean.booster.ads.conf.AdConf
import com.daily.clean.booster.ads.conf.AdPos
import com.daily.clean.booster.ads.AdsListener
import com.daily.clean.booster.ads.model.AdmobInterstitial
import com.daily.clean.booster.ads.model.AdmobNative
import com.daily.clean.booster.ads.model.AdmobOpen
import com.daily.clean.booster.ads.model.BaseAd
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAdOptions

class AdmobLoader: BaseLoader {

    override fun loadNative(
        ctx: Context,
        adPos: AdPos,
        adConf: AdConf,
        adsListener: AdsListener,
        callBack: (ad: BaseAd?) -> Unit
    ) {
        val admobNative = AdmobNative(adPos, adConf)
        //失败回调是必须的
        admobNative.setLoadErrAction {
            callBack.invoke(null)
        }
        AdLoader.Builder(ctx, adConf.id)
            .forNativeAd {
                admobNative.loaded(it, adsListener)
                callBack.invoke(admobNative)
            }
            .withAdListener(admobNative.listener)
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build())
            .build()
            .loadAd(AdRequest.Builder().build())
    }

    override fun loadOpen(
        ctx: Context,
        adPos: AdPos,
        adConf: AdConf,
        adsListener: AdsListener,
        callBack: (ad: BaseAd?) -> Unit
    ) {
        val adRequest = AdRequest.Builder().build()
        AppOpenAd.load(
            ctx.applicationContext,
            adConf.id,
            adRequest,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(p0: AppOpenAd) {
                    val openAd = AdmobOpen(adPos, adConf)
                    openAd.loaded(p0, adsListener)
                    callBack.invoke(openAd)
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    callBack.invoke(null)
                }
            })
    }

    override fun loadInterstitial(
        ctx: Context,
        adPos: AdPos,
        adConf: AdConf,
        adsListener: AdsListener,
        callBack: (ad: BaseAd?) -> Unit
    ) {
        InterstitialAd.load(
            ctx,
            adConf.id,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    callBack.invoke(null)
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    val admobInterstitial = AdmobInterstitial(adPos, adConf)
                    admobInterstitial.loaded(interstitialAd, adsListener)
                    callBack.invoke(admobInterstitial)
                }
            })
    }
}
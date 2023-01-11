package com.daily.clean.booster.ads.loader

import android.content.Context
import com.daily.clean.booster.ads.conf.AdConf
import com.daily.clean.booster.ads.conf.AdPos
import com.daily.clean.booster.ads.AdsListener
import com.daily.clean.booster.ads.model.BaseAd

interface BaseLoader {

    fun loadNative(ctx: Context, adPos: AdPos, adConf: AdConf, adsListener: AdsListener, callBack: (ad: BaseAd?) -> Unit)
    fun loadOpen(ctx: Context, adPos: AdPos, adConf: AdConf, adsListener: AdsListener, callBack: (ad: BaseAd?) -> Unit)
    fun loadInterstitial(ctx: Context, adPos: AdPos, adConf: AdConf, adsListener: AdsListener, callBack: (ad: BaseAd?) -> Unit)

}
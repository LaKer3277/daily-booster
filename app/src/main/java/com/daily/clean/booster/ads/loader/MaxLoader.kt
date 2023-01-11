package com.daily.clean.booster.ads.loader

import android.content.Context
import com.daily.clean.booster.ads.conf.AdConf
import com.daily.clean.booster.ads.conf.AdPos
import com.daily.clean.booster.ads.AdsListener
import com.daily.clean.booster.ads.model.BaseAd

class MaxLoader: BaseLoader {

    override fun loadNative(
        ctx: Context,
        adPos: AdPos,
        adConf: AdConf,
        adsListener: AdsListener,
        callBack: (ad: BaseAd?) -> Unit
    ) {

    }

    override fun loadOpen(
        ctx: Context,
        adPos: AdPos,
        adConf: AdConf,
        adsListener: AdsListener,
        callBack: (ad: BaseAd?) -> Unit
    ) {

    }

    override fun loadInterstitial(
        ctx: Context,
        adPos: AdPos,
        adConf: AdConf,
        adsListener: AdsListener,
        callBack: (ad: BaseAd?) -> Unit
    ) {

    }
}
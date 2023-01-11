package com.daily.clean.booster.ads.model

import androidx.annotation.CallSuper
import com.daily.clean.booster.ads.conf.AdConf
import com.daily.clean.booster.ads.conf.AdPos
import com.daily.clean.booster.ads.AdsListener

open class BaseAd(private val adPos: AdPos, val adConf: AdConf) {

    private val loadedTms = System.currentTimeMillis()

    @CallSuper
    open fun loaded(ad: Any, adsListener: AdsListener) {
        redefineListener(adsListener)
    }

    fun isExpired(): Boolean {
        return (System.currentTimeMillis() - loadedTms) < 60 * 60 * 1000L
    }

    fun redefineListener(adsListener: AdsListener) {
        unitAdShown = { adsListener.onShown() }
        unitAdClick = { adsListener.onClick() }
        unitAdClose = { adsListener.onDismiss() }
    }

    var unitAdShown: () -> Unit = {}
    var unitAdClick: () -> Unit = {}
    var unitAdClose: () -> Unit = {}

    open fun onDestroy() { }

}
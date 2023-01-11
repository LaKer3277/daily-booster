package com.daily.clean.booster.ads.model

import android.app.Activity
import android.view.ViewGroup
import com.daily.clean.booster.ads.conf.AdConf
import com.daily.clean.booster.ads.conf.AdPos

abstract class BaseNav(private val adPos: AdPos, adCong: AdConf): BaseAd(adPos, adCong) {

    abstract fun show(activity: Activity, rootLayout: ViewGroup, inflateLayout: Int): Boolean

}
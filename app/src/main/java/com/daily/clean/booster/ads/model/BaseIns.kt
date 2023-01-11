package com.daily.clean.booster.ads.model

import android.app.Activity
import com.daily.clean.booster.ads.conf.AdConf
import com.daily.clean.booster.ads.conf.AdPos

abstract class BaseIns(private val adPos: AdPos, private val adCong: AdConf): BaseAd(adPos, adCong) {

    abstract fun show(activity: Activity): Boolean

}
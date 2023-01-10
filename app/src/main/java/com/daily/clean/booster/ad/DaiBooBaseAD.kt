package com.daily.clean.booster.ad

import androidx.appcompat.app.AppCompatActivity
import com.daily.clean.booster.entity.DaiBooAdEvent
import com.daily.clean.booster.entity.DaiBooAdItemBean
import com.daily.clean.booster.utils.LogDB
import java.util.*

abstract class DaiBooBaseAD(var adItem: DaiBooAdItemBean) {

    var loadTime: Long = 0
    var isClicked: Boolean = false
    var adEvent: DaiBooAdEvent? = null
    abstract fun show(activity: AppCompatActivity, adShowCallBack: ClnoptADCallBack?)
    abstract fun load(success: (DaiBooBaseAD) -> Unit, failed: () -> Unit)
    abstract fun isAdAvailable(): Boolean
    open fun destroy() {}

    fun wasTimeIn(cacheTime: Int): Boolean {
        if(cacheTime == 0) return true
        val dateDifference: Long = (Date()).time - loadTime
        LogDB.d("#ADS---dateDifference = ${dateDifference/1000}")
        val per: Long = 1000
        return (dateDifference < (per * cacheTime))
    }
    
}

interface ClnoptADCallBack {
    fun onAdShowed(result: Boolean)
    fun onAdDismiss(baseAd: DaiBooBaseAD?)
    fun onAdClicked(baseAd: DaiBooBaseAD?)
}
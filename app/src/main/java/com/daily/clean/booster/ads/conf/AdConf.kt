package com.daily.clean.booster.ads.conf

data class AdConf(val platform: String,      //平台 Admob 和 Max
                  val priority: Int,         //优先级
                  val type: String,          //类型
                  val id: String) {

    fun getFormat() = when (type) {
        "interstitial" -> "interstitial"
        "inter" -> "interstitial"
        "open" -> "open"
        "native" -> "native"
        else -> type
    }

}
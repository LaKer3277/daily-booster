package com.daily.clean.booster.ads.conf

data class AdConf(val Network: String,      //平台 Admob 和 Max
                  val Hierarchy: Int,       //优先级
                  val Form: String,         //类型
                  val Id: String)
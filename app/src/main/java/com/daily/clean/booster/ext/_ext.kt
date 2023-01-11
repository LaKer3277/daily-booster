package com.daily.clean.booster.ext

import android.util.Log
import com.daily.clean.booster.ads.conf.AdPos

fun String.toAdPos(): AdPos {
    return when(this) {
        AdPos.Open.adPos -> AdPos.Open
        AdPos.NavMain.adPos -> AdPos.NavMain
        AdPos.NavResult.adPos -> AdPos.NavResult
        AdPos.InsClean.adPos -> AdPos.InsClean
        else -> AdPos.None
    }
}

fun loggerApp(msg: String) {
    Log.i("loggerApp", msg)
}
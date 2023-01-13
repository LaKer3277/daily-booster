package com.daily.clean.booster.ext

import android.util.Log
import com.daily.clean.booster.ads.conf.AdPos
import com.daily.clean.booster.entity.DaiBooPopBean
import com.google.gson.Gson
import java.text.DecimalFormat
import java.util.*

fun String.toAdPos(): AdPos {
    return when(this) {
        AdPos.Open.adPos -> AdPos.Open
        AdPos.NavMain.adPos -> AdPos.NavMain
        AdPos.NavResult.adPos -> AdPos.NavResult
        AdPos.InsClean.adPos -> AdPos.InsClean
        else -> AdPos.None
    }
}

fun String.json2Pop(): DaiBooPopBean? {
    if (this.isNullOrEmpty()) return null
    return try {
        Gson().fromJson(this, DaiBooPopBean::class.java)
    } catch (e: Exception) {
        null
    }
}


fun convertSize(length: Long): String {
    val format = DecimalFormat("#.##")
    val GiB = 1024 * 1024 * 1024.0f
    val MiB = 1024 * 1024.0f
    val KiB = 1024.0f

    if (length > GiB) {
        return format.format(length / GiB) + " GB"
    }
    if (length > MiB) {
        return format.format(length / MiB) + " MB"
    }
    return if (length > KiB) {
        format.format(length / KiB) + " KB"
    } else format.format(length) + " B"
}

fun currentTms(): Long {
    return System.nanoTime() / 1000_000L
}

fun currentTs(): Long {
    return System.nanoTime() / 1000_000_000L
}

fun intervalDays(date1: Long, date2: Long): Int {
    val calender1 = Calendar.getInstance()
    val calender2 = Calendar.getInstance()
    calender1.time = Date(date1)
    calender2.time = Date(date2)

    var day1 = calender1.get(Calendar.DAY_OF_YEAR)
    var day2 = calender2.get(Calendar.DAY_OF_YEAR)
    var year1 = calender1.get(Calendar.YEAR)
    var year2 = calender2.get(Calendar.YEAR)
    if (year1 > year2) {
        val tempDay = day1
        val tempYear = year1
        day1 = day2
        day2 = tempDay

        year1 = year2
        year2 = tempYear
    }

    return if (year1 == year2) Math.abs(day1 - day2) else {
        var countDay = 0
        for (i in year1 until year2) {
            countDay += if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0) {
                366
            } else {
                365
            }
        }

        countDay + Math.abs(day1 - day2)
    }
}

fun loggerApp(msg: String) {
    Log.i("loggerApp", msg)
}

fun loggerNotify(msg: String) {
    Log.i("loggerNotify", msg)
}

fun loggerHttp(msg: String) {
    Log.i("loggerHttp", msg)
}

fun loggerAds(msg: String) {
    Log.i("AdsLoader", msg)
}

fun loggerEvent(msg: String) {
    Log.i("loggerEvent", msg)
}
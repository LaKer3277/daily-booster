package com.daily.clean.booster.core

import android.graphics.drawable.Drawable
import com.daily.clean.booster.R
import com.daily.clean.booster.entity.DaiBooCleanParentBean
import com.daily.clean.booster.utils.getString

object CleanData {

    var cache: MutableList<DaiBooCleanParentBean> = mutableListOf()
    var appList: MutableList<Drawable?> = mutableListOf()

    fun getAppSize(): Int =
        if (appList.size == 0) {
            30
        } else {
            appList.size
        }

    fun getRAMUseD(): Long {
        val item = cache.find { it.name == R.string.ram_used.getString() }
        return item?.getSelectedSize() ?: 0L
    }

    fun getAppCacheFileSize(): Long {
        val appCache = cache.find { it.name == R.string.app_cache.getString() }
        return appCache?.getSelectedSize() ?: 0L
    }
}
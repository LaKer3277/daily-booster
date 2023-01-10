package com.daily.clean.booster.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import com.daily.clean.booster.BuildConfig
import com.daily.clean.booster.App

object DaiBooRAMUtils {

    fun getUsedMemory(): Long {
        val activityManager =
            App.ins.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.totalMem - memoryInfo.availMem
    }


    fun getUsedMemoryString(context: Context?=null): String {
        val am = App.ins.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memoryInfo)
        val result = memoryInfo.totalMem - memoryInfo.availMem
        return FileSizeUtil.formatFileSize(result)
    }


    fun getFreeRAMtring(context: Context?=null): String {
        val am = App.ins.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memoryInfo)
        val result = memoryInfo.availMem
        return FileSizeUtil.formatFileSize(result)
    }


    fun getTotalMemoryString(context: Context?=null): String {
        val am = App.ins.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memoryInfo)
        val result = memoryInfo.totalMem
        val rGB = FileSizeUtil.formatFileSize(result)
        return  rGB
    }

    fun getUsedMemoryStringPerInt(): Int {
        val activityManager =
            App.ins.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val used = memoryInfo.totalMem - memoryInfo.availMem
        val per = (used.toFloat() / memoryInfo.totalMem) * 100
        return per.toInt()
    }

    fun getUsedMemoryStringPer(): String {
        val activityManager =
            App.ins.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val used = memoryInfo.totalMem - memoryInfo.availMem
        val per = (used.toFloat() / memoryInfo.totalMem) * 100
        return "${per.toInt()}%"
    }


    fun getInstalledAppAmounts(callback: (Int) -> Unit) {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        callback.invoke(App.ins.packageManager.queryIntentActivities(intent, 0).size)
    }
    fun getInstalledAppAmounts2():Int {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
       return App.ins.packageManager.queryIntentActivities(intent, 0).size
    }

    private fun getInstalledPackageNameList(): List<String> {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        return App.ins.packageManager.queryIntentActivities(intent, 0)
            .map { it?.activityInfo?.packageName ?: "" }
            .filter { TextUtils.isEmpty(it).not() && it != BuildConfig.APPLICATION_ID }
    }

    fun clearRAM() {
        val mActivityManager =
            App.ins.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val packageList = getInstalledPackageNameList()
        for (packageName in packageList) {
            mActivityManager.killBackgroundProcesses(packageName)
        }
    }

}
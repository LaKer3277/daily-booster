package com.daily.clean.booster.pop

import com.daily.clean.booster.App
import com.daily.clean.booster.appIns
import com.daily.clean.booster.base.FiBLogEvent
import com.daily.clean.booster.base.FiBRemoteUtil
import com.daily.clean.booster.entity.DaiBooPopItemBean
import com.daily.clean.booster.ext.loggerNotify
import com.daily.clean.booster.utils.DaiBooMK
import com.daily.clean.booster.utils.LogDB


object NotifyManager {

    fun getPopItem(tanId: String): DaiBooPopItemBean? {
        FiBRemoteUtil.daiBooPopBean?.let {
            return when (tanId) {
                NotySourceTime -> it.booster_time
                NotySourceUnlock -> it.booster_unl
                NotySourceUninstall -> it.booster_uni
                NotySourceCharge -> it.booster_cha
                NotySourceBattery -> it.booster_bat
                else -> null
            }
        }
        return null
    }

    fun tryPop(workId: String, sourceId: String): Boolean {
        if (!isCanPopConfig(sourceId, getPopItem(sourceId))) {
            return false
        }
        FiBLogEvent.pop_log(sourceId, 0)
        NotifyPopper.createNotificationAndPop(App.ins, workId, sourceId)
        saveLastPopTime(sourceId)
        return true
    }

    private var index = 0
    fun tryPopRandom(source: String) {
        when (++index) {
            1 -> tryPop(NotyWorkBooster, source)
            2 -> tryPop(NotyWorkCpu, source)
            3 -> tryPop(NotyWorkBattery, source)
            4 -> {
                tryPop(NotyWorkClean, source)
                index = 0
            }
        }
    }

    private fun isCanPopConfig(tanId: String, item: DaiBooPopItemBean?): Boolean {
        if (appIns.isAtForeground()) return false

        if (item == null) return false
        //是否开启体外弹窗
        if (1 != FiBRemoteUtil.daiBooPopBean?.up_pop) {
            loggerNotify("allConfig Not Enable")
            return false
        }
        //是否超过弹窗时间
        val overFirstTime = System.currentTimeMillis() - getFirstInstallTime() >= item.first * 60 * 60 * 1000L
        if (!overFirstTime) {
            loggerNotify("FirstTime Not Reached")
            return false
        }

        //是否在弹窗时间间隔之内
        if (!isOverInTime(item.int, tanId)) {
            loggerNotify("IntervalTime Not reached")
            return false
        }
        //是否超过弹出次数的限制
        if (DaiBooMK.canPopTanIsOverMax(tanId, item.up).not()) {
            loggerNotify("PopTimes reached Max")
            return false
        }
        return true
    }

    fun saveLastPopTime(tanId: String) {
        DaiBooMK.saveTanShowCounts(tanId)
    }

    private fun isOverInTime(inTime: Int, key: String): Boolean {
        if (0 == inTime) return true
        val lastPopTime = DaiBooMK.getTanLastTime(key)
        val dffTime = System.currentTimeMillis() - lastPopTime
        LogDB.dpop("check ---> $key is Not overt last time >>> dffTime = $dffTime ? ${inTime * 60 * 1000}")
        return dffTime >= inTime * 60 * 1000
    }

    private fun getFirstInstallTime(): Long {
        return try {
            App.ins.applicationContext.packageManager.getPackageInfo(
                App.ins.packageName,
                0
            ).firstInstallTime
        } catch (e: Exception) {
            0L
        }
    }


}
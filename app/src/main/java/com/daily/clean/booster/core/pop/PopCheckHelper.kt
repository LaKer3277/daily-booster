package com.daily.clean.booster.core.pop

import com.blankj.utilcode.util.NetworkUtils
import com.daily.clean.booster.App
import com.daily.clean.booster.base.DBConfig
import com.daily.clean.booster.base.FiBLogEvent
import com.daily.clean.booster.base.FiBRemoteUtil
import com.daily.clean.booster.entity.DaiBooPopItemBean
import com.daily.clean.booster.utils.DaiBooMK
import com.daily.clean.booster.utils.LogDB


object PopCheckHelper {


    fun getPopItem(tanId: String): DaiBooPopItemBean? {
        FiBRemoteUtil.daiBooPopBean?.let {
            return when (tanId) {
                DBConfig.DAIBOO_NOTY_TIME -> it.booster_time
                DBConfig.DAIBOO_NOTY_UNLOCK -> it.booster_unl
                DBConfig.DAIBOO_NOTY_UNINSTALL -> it.booster_uni
                DBConfig.DAIBOO_NOTY_CHARGE -> it.booster_cha
                DBConfig.DAIBOO_NOTY_BATTERY -> it.booster_bat
                else -> null
            }
        }
        return null
    }


    private fun isCanPopConfig(tanId: String, item: DaiBooPopItemBean?): Boolean {

        if (DBConfig.DAIBOO_POP_IS_CHECK_CONFIG.not()) return true

        LogDB.dpop("check $tanId ---> $item")
        if (item == null) return false
        //是否开启体外弹窗
        if (isAllowPopTan().not()) {
            LogDB.dpop("pop cancle $tanId---> isAllowPop = false  配置不允许")
            return false
        }
        //是否超过弹窗时间
        if (ifOverFirstInstallTime(item.first).not()) {
            LogDB.dpop("pop cancle $tanId--->first time 没有超过第一次启动时间 ")
            return false
        }

        //是否在弹窗时间间隔之内
        if (isOverInTime(item.int, tanId).not()) {
            LogDB.dpop("pop cancle $tanId--->over interval_time 还没有超过间隔时间")
            return false
        }
        //是否超过弹出次数的限制
//        LogDB.dAlert("check --->is over times>>>$mKey ")
        if (DaiBooMK.canPopTanIsOverMax(tanId, item.up).not()) {
            LogDB.dpop("pop cancle $tanId--->out if times")
            return false
        }
        return true
    }


    fun saveLastPopTime(tanId: String) {
        DaiBooMK.saveTanShowCounts(tanId)
    }


    private fun isAllowPopTan(): Boolean {
//        LogDB.dAlert("check --->is allow>>>")
        return 1 == FiBRemoteUtil.daiBooPopBean?.up_pop
    }

    private fun isOverInTime(inTime: Int, key: String): Boolean {
        if (0 == inTime) return true
        val lastPopTime = DaiBooMK.getTanLastTime(key)
        val dffTime = System.currentTimeMillis() - lastPopTime
        LogDB.dpop("check ---> $key is Not overt last time >>> dffTime = $dffTime ? ${inTime * 60 * 1000}")
        return dffTime >= inTime * 60 * 1000
    }

    private fun ifOverFirstInstallTime(firstTime: Int): Boolean {
//        LogDB.dAlert("check --->is over first install time>>>")
        return System.currentTimeMillis() - getFirstInstallTime() >= firstTime * 60 * 60 * 1000L
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


    fun checkStaus(workId: String, tanId: String): Boolean {

        if (DBConfig.DAIBOO_POP_IS_CHECK_CONFIG && tanId == DBConfig.DAIBOO_NOTY_TIME && (System.currentTimeMillis() - App.timeOnAppStop < DBConfig.DOCMAN_POP_IN_BG_STAY_TIME)) {
            LogDB.dpop("${tanId} canNotShow --- Background stay time not enough ")
            return false
        }

        if (FiBRemoteUtil.isPop_refer().not()) {
            LogDB.dpop("${tanId} canNotShow --- referrer config ")
            return false
        }

        //判断是否有网络
        if (NetworkUtils.isConnected().not()) {
            LogDB.dpop("${tanId} canNotShow --- Network Disconnect")
            return false
        }

        //判断是否锁屏
//        val pm: PowerManager = DBApp.ins.getSystemService(Context.POWER_SERVICE) as PowerManager
//        var isScroonOn = pm.isInteractive
//        if (isScroonOn.not()) {
//            LogDB.dpop("${tanId} isCanNotShow isScroonOn = ${isScroonOn}")
//            return false
//        }
        //判断栈顶，是否在前台
        val isAtTop = App.ins.isAtForeground()
        if (isAtTop) {
            LogDB.dpop("${tanId} canNotShow  --- isAtTop")
            return false
        }
        return true
    }


    fun tryPop(workId: String, tanId: String): Boolean {

        if (checkStaus(workId, tanId).not()) return false
        //后台配置判断
        val isConfigCanShow = isCanPopConfig(tanId, getPopItem(tanId))
        if (isConfigCanShow.not()) {
            LogDB.dpop("${tanId} canNotShow  --- Config Can not Show")
            return false
        }
        FiBLogEvent.pop_log(tanId, 0)
        DaiBooNotifyPop.createNotificationAndPop(App.ins, workId, tanId)
        return true
    }


}
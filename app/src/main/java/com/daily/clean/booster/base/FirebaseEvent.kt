package com.daily.clean.booster.base

import android.os.Bundle
import com.blankj.utilcode.util.TimeUtils
import com.daily.clean.booster.appIns
import com.daily.clean.booster.entity.DaiBooLogEvent
import com.daily.clean.booster.tba.HttpTBA
import com.daily.clean.booster.utils.DaiBooMK
import com.google.firebase.analytics.FirebaseAnalytics
import com.daily.clean.booster.pop.*
import com.daily.clean.booster.utils.MyTimeUtil

object FirebaseEvent {

    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        val firebase = FirebaseAnalytics.getInstance(appIns)
        userRetention()
        firebase
    }

    /**
     * 用户属性
     * 用户使用天数，第一天启动day0,第二天打开day1,依次类推
     */
    private const val KEY_USER_RETENTION = "${DB_NAME}_user_rentention"

    private fun userRetention() {
        val lastReportTime = DaiBooMK.decode(KEY_USER_RETENTION, 0L)
        if (!TimeUtils.isToday(lastReportTime)) {
            val days = MyTimeUtil.calculateDays(System.currentTimeMillis(), lastReportTime)
            firebaseAnalytics.setUserProperty("user_rent", "day$days")
            DaiBooMK.encode(KEY_USER_RETENTION, System.currentTimeMillis())

            //属性
            /*HttpTBA.doReportLog(logEvent = DaiBooLogEvent("biscuit", Bundle().apply {
                putString("user_rent", "day$times")
            }))*/
        }
    }

    fun logEvent(key: String, params: Bundle? = null) {
        if (DB_USE_FB) {
            firebaseAnalytics.logEvent(key, params)
        }

        HttpTBA.doReportLog(logEvent = DaiBooLogEvent(key, params))
    }

    fun pageScanShow(workId: String) {
        val value = when (workId) {
            NotyWorkBooster -> "boost"
            NotyWorkCpu -> "cpu"
            NotyWorkBattery -> "battery"
            else -> "clean"
        }
        logEvent("page_scan_show", Bundle().apply { putString("process", value) })
    }

    fun pageCleanShow(workId: String) {
        val value = when (workId) {
            NotyWorkBooster -> "boost"
            NotyWorkCpu -> "cpu"
            NotyWorkBattery -> "battery"
            else -> "clean"
        }
        logEvent("page_clean_show", Bundle().apply { putString("process", value) })
    }

    fun pageResultShow(workId: String) {
        val value = when (workId) {
            NotyWorkBooster -> "boost"
            NotyWorkCpu -> "cpu"
            NotyWorkBattery -> "battery"
            else -> "clean"
        }
        logEvent("page_result_show", Bundle().apply { putString("process", value) })
    }

    fun pageReturnClick(workId: String) {
        val value = when (workId) {
            NotyWorkBooster -> "boost"
            NotyWorkCpu -> "cpu"
            NotyWorkBattery -> "battery"
            else -> "clean"
        }
        logEvent("page_return_click", Bundle().apply { putString("process", value) })
    }

    fun notyActivityShow(popID: String) {
        val value = when (popID) {
            NotySourceTime -> "time"
            NotySourceUnlock -> "unl"
            NotySourceUninstall -> "uni"
            NotySourceCharge -> "cha"
            NotySourceBattery -> "bat"
            else -> "time"
        }
        logEvent("up_ac_show", Bundle().apply { putString("show", value) })
    }

    fun notyActivityClick(popID: String) {
        val value = when (popID) {
            NotySourceTime -> "time"
            NotySourceUnlock -> "unl"
            NotySourceUninstall -> "uni"
            NotySourceCharge -> "cha"
            NotySourceBattery -> "bat"
            else -> "time"
        }
        logEvent("up_ac_click", Bundle().apply { putString("click", value) })
    }

    fun notifyResidentClick(workId: String) {
        val value = when (workId) {
            NotyWorkBooster -> "boost"
            NotyWorkCpu -> "cpu"
            NotyWorkBattery -> "battery"
            else -> "clean"
        }
        logEvent("notifi_click", Bundle().apply { putString("click", value) })
    }


    fun popLog(sourceId: String, type: Int) {
        when (type) {
            0 -> logEvent("up_all_invoke")
            2 -> logEvent("up_all_click")
        }

        when (sourceId) {
            NotySourceBattery -> when (type) {
                0 -> logEvent("up_battery_invoke")
                2 -> logEvent("up_battery_click")
            }
            NotySourceUninstall -> when (type) {
                0 -> logEvent("up_unins_invoke")
                2 -> logEvent("up_unins_click")
            }
            NotySourceCharge -> when (type) {
                0 -> logEvent("up_charge_invoke")
                2 -> logEvent("up_charge_click")
            }
            NotySourceTime -> when (type) {
                0 -> logEvent("up_time_invoke")
                2 -> logEvent("up_time_click")
            }
            NotySourceUnlock -> when (type) {
                0 -> logEvent("up_unlock_invoke")
                2 -> logEvent("up_unlock_click")
            }
        }
    }


    /**
     * 一轮waterfall广告请求开始发出时记录，附带两个事件参数：
    1. Type（广告类型），参数值：open, interstitial, native
    2. LoadTime（一轮waterfall广告请求发出-加载完成的时间，单位秒），参数值：1,2,3,...
    3. Result（该轮请求是否得到填充），参数值：success, fail
     */
    fun ad_request(tag: String, time: Long, result: Boolean) {
        var bundle = Bundle()
        bundle.putString("ad_pos_id", tag)
        bundle.putLong("LoadTime", time)
        bundle.putString("Result", if (result) "success" else "fail")
        logEvent("ad_request", bundle)
    }

    fun ad_chance(tag: String) {
        val bundle = Bundle()
        bundle.putString("ad_pos_id", tag)
        logEvent("db_ad_chance", bundle)
    }

    fun dm_ad_impression(tag: String) {
        val bundle = Bundle()
        bundle.putString("ad_pos_id", tag)
        logEvent("db_ad_impression", bundle)
    }

    fun ad_click(tag: String) {
        val bundle = Bundle()
        bundle.putString("ad_pos_id", tag)
        logEvent("ad_click", bundle)
    }
}
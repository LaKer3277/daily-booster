package com.daily.clean.booster.base

import android.os.Bundle
import com.blankj.utilcode.util.TimeUtils
import com.daily.clean.booster.ads.conf.AdPos
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

    fun adChance(adPos: AdPos) {
        val bundle = Bundle()
        bundle.putString("ad_pos_id", adPos.adPos)
        logEvent("db_ad_chance", bundle)
    }

    fun adImpression(adPos: AdPos) {
        val bundle = Bundle()
        bundle.putString("ad_pos_id", adPos.adPos)
        logEvent("db_ad_impression", bundle)
    }
}
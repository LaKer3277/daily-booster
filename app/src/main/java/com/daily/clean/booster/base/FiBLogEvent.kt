package com.daily.clean.booster.base


import android.os.Bundle
import com.blankj.utilcode.util.TimeUtils
import com.daily.clean.booster.DBApp
import com.daily.clean.booster.entity.DaiBooLogEvent
import com.daily.clean.booster.tba.HttpTBA
import com.daily.clean.booster.utils.DaiBooMK
import com.daily.clean.booster.utils.LogDB
import com.google.firebase.analytics.FirebaseAnalytics
import java.util.*

object FiBLogEvent {

    val firebaseAnalytics: FirebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(DBApp.ins)
    }

    fun logEvent(key: String) {
        logEvent(key, null)
    }

    fun logEvent(key: String, params: Bundle?) {
        LogDB.dEvent("LOGDOT---$key <<< $params")
        if (DBConfig.DAIBOO_USE_FB) {
            firebaseAnalytics.logEvent(key, params)
        }

        HttpTBA.doReport(HttpTBA.EVENT_LOG, logEvent = DaiBooLogEvent(key, params))

    }


    //100--->>>
    fun clean_page_to_result_start(workId: String) {
        val value = when (workId) {
            DBConfig.DAIBOO_WORK_ID_BOOSTER -> "boost"
            DBConfig.DAIBOO_WORK_ID_CPU -> "cpu"
            DBConfig.DAIBOO_WORK_ID_BATTERY -> "battery"
            else -> "clean"
        }
        logEvent("clean_ad_to_result_start", Bundle().apply { putString("process", value) })
    }

    fun clean_page_to_result_end(workId: String) {
        val value = when (workId) {
            DBConfig.DAIBOO_WORK_ID_BOOSTER -> "boost"
            DBConfig.DAIBOO_WORK_ID_CPU -> "cpu"
            DBConfig.DAIBOO_WORK_ID_BATTERY -> "battery"
            else -> "clean"
        }
        logEvent("clean_ad_to_result_end", Bundle().apply { putString("process", value) })
    }

    fun session_sttt() = logEvent("session_sttt")
    fun app_active() = logEvent("app_active")
    fun start_page() = logEvent("start_page")
    fun start_first_() = logEvent("start_first_")
    fun start_first_clean() = logEvent("start_first_clean")
    fun start_first_result() = logEvent("start_first_result")
    fun start_first_return() = logEvent("start_first_return")
    fun per_show() = logEvent("per_show")
    fun per_agree() = logEvent("per_agree")
    fun per_reject() = logEvent("per_reject")
    fun page_home_show() = logEvent("page_home_show")
    fun page_home_clean() = logEvent("page_home_clean")
    fun page_home_clean2() = logEvent("page_home_clean2")
    fun page_home_boost() = logEvent("page_home_boost")
    fun page_home_cpu() = logEvent("page_home_cpu")
    fun page_home_battery() = logEvent("page_home_battery")
    fun page_scan_show(workId: String) {
        val value = when (workId) {
            DBConfig.DAIBOO_WORK_ID_BOOSTER -> "boost"
            DBConfig.DAIBOO_WORK_ID_CPU -> "cpu"
            DBConfig.DAIBOO_WORK_ID_BATTERY -> "battery"
            else -> "clean"
        }
        logEvent("page_scan_show", Bundle().apply { putString("process", value) })
    }

    fun page_clean_show(workId: String) {

        val value = when (workId) {
            DBConfig.DAIBOO_WORK_ID_BOOSTER -> "boost"
            DBConfig.DAIBOO_WORK_ID_CPU -> "cpu"
            DBConfig.DAIBOO_WORK_ID_BATTERY -> "battery"
            else -> "clean"
        }
        logEvent("page_clean_show", Bundle().apply { putString("process", value) })
    }

    fun page_result_show(workId: String) {

        val value = when (workId) {
            DBConfig.DAIBOO_WORK_ID_BOOSTER -> "boost"
            DBConfig.DAIBOO_WORK_ID_CPU -> "cpu"
            DBConfig.DAIBOO_WORK_ID_BATTERY -> "battery"
            else -> "clean"
        }
        logEvent("page_result_show", Bundle().apply { putString("process", value) })
    }

    fun page_return_click(workId: String) {

        val value = when (workId) {
            DBConfig.DAIBOO_WORK_ID_BOOSTER -> "boost"
            DBConfig.DAIBOO_WORK_ID_CPU -> "cpu"
            DBConfig.DAIBOO_WORK_ID_BATTERY -> "battery"
            else -> "clean"
        }
        logEvent("page_return_click", Bundle().apply { putString("process", value) })

    }

    fun up_time_invoke() = logEvent("up_time_invoke")
    fun up_time_click() = logEvent("up_time_click")
    fun up_unlock_invoke() = logEvent("up_unlock_invoke")
    fun up_unlock_click() = logEvent("up_unlock_click")
    fun up_unins_invoke() = logEvent("up_unins_invoke")
    fun up_unins_click() = logEvent("up_unins_click")
    fun up_charge_invoke() = logEvent("up_charge_invoke")
    fun up_charge_click() = logEvent("up_charge_click")
    fun up_battery_invoke() = logEvent("up_battery_invoke")
    fun up_battery_click() = logEvent("up_battery_click")

    fun up_ac_show(popID: String) {

        val value = when (popID) {
            DBConfig.DAIBOO_NOTY_TIME -> "time"
            DBConfig.DAIBOO_NOTY_UNLOCK -> "unl"
            DBConfig.DAIBOO_NOTY_UNINSTALL -> "uni"
            DBConfig.DAIBOO_NOTY_CHARGE -> "cha"
            DBConfig.DAIBOO_NOTY_BATTERY -> "bat"
            else -> "time"
        }
        logEvent("up_ac_show", Bundle().apply { putString("show", value) })
    }

    fun up_ac_click(popID: String) {

        val value = when (popID) {
            DBConfig.DAIBOO_NOTY_TIME -> "time"
            DBConfig.DAIBOO_NOTY_UNLOCK -> "unl"
            DBConfig.DAIBOO_NOTY_UNINSTALL -> "uni"
            DBConfig.DAIBOO_NOTY_CHARGE -> "cha"
            DBConfig.DAIBOO_NOTY_BATTERY -> "bat"
            else -> "time"
        }
        logEvent("up_ac_click", Bundle().apply { putString("click", value) })
    }

    fun up_all_invoke() = logEvent("up_all_invoke")
    fun up_all_click() = logEvent("up_all_click")
    fun up_all_start() = logEvent("up_all_start")
    fun up_all_page() = logEvent("up_all_page")
    fun up_all_relust() = logEvent("up_all_relust")
    fun notifi_click(workId: String) {

        val value = when (workId) {
            DBConfig.DAIBOO_WORK_ID_BOOSTER -> "boost"
            DBConfig.DAIBOO_WORK_ID_CPU -> "cpu"
            DBConfig.DAIBOO_WORK_ID_BATTERY -> "battery"
            else -> "clean"
        }
        logEvent("notifi_click", Bundle().apply { putString("click", value) })

    }


    fun pop_log(tanId: String, type: Int) {

        when (type) {
            0 -> up_all_invoke() // 尝试
            2 -> up_all_click() // 点击
        }

        when (tanId) {
            DBConfig.DAIBOO_NOTY_BATTERY -> when (type) {
                0 -> up_battery_invoke()
                2 -> up_battery_click()
            }
            DBConfig.DAIBOO_NOTY_UNINSTALL -> when (type) {
                0 -> up_unins_invoke()
                2 -> up_unins_click()
            }
            DBConfig.DAIBOO_NOTY_CHARGE -> when (type) {
                0 -> up_charge_invoke()
                2 -> up_charge_click()
            }
            DBConfig.DAIBOO_NOTY_TIME -> when (type) {
                0 -> up_time_invoke()
                2 -> up_time_click()
            }
            DBConfig.DAIBOO_NOTY_UNLOCK -> when (type) {
                0 -> up_unlock_invoke()
                2 -> up_unlock_click()
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


//100---<<<<


    /**
     * 用户属性
     * 用户使用天数，第一天启动day0,第二天打开day1,依次类推
     */
    const val KEY_LOGIN_TIMES = "${DBConfig.DAIBOO_SPNAME}_LOGIN_TIMES"
    const val KEY_USER_RENTENTION = "${DBConfig.DAIBOO_SPNAME}_user_rentention"

    fun user_rent() {
        var times = DaiBooMK.decode(KEY_LOGIN_TIMES, 0)
        val lastReportTime = DaiBooMK.decode(KEY_USER_RENTENTION, 0L)
        LogDB.dEvent("LOGDOT---user_retention value=day$times")
        if (TimeUtils.isToday(lastReportTime).not()) {
            if (DBConfig.DAIBOO_USE_FB)
                firebaseAnalytics.setUserProperty("user_rent", "day$times")
            times++
            DaiBooMK.encode(KEY_LOGIN_TIMES, times)
            DaiBooMK.encode(KEY_USER_RENTENTION, Date().time)

            //属性
            HttpTBA.doReport(HttpTBA.EVENT_LOG, logEvent = DaiBooLogEvent("biscuit", Bundle().apply {
                putString("user_rent", "day$times")
            }))
            LogDB.dEvent("LOGDOT---保存数据")
        } else {
            LogDB.dEvent("LOGDOT---今日已经LOGDOT")
        }
    }


//    fun ad_impression_revenue(va: Double,precisionType:String,adNetwork:String) {
//        val bundle = Bundle().apply {
//            putDouble(FirebaseAnalytics.Param.VALUE, va)
//            putString(FirebaseAnalytics.Param.CURRENCY, "USD")
//            putString("precisionType", precisionType)
//            putString("adNetwork", adNetwork)
//        }
//        logEvent("ad_impression_revenue", params = bundle)
//    }
//
//    //上报 AdLTV 价值LOGDOT
//    fun log_eventAdLTV_OneDay(impre: Double) {
////        var currentImpre = impre / 1000000
//        var currentImpre = impre
//        val previousRevenue = DaiBooMK.decode<Double>("dm_adltv_ad_revenue", 0.0)
//        var cc: Double = previousRevenue + currentImpre
//        DaiBooMK.encode("dm_adltv_ad_revenue", cc)
//        LogDM.d("AdLTV---dm_adltv_ad_revenue:  =$previousRevenue + $currentImpre =$cc")
//
//        FireBRemoteUtil.adltvOneDayConBean?.let {
//            val list = mutableListOf<Double>(
//                it.adltv_top50,
//                it.adltv_top40,
//                it.adltv_top30,
//                it.adltv_top20,
//                it.adltv_top10
//            )
//
//            list.forEachIndexed { index, fl ->
//                if (previousRevenue < fl && cc >= fl) {
//                    val bundle = Bundle().apply {
//                        putDouble(FirebaseAnalytics.Param.VALUE, fl)
//                        putString(FirebaseAnalytics.Param.CURRENCY, "USD")
//                    }
//                    val nameKey = when (index) {
//                        0 -> "AdLTV_Top50Percent"
//                        1 -> "AdLTV_Top40Percent"
//                        2 -> "AdLTV_Top30Percent"
//                        3 -> "AdLTV_Top20Percent"
//                        4 -> "AdLTV_Top10Percent"
//                        else -> ""
//                    }
//                    logEvent(nameKey, bundle)
//                }
//            }
//        }
//
//        val totalValuePrevious = DaiBooMK.decode("dm_adltv_total_ad_revenue", 0.0)
//        val currentTotal = totalValuePrevious + currentImpre
//        if (currentTotal >= 0.01) {
//            logEvent("TotalAdRevenue001", Bundle().apply {
//                putDouble(FirebaseAnalytics.Param.VALUE, currentTotal)
//                putString(FirebaseAnalytics.Param.CURRENCY, "USD")
//            })
//            DaiBooMK.encode("dm_adltv_total_ad_revenue", 0f)
//        } else {
//            DaiBooMK.encode("dm_adltv_total_ad_revenue", currentTotal)
//        }
//
//
//    }
}
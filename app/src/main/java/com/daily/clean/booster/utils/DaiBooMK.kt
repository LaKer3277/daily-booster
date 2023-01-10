package com.daily.clean.booster.utils

import com.blankj.utilcode.util.TimeUtils
import com.daily.clean.booster.base.DBConfig
import com.daily.clean.booster.entity.DaiBooPopShowItem
import com.tencent.mmkv.MMKV
import java.util.*

object DaiBooMK {

    val mmkv: MMKV =
        MMKV.mmkvWithID(DBConfig.DAIBOO_SPNAME + "MMKV", MMKV.MULTI_PROCESS_MODE)

    private const val MK_IS_FIRST_START = "${DBConfig.DAIBOO_SPNAME}is_has_start"
    private const val MK_LAST_CLEAN_TIME = "${DBConfig.DAIBOO_SPNAME}last_Clean_time"
    private const val MK_CLEAN_JUNK_SIZE_APPCACHE = "${DBConfig.DAIBOO_SPNAME}clean_junk_appcache"
    private const val MK_LAST_BOOST_TIME = "${DBConfig.DAIBOO_SPNAME}last_boost_time"
    private const val MK_LAST_SCAN_TIME = "${DBConfig.DAIBOO_SPNAME}last_scan_time"
    private const val MK_LAST_TABLE_INT_TIME = "${DBConfig.DAIBOO_SPNAME}last_table_int_time"

    const val MK_REFERRER_URL = "${DBConfig.DAIBOO_SPNAME}mk_referrer_url"
    const val MK_REFERRER_INSTALL_VERSION = "${DBConfig.DAIBOO_SPNAME}mk_referrer_install_version"
    const val MK_REFERRER_INSTALL_TIME = "${DBConfig.DAIBOO_SPNAME}mk_referrer_install_time"
    const val MK_REFERRER_CLICK = "${DBConfig.DAIBOO_SPNAME}mk_referrer_click_timestamp_seconds"
    const val MK_REFERRER_CLICK_SERVER = "${DBConfig.DAIBOO_SPNAME}mk_referrer_click_timestamp_server_seconds"
    const val MK_REFERRER_INSTALL_SERVER = "${DBConfig.DAIBOO_SPNAME}mk_install_begin_timestamp_server_seconds"
    const val MK_REFERRER_GOOGLE_PLAY_INSTANT= "${DBConfig.DAIBOO_SPNAME}mk_google_play_instant"

    const val MK_LAST_SESSION_TIME = "${DBConfig.DAIBOO_SPNAME}mk_last_session_time"
    const val MK_IS_POSTED_DEV = "${DBConfig.DAIBOO_SPNAME}mk_is_posted_dev"
    const val MK_NOT_FIRST_IN_NMLIST = "${DBConfig.DAIBOO_SPNAME}mk_is_not_first_enter_nmlist"
    const val MK_NM_RESULT_TO_GIUDE = "${DBConfig.DAIBOO_SPNAME}mk_result_to_guide_nm"
    const val MK_NM_START_TO_GIUDE = "${DBConfig.DAIBOO_SPNAME}mk_start_to_guide_nm"
    const val MK_TBA_INSTALL_TIME = "${DBConfig.DAIBOO_SPNAME}mk_tab_install_time"

    const val MK_TBA_LIST_CACHE = "${DBConfig.DAIBOO_SPNAME}mk_tab_list_CACHE"


    fun addStartNMGuideTimes() {
        var ct = decode(MK_NM_START_TO_GIUDE, 0)
        ct++
        encode(MK_NM_START_TO_GIUDE, ct)
    }

    fun isNeedStart2Guide(): Boolean {
        return decode(MK_NM_START_TO_GIUDE, 0) < 3
    }

    fun addResultNMGuideTimes() {
        var ct = decode(MK_NM_RESULT_TO_GIUDE, 0)
        ct++
        encode(MK_NM_RESULT_TO_GIUDE, ct)
    }

    fun isNeedResult2Guide(): Boolean {
        return decode(MK_NM_RESULT_TO_GIUDE, 0) < 2
    }

    fun isFirstStart(): Boolean {
        return decode(MK_IS_FIRST_START, false).not()
    }

    fun saveFirstStart() {
        encode(MK_IS_FIRST_START, true)
    }

    private fun getLastCleanTime(): Long {
        return mmkv.decodeLong(MK_LAST_CLEAN_TIME)
    }

    fun isNeedClean(): Boolean {
        return Date().time - getLastCleanTime() > DBConfig.CLEAN_TIME_INTETNAL
    }

    fun reSetCleanTime() {
        mmkv.encode(MK_LAST_CLEAN_TIME, Date().time)
    }

    fun reSetBoostTime() {
        mmkv.encode(MK_LAST_BOOST_TIME, Date().time)
    }

    fun getLastBoostTime(): Long {
        return mmkv.decodeLong(MK_LAST_BOOST_TIME)
    }

    fun isNeedShowRAM(): Boolean {
        return Date().time - getLastBoostTime() > 5.ss
    }

    fun saveAppCacheSize(size: Long) {
        mmkv.encode(MK_CLEAN_JUNK_SIZE_APPCACHE, size)
    }

    fun getAppCacheJunkSize(): Long {
        return mmkv.decodeLong(MK_CLEAN_JUNK_SIZE_APPCACHE)
    }

    fun reSetScanTime() {
        mmkv.encode(MK_LAST_SCAN_TIME, Date().time)
    }

     fun getLastScanTime(): Long {
        return mmkv.decodeLong(MK_LAST_SCAN_TIME)
    }


    fun setAdShowTimes(v: Int) {
        mmkv.encode("ad_show_times", v)
    }


    fun <T> encode(key: String, value: T) {
        when (value) {
            is Long -> mmkv.encode(key, value)
            is Boolean -> mmkv.encode(key, value)
            is String -> mmkv.encode(key, value)
            is Int -> mmkv.encode(key, value)
            is Float -> mmkv.encode(key, value)
            is Double -> mmkv.encode(key, value)
            else -> {
            }
        }
    }


    fun <T> decode(key: String, dedaultV: T): T {
        return when (dedaultV) {
            is Long -> mmkv.decodeLong(key, dedaultV) as T
            is Int -> mmkv.decodeInt(key, dedaultV) as T
            is String -> mmkv.decodeString(key, dedaultV) as T
            is Boolean -> mmkv.decodeBool(key, dedaultV) as T
            is Float -> mmkv.decodeFloat(key, dedaultV) as T
            is Double -> mmkv.decodeDouble(key, dedaultV) as T
            else -> {
                dedaultV
            }
        }
    }

    fun getIP(): String = decode("app_ip", "")
    fun saveIP(ip: String) = encode("app_ip", ip)


    fun getAdShowTimes(): Int = mmkv.decodeInt("ad_show_times", 0)


    fun setAdClickTimes(v: Int) {
        mmkv.encode("ad_click_times", v)
    }

    fun getAdClickTimes(): Int = mmkv.decodeInt("ad_click_times", 0)


    fun canPopTanIsOverMax(key: String, max: Int): Boolean {
        if (0 == max) return true
        return try {
            val countItem = mmkv.decodeParcelable(key, DaiBooPopShowItem::class.java)
            LogDB.dpop("check ---> last times = ${countItem?.counts} ")
            if (null != countItem && TimeUtils.isToday(countItem.time)) {
                countItem.counts < max
            } else true
        } catch (e: Exception) {
            true
        }
    }

    fun getTanLastTime(tanId: String): Long {
        val countItem = mmkv.decodeParcelable(tanId, DaiBooPopShowItem::class.java)
        return countItem?.time ?: 0L
    }

    fun saveTanShowCounts(tanId: String) {
        try {
            val item = mmkv.decodeParcelable(tanId, DaiBooPopShowItem::class.java)
            if (null != item && TimeUtils.isToday(item.time)) {
                item.counts++
                item.time = Date().time
                mmkv.encode(tanId, item)
                LogDB.dpop("check --->save last one>>${tanId} ")
            } else {
                mmkv.encode(tanId, DaiBooPopShowItem())
                LogDB.dpop("check --->save new one>>${tanId} ")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isNeedPostDev(): Boolean {
        return decode(MK_IS_POSTED_DEV, false).not()
    }

    fun savePostDev(b: Boolean) {
        encode(MK_IS_POSTED_DEV, b)
    }



    fun getLastTableADShowTime(): Long {
        return mmkv.decodeLong(MK_LAST_TABLE_INT_TIME)
    }
    fun saveLastTableADShowTime() {
         mmkv.encode(MK_LAST_TABLE_INT_TIME,Date().time)
    }
}
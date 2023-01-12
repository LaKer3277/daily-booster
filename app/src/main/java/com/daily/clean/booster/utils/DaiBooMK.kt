package com.daily.clean.booster.utils

import com.blankj.utilcode.util.TimeUtils
import com.daily.clean.booster.base.*
import com.daily.clean.booster.entity.DaiBooPopShowItem
import com.daily.clean.booster.ext.ss
import com.tencent.mmkv.MMKV
import java.util.*

object DaiBooMK {

    private val mmkv: MMKV =
        MMKV.mmkvWithID(DB_NAME + "MMKV", MMKV.MULTI_PROCESS_MODE)

    private const val MK_IS_FIRST_START = "${DB_NAME}is_has_start"
    private const val MK_LAST_CLEAN_TIME = "${DB_NAME}last_Clean_time"
    private const val MK_CLEAN_JUNK_SIZE_APPCACHE = "${DB_NAME}clean_junk_appcache"
    private const val MK_LAST_BOOST_TIME = "${DB_NAME}last_boost_time"
    private const val MK_LAST_SCAN_TIME = "${DB_NAME}last_scan_time"

    const val MK_REFERRER_URL = "${DB_NAME}mk_referrer_url"
    const val MK_REFERRER_INSTALL_VERSION = "${DB_NAME}mk_referrer_install_version"
    const val MK_REFERRER_INSTALL_TIME = "${DB_NAME}mk_referrer_install_time"
    const val MK_REFERRER_CLICK = "${DB_NAME}mk_referrer_click_timestamp_seconds"
    const val MK_REFERRER_CLICK_SERVER = "${DB_NAME}mk_referrer_click_timestamp_server_seconds"
    const val MK_REFERRER_INSTALL_SERVER = "${DB_NAME}mk_install_begin_timestamp_server_seconds"
    const val MK_REFERRER_GOOGLE_PLAY_INSTANT= "${DB_NAME}mk_google_play_instant"

    const val MK_LAST_SESSION_TIME = "${DB_NAME}mk_last_session_time"
    const val MK_TBA_INSTALL_TIME = "${DB_NAME}mk_tab_install_time"

    const val MK_TBA_LIST_CACHE = "${DB_NAME}mk_tab_list_CACHE"

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
        return Date().time - getLastCleanTime() > CLEAN_TIME_INTERVAL
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

}
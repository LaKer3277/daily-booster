package com.daily.clean.booster.tba

import android.os.Build
import android.webkit.WebSettings
import com.daily.clean.booster.BuildConfig
import com.daily.clean.booster.appIns
import com.daily.clean.booster.base.*
import com.daily.clean.booster.entity.DaiBooAdEvent
import com.daily.clean.booster.entity.DaiBooLogEvent
import com.daily.clean.booster.ext.loggerHttp
import com.daily.clean.booster.utils.DaiBooMK
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.util.*

object HttpTBA: IHttpBase(), CoroutineScope by GlobalScope {

    private var userAgentStr = ""

    private const val EVENT_SESSION = "session_event"
    private const val EVENT_INSTALL = "install_event"
    private const val EVENT_AD = "ad_event"
    private const val EVENT_LOG = "log_event"

    fun initStartup() {
        userAgentStr = try {
            WebSettings.getDefaultUserAgent(appIns)// app context can get
        } catch (e: Exception) {
            ""
        }
        screen_dpi = "${appIns.resources.displayMetrics.density}"
        launch {
            queryAdvertisingInfo()
            queryRefer()
        }
    }

    fun doReportSession() {
        doReport(EVENT_SESSION)
    }

    fun doReportRefer(bodyMap: MutableMap<String, Any>) {
        doReport(EVENT_INSTALL, referMap = bodyMap)
    }

    fun doReportLog(logEvent: DaiBooLogEvent? = null) {
        doReport(EVENT_LOG, logEvent = logEvent)
    }

    fun doReportAd(adEvent: DaiBooAdEvent? = null) {
        if (adEvent == null) return

        val adPreEcpm = adEvent.daiboo_value_micros
        val currency = "USD"
        //广告网络，广告真实的填充平台，例如admob的bidding，填充了Facebook的广告，此值为Facebook
        val adNetwork = adEvent.daiboo_ad_network
        val adSource = adEvent.daiboo_ad_item.platform
        val adCodeId = adEvent.daiboo_ad_item.id
        val adPosId = adEvent.daiboo_key
        val adRitId = adEvent.daiboo_ad_item.id
        val adSense = ""
        val adFormat = adEvent.daiboo_ad_item.getFormat()
        //google ltvpingback的预估收益类型
        val precisionType = adEvent.daiboo_precision_type
        val ip = ipBean?.ip ?: ""           //客户端IP地址，获取的结果需要判断是否为合法的ip地址！！

        val mapBody = mutableMapOf<String, Any>()
        mapBody.apply {
            this["knick"] = adPreEcpm
            this["defrost"] = currency
            this["creepy"] = adNetwork
            this["lubbock"] = adSource
            this["belgrade"] = adCodeId
            this["chevron"] = adPosId
            this["ember"] = adRitId
            this["globular"] = adSense
            this["vex"] = adFormat
            this["hyena"] = precisionType
            this["familiar"] = ip
            this["gogh"] = ip
        }
        doReport(EVENT_AD, adEventMap = mapBody)
    }

    //上报data
    private fun doReport(
        event: String,
        logEvent: DaiBooLogEvent? = null,
        adEventMap: MutableMap<String, Any>? = null,
        referMap: MutableMap<String, Any>? = null) {
        if (!DB_USE_TBA) {
            return
        }
        //session 事件只是在开机的时候上报
        if (event == EVENT_SESSION) {
            val lastSessionTime: Long =
                DaiBooMK.decode(DaiBooMK.MK_LAST_SESSION_TIME, 0L)
            if (Date().time - lastSessionTime < 30 * 1000) return
        }

        GlobalScope.launch(Dispatchers.IO) {
            httpReport(event, logEvent, adEventMap, referMap)
        }
    }

    private suspend fun httpReport(
        event: String,
        logEvent: DaiBooLogEvent? = null,
        adEventMap: MutableMap<String, Any>? = null,
        referMap: MutableMap<String, Any>? = null
    ) {
        val cacheList = mutableListOf<Any>()
        val bundleId = BuildConfig.APPLICATION_ID
        val os = "lizard"                   //操作系统；映射关系：{"lizard": "android", "delano": "ios", "nbs": "web"}
        val ip = ipBean?.ip ?: ""           //客户端IP地址，获取的结果需要判断是否为合法的ip地址！！

        val systemLanguage = "${Locale.getDefault().language}_${Locale.getDefault().country}"
        val clientTs = if (event == EVENT_INSTALL) {
            DaiBooMK.decode(DaiBooMK.MK_TBA_INSTALL_TIME, Date().time)
        } else {
            Date().time                     //日志发生的客户端时间，毫秒数
        }
        //头
        val header = mutableMapOf<String, Any>()
        header.run {
            this["brad"] = bundleId.replace(";", "")
            this["len"] = getAndroidId().replace(";", "")
        }
        //参数
        val queryMap = mutableMapOf<String, Any>()
        queryMap.run {
            this["smoke"] = URLEncoder.encode(screen_dpi, "UTF-8")
            this["angle"] = URLEncoder.encode(systemLanguage, "UTF-8")
            this["weapon"] = URLEncoder.encode(ip, "UTF-8")
            this["universe"] = URLEncoder.encode(os, "UTF-8")
        }
        //body
        val mapBody: MutableMap<String, Any> = mutableMapOf()
        mapBody.run {
            this["iranian"] = getCommonMap()
        }

        try {
            when (event) {
                EVENT_INSTALL -> {
                    referMap?.let {
                        mapBody.putAll(it)
                    }
                }

                EVENT_AD -> {
                    adEventMap?.let {
                        mapBody.apply {
                            this["woodlot"] = it
                        }
                    }
                }

                EVENT_LOG -> {
                    logEvent?.let {
                        mapBody["telltale"] = it.daiboo_log_name
                        it.daiboo_log_param?.let { p ->
                            mapBody["flog"] = p
                        }
                    }
                }
                //session
                else -> {
                    mapBody["ocarina"] = mutableMapOf<String, Any>()
                }
            }

            cacheList.clear()
            //添加 当前data
            cacheList.add(mapBody)

            //添加历史data
            /*val listJson = DaiBooMK.decode(DaiBooMK.MK_TBA_LIST_CACHE, "")
            if (listJson.isNotEmpty()) {
                val jlist = JsonParser().parse(listJson).asJsonArray
                jlist?.forEach {
                    cacheList.add(it.asJsonObject)
                }
            }*/

            doHttpPost(cacheList, header, queryMap)
            //DaiBooMK.encode(DaiBooMK.MK_TBA_LIST_CACHE, "")
            when (event) {
                EVENT_SESSION -> DaiBooMK.encode(DaiBooMK.MK_LAST_SESSION_TIME, Date().time)
                EVENT_INSTALL -> DaiBooMK.encode(DaiBooMK.MK_TBA_INSTALL_TIME, clientTs)
            }
        } catch (e: Exception) {
            /*GlobalScope.launch(Dispatchers.IO) {
                cacheList.run {
                    if (size > 0) {
                        while (size > 10) {
                            cacheList.removeAt(0)
                        }
                        val endJson = Gson().toJson(cacheList)
                        DaiBooMK.encode(DaiBooMK.MK_TBA_LIST_CACHE, endJson)
                    }
                }
            }*/
        }
    }

    private suspend fun doHttpPost(cacheList: MutableList<Any>, header: MutableMap<String, Any>, queryMap: MutableMap<String, Any>) {
        val endJson = Gson().toJson(cacheList)
        val requestBody = endJson.toRequestBody("text/plain".toMediaTypeOrNull())
        //请求
        val result = serviceStr.report(header, requestBody, queryMap)
        loggerHttp("HttpTBA, result: $result")
        cacheList.clear()
    }


    private fun getLastUpdateTime() = try {
        appIns.packageManager.getPackageInfo(appIns.packageName, 0).lastUpdateTime
    } catch (e: Exception) {
        0L
    }

    private fun getFirstInstallTime() = try {
        appIns.packageManager.getPackageInfo(appIns.packageName, 0).firstInstallTime
    } catch (e: Exception) {
        0L
    }

    override fun recordSelfInstall(
        referrerUrl: String,
        version: String,
        clickTime: Long,
        beginTime: Long,
        serverClickTime: Long,
        serverBeginTime: Long,
        googlePlayInstantParam: Boolean
    ) {
        val mapBody: MutableMap<String, Any> = mutableMapOf()
        mapBody.run {
            put("telltale", "calcite")
            put("grave", "build/${Build.ID}")        //系统构建版本，Build.ID， 以 build/ 开头
            put("dyeing", referrerUrl)
            put("radon", version)
            put("skin", userAgentStr)
            put("ix", if (isLimitedTracking) "maybe" else "lactate")
            put("quid", clickTime)
            put("like", beginTime)
            put("bernet", serverClickTime)
            put("shasta", serverBeginTime)
            put("wintry", getFirstInstallTime())
            put("jab", getLastUpdateTime())
            put("purcell", googlePlayInstantParam)
        }
        doReportRefer(mapBody)
    }

    var isUserBuyer = false
    var isUserBuyerFb = false
    var isUserCommon = true
    override fun recognizeUser(installRefer: String) {
        isUserBuyerFb = installRefer.contains("fb4a", false)
        isUserBuyer = isUserBuyerFb
                || installRefer.contains("gclid", false)
                || installRefer.contains("not%20set", false)
                || installRefer.contains("youtubeads", false)
                || installRefer.contains("%7B%22", false)
        isUserCommon = !isUserBuyer
    }

}
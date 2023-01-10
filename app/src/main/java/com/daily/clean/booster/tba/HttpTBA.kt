package com.daily.clean.booster.tba

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Base64
import android.util.Log
import android.webkit.WebSettings
import android.widget.Toast
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.blankj.utilcode.util.RomUtils
import com.daily.clean.booster.BuildConfig
import com.daily.clean.booster.App
import com.daily.clean.booster.base.DBConfig
import com.daily.clean.booster.base.FiBRemoteUtil
import com.daily.clean.booster.entity.DaiBooAdEvent
import com.daily.clean.booster.entity.DaiBooIpBean
import com.daily.clean.booster.entity.DaiBooLogEvent
import com.daily.clean.booster.utils.DaiBooMK
import com.daily.clean.booster.utils.FileSizeUtil
import com.daily.clean.booster.utils.LogDB
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.TimeUnit


object HttpTBA {

    val BASE_URL = "https://epoch.dailybooster.link/"

    //test
//    val BASE_URL = "http://irishmangrandson-735057175.us-east-1.elb.amazonaws.com/"

    //retrofit对象
    private var retrofit: Retrofit? = null
    private var retrofit2: Retrofit? = null


    //请求的api，可以根据不同的场景设置多个
    val service: BoosterApi by lazy {
        getRetrofit().create(BoosterApi::class.java)
    }

    val serviceStr: BoosterApi by lazy {
        getRetrofitStr().create(BoosterApi::class.java)
    }

    private fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
//                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build()
        }
        return retrofit!!
    }

    private fun getRetrofitStr(): Retrofit {
        if (retrofit2 == null) {
            retrofit2 = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getOkHttpClient())
                .addConverterFactory(ScalarsConverterFactory.create())
//                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build()
        }
        return retrofit2!!
    }

    private fun getOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient().newBuilder()

        var loggingInterceptor =
            HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
//                   LogClnopt.dReport("HTTP:$message")
                    Log.d("HTTP--->", "$message")
                }
            })
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        builder.run {
            addInterceptor(loggingInterceptor)
            connectTimeout(60, TimeUnit.SECONDS)
            readTimeout(60, TimeUnit.SECONDS)
            writeTimeout(60, TimeUnit.SECONDS)
//            retryOnConnectionFailure(true) // 错误重连
            // cookieJar(CookieManager())
        }
        return builder.build()
    }


    var referrerUrl: String = ""
    var response: ReferrerDetails? = null
    var userAgentStr = ""
    var screen_res = ""

    const val EVENT_SESSION = "session_event"
    const val EVENT_INSTALL = "install_event"
    const val EVENT_AD = "ad_event"
    const val EVENT_LOG = "log_event"

    /**
     *  上报data
     */
    open fun doReport(
        event: String,
        adevent: DaiBooAdEvent? = null,
        logEvent: DaiBooLogEvent? = null
    ) {
        if (DBConfig.DAIBOO_USE_TBA.not()) {
            LogDB.d("DAIBOO_USE_TBA == false")
            return
        }
        //session 事件只是在开机的时候上报
        if (event == EVENT_SESSION) {
            val lastSessionTime: Long =
                DaiBooMK.decode(DaiBooMK.MK_LAST_SESSION_TIME, 0L)
            if (Date().time - lastSessionTime < 30 * 1000) return
        }
        getIP()
        GlobalScope.launch(Dispatchers.IO) {
            if (event == EVENT_AD) {
                delay(1000)
                adevent?.let {
                    if (it.daiboo_value_micros > 0) {
//                        FireBLogEventUtils.log_ad_impression_re(it.daiboo_mico)
                    }
                }

            }
            http_report(event, adevent, logEvent)
        }

    }

    var ipBean: DaiBooIpBean? = null
    fun getIP() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                ipBean = service.getIp()
                ipBean?.let {
                    DaiBooMK.saveIP(it.ip)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }


    private suspend fun http_report(
        event: String,
        adevent: DaiBooAdEvent? = null,
        logEvent: DaiBooLogEvent? = null,
    ) {
        LogDB.dReport("report---start----$event")
        var cacheList = mutableListOf<Any>()
        try {

            val bundle_id = BuildConfig.APPLICATION_ID
            val network_type = "wifi"


            val channel = "gp"
            val os = "lizard"//操作系统；映射关系：{"lizard": "android", "delano": "ios", "nbs": "web"}
            val storage_size = FileSizeUtil.getTotalInternalMemorySize()
            val idfa = ""// IOS
            val server_ip = ""// 服务器IP地址，只用于内部填充
            val device_model = Build.MODEL// 手机型号
            val sdk_ver = Build.VERSION.SDK_INT

            // 没有开启google广告服务的设备获取不到，但是必须要尝试获取，用于归因，原值，google广告id
            var gaid = ""
            //用户是否启用了限制跟踪，0：没有限制，1：限制了；映射关系：{"lactate": 0, "maybe": 1}
            var lat = "lactate"
            try {
                AdvertisingIdClient.getAdvertisingIdInfo(App.ins).apply {
                    gaid = this.id ?: ""
                    lat = if (isLimitAdTrackingEnabled) "maybe" else "lactate"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                LogDB.e("get GAID-ERROR----")
            }

            val ip = ipBean?.ip ?: "" //客户端IP地址，获取的结果需要判断是否为合法的ip地址！！

            var uuid: String? = DaiBooMK.mmkv.decodeString("uuid", "")
            if (uuid.isNullOrEmpty()) {
                uuid = UUID.randomUUID().toString().replace("-", "")
                DaiBooMK.encode("uuid", uuid)
            }
            val key = UUID.randomUUID().toString()
            val distinct_id = uuid //用户排重字段，统计涉及到的排重用户数就是依据该字段，对接时需要和产品确认：事件公共字段
            if (DBConfig.DAIBOO_USE_SHOW_UUID) {
                GlobalScope.launch(Dispatchers.Main) {
                    Toast.makeText(App.ins, "$distinct_id", Toast.LENGTH_SHORT).show()
                }
            }

            val app_version = BuildConfig.VERSION_NAME //应用的版本
            val zone_offset = TimeZone.getDefault().rawOffset / 3600000//客户端时区
            val os_version = Build.VERSION.RELEASE//操作系统版本号
            val cpu_name = Build.CPU_ABI ?: ""


            val operator = getGSMCellLocationInfo()//网络供应商名称，mcc和mnc： https://string.quest/read/2746270
            val manufacturer = RomUtils.getRomInfo()?.name ?: ""//收集厂商，hauwei、opple
            val android_id = getAndroidId()//android App需要有该字段，原值
            val idfv = ""//ios的idfv原值
            val log_id = UUID.randomUUID().toString()//日志唯一id，用于排重日志
            val system_language = "${Locale.getDefault().language}_${Locale.getDefault().country}"
            val os_country = Locale.getDefault().country
            val brand = Build.BRAND ?: ""//品牌
            val client_ts = if (event == EVENT_INSTALL) {
                DaiBooMK.decode(DaiBooMK.MK_TBA_INSTALL_TIME, Date().time)
            } else {
                Date().time//日志发生的客户端时间，毫秒数
            }


            val last_update_seconds = getLastUpdateIime()
            val install_first_seconds = getFirstInstallTime()

            val build = "build/Build.ID" //系统构建版本，Build.ID， 以 build/ 开头
            //webview中的user_agent, 注意为webview的，android中的useragent有;wv关键字
            val user_agent = userAgentStr


            //头
            val header = mutableMapOf<String, Any>()
            header.run {
                this["brad"] = bundle_id.replace(";", "")
                this["len"] = android_id.replace(";", "")
            }
            //参数
            val queryMap = mutableMapOf<String, Any>()
            queryMap.run {
                this["smoke"] = URLEncoder.encode(screen_dpi, "UTF-8")
                this["angle"] = URLEncoder.encode(system_language, "UTF-8")
                this["weapon"] = URLEncoder.encode(ip, "UTF-8")
                this["universe"] = URLEncoder.encode(os, "UTF-8")
            }
            //body
            var mapBody: MutableMap<String, Any> = mutableMapOf()
            mapBody.run {
                this["iranian"] = mutableMapOf<String, Any>().apply {
                    this["medley"] = brand
                    this["earwig"] = idfa
                    this["adultery"] = gaid
                    this["angle"] = system_language
                    this["smoke"] = screen_dpi
                    this["feverish"] = manufacturer
                    this["universe"] = os
                    this["midland"] = operator
                    this["liberate"] = network_type
                    this["jittery"] = client_ts
                    this["mobility"] = distinct_id
                    this["weapon"] = ip
                    this["nihilist"] = zone_offset
                    this["leona"] = os_version
                    this["supplant"] = channel
                    this["brad"] = bundle_id
                    this["monsieur"] = os_country
                    this["dade"] = app_version
                    this["widgeon"] = device_model
                    this["bergland"] = log_id
                    this["len"] = android_id
                    this["bong"] = idfv
                    this["casualty"] = cpu_name
                    this["phil"] = key
                }

            }


            when (event) {
                EVENT_INSTALL -> {

                    //referrer 信息
                    val referrer_url =
                        if (referrerUrl.isNullOrEmpty().not())
                            referrerUrl
                        else DaiBooMK.decode(DaiBooMK.MK_REFERRER_URL, "")
                    val install_version =
                        DaiBooMK.decode(DaiBooMK.MK_REFERRER_INSTALL_VERSION, "")

                    val install_begin_timestamp_seconds = DaiBooMK.decode(DaiBooMK.MK_REFERRER_INSTALL_TIME, 0L)
                    val install_begin_timestamp_server_seconds = DaiBooMK.decode(DaiBooMK.MK_REFERRER_INSTALL_SERVER, 0L)

                    val referrer_click_timestamp_seconds = DaiBooMK.decode(DaiBooMK.MK_REFERRER_CLICK, 0L)
                    val referrer_click_timestamp_server_seconds = DaiBooMK.decode(DaiBooMK.MK_REFERRER_CLICK_SERVER, 0L)
                    val google_play_instant = DaiBooMK.decode(DaiBooMK.MK_REFERRER_GOOGLE_PLAY_INSTANT, false)

                    mapBody.run {
                        put("telltale", "calcite")
                        put("grave", build)
                        put("dyeing", referrer_url)
                        put("radon", install_version)
                        put("skin", user_agent)
                        put("ix", lat)
                        put("quid", referrer_click_timestamp_seconds)
                        put("like", install_begin_timestamp_seconds)
                        put("bernet", referrer_click_timestamp_server_seconds)
                        put("shasta", install_begin_timestamp_server_seconds)
                        put("wintry", install_first_seconds)
                        put("jab", last_update_seconds)
                        put("purcell", google_play_instant)
                    }

                }
                EVENT_AD -> {

                    adevent?.let {

                        val ad_pre_ecpm = it.daiboo_value_micros
                        val currency = "USD"
                        //广告网络，广告真实的填充平台，例如admob的bidding，填充了Facebook的广告，此值为Facebook
                        val ad_network = it.daiboo_ad_network
                        val ad_source = it.daiboo_ad_item.Network
                        val ad_code_id = it.daiboo_ad_item.Id
                        val ad_pos_id = it.daiboo_key
                        val ad_rit_id = it.daiboo_ad_item.Id
                        val ad_sense = ""
                        val ad_format = it.daiboo_ad_item.getFormat()
                        //google ltvpingback的预估收益类型
                        val precision_type = it.daiboo_precision_type
                        val ad_impression_ip = ip
                        val ad_load_ip = ip
                        val sdk_ver = "21.3.0"

                        mapBody.apply {

                            this["woodlot"] = mutableMapOf<String, Any>().apply {
                                this["knick"] = ad_pre_ecpm
                                this["defrost"] = currency
                                this["creepy"] = ad_network
                                this["lubbock"] = ad_source
                                this["belgrade"] = ad_code_id
                                this["chevron"] = ad_pos_id
                                this["ember"] = ad_rit_id
                                this["globular"] = ad_sense
                                this["vex"] = ad_format
                                this["hyena"] = precision_type
                                this["familiar"] = ad_load_ip
                                this["gogh"] = ad_impression_ip
                            }

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
            val listJson = DaiBooMK.decode(DaiBooMK.MK_TBA_LIST_CACHE, "")
            LogDB.dReport("$event---> history data 1：${listJson}")
            if (listJson.isNotEmpty()) {
                val jlist = JsonParser().parse(listJson).asJsonArray
                jlist?.forEach {
                    cacheList.add(it.asJsonObject)
                }
                LogDB.dReport("$event--->history data 2：${cacheList.size}")
            }

            val endJson = Gson().toJson(cacheList)
            LogDB.dReport("$event--->original data=>$endJson")
            val requestBody = endJson.toRequestBody("text/plain".toMediaTypeOrNull())
            //请求
            LogDB.dReport("$event-->start post")
            var result = serviceStr.report(header, requestBody, queryMap)
            LogDB.dReport("$event---result=$result ")

            cacheList.clear()
            DaiBooMK.encode(DaiBooMK.MK_TBA_LIST_CACHE, "")
            if (event == EVENT_SESSION) {
                DaiBooMK.encode(DaiBooMK.MK_LAST_SESSION_TIME, Date().time)
            }
            if (event == EVENT_INSTALL) {
                DaiBooMK.encode(DaiBooMK.MK_TBA_INSTALL_TIME, client_ts)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            LogDB.eReport("$event--->Exception")

            GlobalScope.launch(Dispatchers.IO) {

                cacheList.run {
                    if (size > 0) {
                        while (size > 10) {
                            cacheList.removeAt(0)
                        }
                        val endJson = Gson().toJson(cacheList)
                        DaiBooMK.encode(DaiBooMK.MK_TBA_LIST_CACHE, endJson)
                        LogDB.eReport("$event--->history data  $size")
                    }
                }
            }

        }

    }


    fun base64Encode(s: String): String? {
        return try {
            Base64.encodeToString(s.toByteArray(charset("UTF-8")), Base64.NO_WRAP)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            ""
        }
    }


    /**
     * 获取随机字符串
     */
    fun getRadomStr(): String {
        val idctChars = mutableListOf<Char>().apply {
            "qwertyuiopasdfghjklzxcvbnm1234567890".forEach {
                this.add(it)
            }
        }
        val rs = StringBuilder().apply {
            (2..((4..6).random())).onEach {
                append(idctChars.random())
            }
        }
        return rs.toString()
    }


    var screen_dpi = ""
    fun getReferrerInfo(context: Context, block: (String?) -> Unit) {

        if (referrerUrl.isNullOrEmpty().not()) {
            LogDB.d("get referrer >>> from MK")
            block(referrerUrl)
            return
        }
        LogDB.d("get referrer data>>>")
        val referrerClient = InstallReferrerClient.newBuilder(context).build()
        referrerClient.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        LogDB.d("getreferrer data>>>OK")
                        // Connection established.
                        response = referrerClient.installReferrer
                        response?.run {
                            referrerUrl = installReferrer
                            LogDB.d("getreferrer data>>>$referrerUrl")
                            DaiBooMK.encode(DaiBooMK.MK_REFERRER_URL, referrerUrl)
                            DaiBooMK.encode(DaiBooMK.MK_REFERRER_INSTALL_VERSION, installVersion)
                            DaiBooMK.encode(DaiBooMK.MK_REFERRER_INSTALL_TIME, installBeginTimestampSeconds)
                            DaiBooMK.encode(DaiBooMK.MK_REFERRER_INSTALL_SERVER, installBeginTimestampServerSeconds)

                            DaiBooMK.encode(DaiBooMK.MK_REFERRER_CLICK, referrerClickTimestampSeconds)
                            DaiBooMK.encode(DaiBooMK.MK_REFERRER_CLICK_SERVER, referrerClickTimestampServerSeconds)
                            DaiBooMK.encode(DaiBooMK.MK_REFERRER_CLICK_SERVER, referrerClickTimestampServerSeconds)
                            DaiBooMK.encode(DaiBooMK.MK_REFERRER_GOOGLE_PLAY_INSTANT, googlePlayInstantParam)
                        }
                        doReport(EVENT_INSTALL)
                        referrerClient.endConnection()
                        block(referrerUrl)
                    }
                    else -> {
                        LogDB.d("getreferrer data>>>Failed  $responseCode")
                        block(null)
                        if (DaiBooMK.decode(DaiBooMK.MK_TBA_INSTALL_TIME, 0L) == 0L) {
                            doReport(EVENT_INSTALL)
                        }

                    }
                }
            }

            override fun onInstallReferrerServiceDisconnected() {}
        })
    }


    /**
     * 获取手机基站信息
     * @throws JSONException
     */
    private fun getGSMCellLocationInfo(): String {
        try {
            val manager =
                App.ins.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val operator = manager.networkOperator

            /**通过operatorgetMCC 和MNC  */
            val mcc = operator.substring(0, 3).toInt()
            val mnc = operator.substring(3).toInt()
            return "$mcc$mnc"
        } catch (e: java.lang.Exception) {
            return ""
        }

    }

    private fun getAndroidId(): String {
        return Settings.System.getString(App.ins.contentResolver, Settings.System.ANDROID_ID);
    }


    private fun getLastUpdateIime() = try {
        App.ins.packageManager.getPackageInfo(App.ins.packageName, 0).lastUpdateTime
    } catch (e: Exception) {
        0L
    }

    private fun getFirstInstallTime() = try {
        App.ins.packageManager.getPackageInfo(App.ins.packageName, 0).firstInstallTime
    } catch (e: Exception) {
        0L
    }


    fun reportFirst() {
        userAgentStr = try {
            WebSettings.getDefaultUserAgent(App.ins)// app context can get
        } catch (e: Exception) {
            ""
        }
        referrerUrl = DaiBooMK.decode(DaiBooMK.MK_REFERRER_URL, "")
        screen_dpi = "${App.ins.resources.displayMetrics.density}"
        val w = App.ins.resources.displayMetrics.widthPixels
        val h = App.ins.resources.displayMetrics.heightPixels
        screen_res = "$w$h"

        doReport(EVENT_SESSION)
        getReferrerInfo(App.ins) { rf ->
            rf?.let {
                FiBRemoteUtil.isByuser = it.contains("fb4a", false)
                        || rf.contains("gclid", false)
                        || rf.contains("not%20set", false)
                        || rf.contains("youtubeads", false)
                        || rf.contains("%7B%22", false)
                FiBRemoteUtil.isfacebookUser = it.contains("fb4a", false)
            }
        }
    }

}
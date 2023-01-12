package com.daily.clean.booster.tba

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Base64
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.blankj.utilcode.util.RomUtils
import com.daily.clean.booster.BuildConfig
import com.daily.clean.booster.appIns
import com.daily.clean.booster.entity.DaiBooIpBean
import com.daily.clean.booster.ext.loggerHttp
import com.daily.clean.booster.utils.DaiBooMK
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.UnsupportedEncodingException
import java.util.*
import java.util.concurrent.TimeUnit

abstract class IHttpBase {

    private val BASE_URL = "https://epoch.dailybooster.link/"
    //retrofit对象
    private var retrofit: Retrofit? = null
    private var retrofit2: Retrofit? = null
    protected var screen_dpi = ""

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
                .build()
        }
        return retrofit2!!
    }

    private fun getOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient().newBuilder()

        val loggingInterceptor =
            HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    loggerHttp("HTTPLog--->$message")
                }
            })
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        builder.run {
            addInterceptor(loggingInterceptor)
            connectTimeout(60, TimeUnit.SECONDS)
            readTimeout(60, TimeUnit.SECONDS)
            writeTimeout(60, TimeUnit.SECONDS)
        }
        return builder.build()
    }

    protected val tag = "DeepQueryApi"
    protected var gaid: String = ""
    protected var isLimitedTracking = false
    @Suppress("BlockingMethodInNonBlockingContext")
    protected fun queryAdvertisingInfo(action: (() -> Unit)? = null) {
        if (gaid.isNotEmpty()) {
            action?.invoke()
            return
        }
        val idInfo = try {
            AdvertisingIdClient.getAdvertisingIdInfo(appIns)
        } catch (e: GooglePlayServicesNotAvailableException) {
            null
        } catch (e: GooglePlayServicesRepairableException) {
            null
        } catch (e: Exception) {
            null
        }

        if (idInfo != null) {
            gaid = idInfo.id ?: ""
            isLimitedTracking = idInfo.isLimitAdTrackingEnabled
        }
        action?.invoke()
    }

    protected var ipBean: DaiBooIpBean? = null
    protected fun getIP() {
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

    protected fun queryRefer() {
        val refer = DaiBooMK.decode(DaiBooMK.MK_REFERRER_URL, "")
        if (refer.isNotEmpty()) {
            recognizeUser(refer)
            return
        }
        //没获取到的时候先报一次空值
        //NewUploaderImpl.doReferrer("", "", 0L, 0L)

        try {
            val referrerClient = InstallReferrerClient.newBuilder(appIns).build()
            referrerClient.startConnection(object : InstallReferrerStateListener {

                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    when (responseCode) {
                        InstallReferrerClient.InstallReferrerResponse.OK -> {
                            // Connection established.
                            val response: ReferrerDetails = referrerClient.installReferrer ?: return
                            val version = response.installVersion ?: ""
                            val clickTime: Long = response.referrerClickTimestampSeconds
                            val beginTime: Long = response.installBeginTimestampSeconds
                            val serverClickTime = response.referrerClickTimestampServerSeconds
                            val serverBeginTime = response.installBeginTimestampServerSeconds
                            val referrerUrl: String = response.installReferrer ?: ""
                            val gpParam = response.googlePlayInstantParam

                            recognizeUser(referrerUrl)
                            DaiBooMK.encode(DaiBooMK.MK_REFERRER_URL, referrerUrl)
                            referrerClient.endConnection()
                            recordSelfInstall(referrerUrl, version, clickTime, beginTime, serverClickTime, serverBeginTime, gpParam)
                            //NewUploaderImpl.doReferrer(referrerUrl, version, beginTime, serverClickTime)
                        }
                    }
                }

                override fun onInstallReferrerServiceDisconnected() {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                }
            })
        } catch (e: Exception) {
        }
    }

    abstract fun recordSelfInstall(
        referrerUrl: String,
        version: String,
        clickTime: Long,
        beginTime: Long,
        serverClickTime: Long,
        serverBeginTime: Long,
        googlePlayInstantParam: Boolean
    )
    abstract fun recognizeUser(installRefer: String)
    protected fun getCommonMap(): MutableMap<String, Any> {
        val map = _commonMap
        val returnMap = mutableMapOf<String, Any>()
        for (entry in map) {
            returnMap[entry.key] = entry.value
        }

        returnMap["bergland"] = UUID.randomUUID().toString()
        returnMap["jittery"] = System.currentTimeMillis()
        returnMap["phil"] = UUID.randomUUID().toString()
        return returnMap
    }
    private val _commonMap: MutableMap<String, Any> = buildCommonMap()
    private fun buildCommonMap(): MutableMap<String, Any> {
        val map: MutableMap<String, Any> = mutableMapOf()
        map.run {
            this["medley"] = Build.BRAND ?: ""//品牌
            this["earwig"] = ""
            this["adultery"] = gaid
            this["angle"] = "${Locale.getDefault().language}_${Locale.getDefault().country}"
            this["smoke"] = screen_dpi
            this["feverish"] = RomUtils.getRomInfo()?.name ?: ""//收集厂商，hauwei、opple
            this["universe"] = "lizard"                         //操作系统；映射关系：{"lizard": "android", "delano": "ios", "nbs": "web"}
            this["midland"] = getGSMCellLocationInfo()          //网络供应商名称，mcc和mnc： https://string.quest/read/2746270
            this["liberate"] = ""
            this["mobility"] = DaiBooMK.deviceId()
            this["weapon"] = ipBean?.ip ?: ""                   //客户端IP地址
            this["nihilist"] = TimeZone.getDefault().rawOffset / 3600000//客户端时区
            this["leona"] = Build.VERSION.RELEASE
            this["supplant"] = "gp"
            this["brad"] = appIns.packageName
            this["monsieur"] = Locale.getDefault().country
            this["dade"] = BuildConfig.VERSION_NAME
            this["widgeon"] = Build.MODEL
            this["len"] = getAndroidId()
            this["bong"] = ""
            this["casualty"] = Build.CPU_ABI ?: ""
        }

        return map
    }

    private fun getGSMCellLocationInfo(): String {
        return try {
            val manager =
                appIns.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val operator = manager.networkOperator

            /**通过operatorgetMCC 和MNC  */
            val mcc = operator.substring(0, 3).toInt()
            val mnc = operator.substring(3).toInt()
            "$mcc$mnc"
        } catch (e: java.lang.Exception) {
            ""
        }
    }

    protected fun getAndroidId(): String {
        return Settings.System.getString(appIns.contentResolver, Settings.System.ANDROID_ID);
    }

    protected fun base64Encode(s: String): String {
        return try {
            Base64.encodeToString(s.toByteArray(charset("UTF-8")), Base64.NO_WRAP)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            ""
        }
    }

    protected fun getRandomStr(): String {
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
}
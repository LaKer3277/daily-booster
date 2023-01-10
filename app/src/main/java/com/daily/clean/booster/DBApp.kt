package com.daily.clean.booster

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.alive.union.longer.DaiBooh2
import com.applovin.adview.AppLovinFullscreenActivity
import com.applovin.sdk.AppLovinSdk
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ProcessUtils
import com.daily.clean.booster.base.DBConfig
import com.daily.clean.booster.base.FiBLogEvent
import com.daily.clean.booster.base.FiBRemoteUtil
import com.daily.clean.booster.core.service.Heart
import com.daily.clean.booster.ui.*
import com.daily.clean.booster.utils.AudienceNetworkInitializeHelper
import com.daily.clean.booster.utils.LogDB
import com.daily.clean.booster.utils.isADActivity
import com.daily.clean.booster.utils.startCleanService
import com.daily.clean.booster.utils.work.FiBLogWorker
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class DBApp : Application(), Application.ActivityLifecycleCallbacks {

    companion object {
        lateinit var ins: DBApp
        const val app_name = BuildConfig.APPLICATION_ID
        var isReceiveerScreenOn = false
        var listActivity: MutableList<Activity>? = mutableListOf()
        var isAdActivityResume = false
        var isNotDoHotStart = false
        var isColdStart = true
        var isHotStart = false

        var timeOnAppStop = 0L
        var activityCount = 0
        var backJob: Job? = null

    }


    override fun onCreate() {
        super.onCreate()
        ins = this
        DaiBooh2(this).it()
        
        if (isNotMainProcess()) return
        kotlin.runCatching {
//            Heart.registerReceivers(this)
            Heart.statrTimingAlertJob()
        }
        startCleanService()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        registerActivityLifecycleCallbacks(this)
        MMKV.initialize(this)

        AudienceNetworkInitializeHelper.initialize(this)
        if (DBConfig.DAIBOO_USE_FB)
            Firebase.initialize(this)
        if (DBConfig.DAIBOO_USE_AD) {
            MobileAds.initialize(this)
//            initMaxAD()
        }
        val testDeviceIds = listOf("", "", "")
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        MobileAds.setRequestConfiguration(configuration)

        FiBRemoteUtil.initFireBaseData()
        FiBLogEvent.app_active()
        FiBLogEvent.user_rent()

//        NLU.initNotificationListenerService(this)
        initWork(this)

        LogDB.dLife("App onCreate $isColdStart")
    }


    fun initMaxAD() {
        // Make sure to set the mediation provider value to "max" to ensure proper functionality
        AppLovinSdk.getInstance(this).run {
            mediationProvider = "max"
            initializeSdk {}
//            showMediationDebugger()
        }
    }


    private fun isNotMainProcess(): Boolean {
        var pName = getProcessNameString()
        LogDB.d("process name:${pName} ")
        return app_name != getProcessNameString()
    }

    private fun getProcessNameString(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getProcessName()
        } else ProcessUtils.getCurrentProcessName()
    }


    fun isAtForeground(): Boolean {
        return activityCount > 0
    }


    override fun onActivityStarted(activity: Activity) {
        LogDB.dLife("Started: ---${activity::class.java.simpleName}  isHotStart = $isHotStart  isColdStar =${isColdStart}  action = ${activity.intent?.action} ")
        activityCount++
        backJob?.cancel()
        if (isHotStart) {
            isHotStart = false
            when (activity) {
                is SplashActivity, is AdActivity, is AppLovinFullscreenActivity, is NotificationActivity -> {}
                else -> {
                    if (activity.intent?.action == DBConfig.DAIBOO_ACTION_FROM_NOTIFY_NM) return
                    if (activity.intent?.action == DBConfig.DAIBOO_ACTION_FROM_POP_NOTY_POP) return
                    if (isScreenOn().not()) return
                    ActivityUtils.startActivity(SplashActivity::class.java)
                }
            }
        } else if (isColdStart) {
            isColdStart = false
        }


    }

    override fun onActivityStopped(activity: Activity) {
        activityCount--
        LogDB.dLife("Stopped: ---${activity::class.java.simpleName}  count=${activityCount} $isNotDoHotStart ")
        if (activityCount <= 0) {
            timeOnAppStop = System.currentTimeMillis()
            backJob = GlobalScope.launch {
                delay(3000L)
                if (activity.isADActivity()) {
                    LogDB.dLife("Stopped: ---${activity::class.java.simpleName} -- finish()")
                    activity.finish()
                }
                delay(1000L)
                if (activityCount <= 0
                    && isNotDoHotStart.not()
                    && activity !is FirstEnterActivity
                    && activity !is NotificationActivity
                ) {
                    isHotStart = true
                    LogDB.dLife("Stopped: ---${activity::class.java.simpleName} -- isHotStart=$isHotStart")

                }
            }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        LogDB.dLife("Created: ---${activity::class.java.simpleName}")
        listActivity?.add(activity)
        if (activity is SplashActivity || activity is LaunchActivity) {
            startCleanService()
        }


    }

    override fun onActivityResumed(activity: Activity) {
        LogDB.dLife("Resumed: ---${activity::class.java.simpleName}  ${listActivity?.size}")
        isAdActivityResume = activity.isADActivity()
        if (activity is SplashActivity || activity is JunkScanActivity) {
            isNotDoHotStart = false
        }
    }

    override fun onActivityPaused(activity: Activity) {
        LogDB.dLife("Paused: ---${activity::class.java.simpleName}")
        if (activity.isADActivity()) {
            isAdActivityResume = false
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        LogDB.dLife("Destroyed: ---${activity::class.java.simpleName}  ${listActivity?.size}")
        listActivity?.remove(activity)
        if (listActivity?.isEmpty() == true) {
            backJob?.cancel()
            isColdStart = true
        }
    }

    fun isScreenOn(): Boolean {
        val pm = ins.applicationContext.getSystemService(POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT >= 20) pm.isInteractive else pm.isScreenOn
    }


    fun initWork(c: Context) {
        val sendLogsWorkRequest =
            PeriodicWorkRequestBuilder<FiBLogWorker>(PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
                .build()
        WorkManager.getInstance(c).enqueueUniquePeriodicWork(
            "${DBConfig.DAIBOO_SPNAME}_work_clean",
            ExistingPeriodicWorkPolicy.KEEP,
            sendLogsWorkRequest
        )
    }


}


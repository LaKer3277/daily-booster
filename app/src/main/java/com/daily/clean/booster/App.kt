package com.daily.clean.booster

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.alive.union.longer.DaiBooh2
import com.applovin.adview.AppLovinFullscreenActivity
import com.applovin.sdk.AppLovinSdk
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ProcessUtils
import com.daily.clean.booster.base.*
import com.daily.clean.booster.base.FirebaseEvent
import com.daily.clean.booster.core.StartupReceiver
import com.daily.clean.booster.datas.RemoteConfig
import com.daily.clean.booster.ext.loggerApp
import com.daily.clean.booster.ui.*
import com.daily.clean.booster.ui.clean.JunkScanActivity
import com.daily.clean.booster.utils.AudienceNetworkInitializeHelper
import com.daily.clean.booster.ext.isADActivity
import com.daily.clean.booster.tba.HttpTBA
import com.daily.clean.booster.utils.work.WorkerAll
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.FirebaseApp
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


val isDebugMode = BuildConfig.DEBUG
lateinit var appIns: App
class App : Application(), Application.ActivityLifecycleCallbacks {

    companion object {
        const val app_name = BuildConfig.APPLICATION_ID
        var isReceiverScreenOn = false
        var isAdActivityResume = false
        var isNotDoHotStart = false
    }

    override fun onCreate() {
        super.onCreate()
        appIns = this
        DaiBooh2(this).it()
        
        if (isNotMainProcess()) return
        StartupReceiver.registerReceivers(this)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        MMKV.initialize(this)

        AudienceNetworkInitializeHelper.initialize(this)
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {}
        initAds()

        RemoteConfig.ins.fetchInit()
        HttpTBA.initStartup()

        WorkerAll.startImmediatelyWork(this)
        registerActivityLifecycleCallbacks(this)

        GlobalScope.launch {
            while (true) {
                FirebaseEvent.logEvent("session_sttt")
                delay(30 * 60_000L)
            }
        }
    }

    private fun initAds() {
        MobileAds.initialize(this)

        val testDeviceIds = listOf("")
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        MobileAds.setRequestConfiguration(configuration)
    }

    fun initMaxAD() {
        // Make sure to set the mediation provider value to "max" to ensure proper functionality
        AppLovinSdk.getInstance(this).run {
            mediationProvider = "max"
            initializeSdk {}
            //showMediationDebugger()
        }
    }


    private fun isNotMainProcess(): Boolean {
        val pName = getProcessNameString()
        loggerApp("process name:${pName} ")
        return app_name != getProcessNameString()
    }

    private fun getProcessNameString(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getProcessName()
        } else ProcessUtils.getCurrentProcessName()
    }


    private var isHotStart = false
    private var backJob: Job? = null
    private var activityCount = 0
    fun isAtForeground(): Boolean {
        return activityCount > 0
    }





    override fun onActivityStarted(activity: Activity) {
        Log.d("ActivityLife", "onActivityStarted: ---$activity  isHotStart = $isHotStart  action = ${activity.intent?.action} ")
        activityCount++
        backJob?.cancel()
        StartupReceiver.cancelTimingJob()
        if (isHotStart) {
            isHotStart = false
            when (activity) {
                is SplashActivity, is AdActivity, is AppLovinFullscreenActivity, is NotificationActivity -> {}
                else -> {
                    if (activity.intent?.action == DB_ACTION_FROM_POP_NOTY) return
                    if (isScreenOn().not()) return
                    ActivityUtils.startActivity(SplashActivity::class.java)
                }
            }
        }
    }

    override fun onActivityStopped(activity: Activity) {
        activityCount--
        Log.d("ActivityLife", "onActivityStopped: ---$activity  count=${activityCount} $isNotDoHotStart ")
        if (activityCount <= 0) {
            StartupReceiver.startTimingAlertJob()

            backJob = GlobalScope.launch {
                delay(3000L)
                if (activity.isADActivity()) {
                    activity.finish()
                }
                if (activityCount <= 0
                    && isNotDoHotStart.not()
                    && activity !is FirstEnterActivity
                    && activity !is NotificationActivity
                ) {
                    isHotStart = true
                }
            }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityResumed(activity: Activity) {
        isAdActivityResume = activity.isADActivity()
        if (activity is SplashActivity || activity is JunkScanActivity) {
            isNotDoHotStart = false
        }
    }

    override fun onActivityPaused(activity: Activity) {
        if (activity.isADActivity()) {
            isAdActivityResume = false
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        Log.d("ActivityLife", "onActivityDestroyed: ---$activity")
    }

    private fun isScreenOn(): Boolean {
        val pm = appIns.applicationContext.getSystemService(POWER_SERVICE) as PowerManager
        return pm.isInteractive
    }


}


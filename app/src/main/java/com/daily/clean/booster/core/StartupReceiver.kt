package com.daily.clean.booster.core

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.daily.clean.booster.App
import com.daily.clean.booster.appIns
import com.daily.clean.booster.pop.*
import com.daily.clean.booster.pop.NotifyManager
import kotlinx.coroutines.*

object StartupReceiver {

    private var isChargeConnect = false
    fun registerReceivers(context: Context) {
        //屏幕点亮 和 关闭的监听
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                doAction(context, intent)
            }
        }, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_USER_PRESENT)
        })

        //卸载/安装 app
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                doAction(context, intent)
            }
        }, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addDataScheme("package")
        })

        startTimingAlertJob()
    }


    private fun doAction(context: Context?, intent: Intent?) {
        when (intent?.action) {
            //屏幕亮起的时候
            Intent.ACTION_SCREEN_ON -> {
                App.isReceiverScreenOn = true
            }

            Intent.ACTION_SCREEN_OFF -> {
                App.isReceiverScreenOn = false
                NotifyManager.tryPopRandom(NotySourceUnlock)
            }

            //充电
            Intent.ACTION_POWER_CONNECTED -> {
                isChargeConnect = true
                NotifyManager.tryPop(NotyWorkBattery, NotySourceCharge)
            }

            Intent.ACTION_POWER_DISCONNECTED -> {
                isChargeConnect = false
            }

            //解锁
            Intent.ACTION_USER_PRESENT -> {
                NotifyManager.tryPopRandom(NotySourceUnlock)
            }

            //卸载
            Intent.ACTION_PACKAGE_REMOVED -> {
                NotifyManager.tryPop(NotyWorkClean, NotySourceUninstall)
            }
        }
    }

    fun cancelTimingJob() {
        timingJob?.cancel()
    }

    private var timingJob: Job? = null
    fun startTimingAlertJob() {
        cancelTimingJob()
        timingJob = GlobalScope.launch {
            while (true) {
                delay(30_000L)
                val manager: BatteryManager = appIns.getSystemService(Service.BATTERY_SERVICE) as BatteryManager
                when (manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)) {
                    4, 9, 14, 19, 29, 39 -> {
                        if (isChargeConnect.not()) {
                            NotifyManager.tryPop(NotyWorkBattery, NotySourceBattery)
                        }
                    }
                }
                
                delay(30_000L)
                NotifyManager.tryPopRandom(NotySourceTime)
            }
        }
    }
}
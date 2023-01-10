package com.daily.clean.booster.core.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.daily.clean.booster.App
import com.daily.clean.booster.base.DBConfig
import com.daily.clean.booster.core.pop.PopCheckHelper
import com.daily.clean.booster.entity.DaiBooUIItem
import com.daily.clean.booster.utils.LogDB
import kotlinx.coroutines.*

object Heart {

    var alertReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            doAction(context, intent)
        }
    }

    var packageRemoveReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            doAction(context, intent)
        }
    }

    fun unrefister(context: Context){
        context.unregisterReceiver(alertReceiver)
        context.unregisterReceiver(packageRemoveReceiver)
    }

    fun registerReceivers(context: Context) {
        //屏幕点亮 和 关闭的监听
        context.registerReceiver(alertReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_USER_PRESENT)
        })

        //卸载/安装 app
        context.registerReceiver(packageRemoveReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addDataScheme("package")
        })

        LogDB.dpop("registerReceivers.....init")
    }


    var isChargeConnect = false
    fun doAction(context: Context?, intent: Intent?) {
        when (intent?.action) {
            //屏幕亮起的时候
            Intent.ACTION_SCREEN_ON -> {
                LogDB.dpop("ACTION_SCREEN_ON")
                App.isReceiverScreenOn = true
                startTimingAlertJob()

            }
            Intent.ACTION_SCREEN_OFF -> {
                App.isReceiverScreenOn = false
                startOpen(DaiBooUIItem.Items.getPopList()[0].id, DBConfig.DAIBOO_NOTY_UNLOCK, true)
            }
            //充电
            Intent.ACTION_POWER_CONNECTED -> {
                LogDB.dpop("ACTION_POWER_CONNECTED")
                isChargeConnect = true
                startOpen(DBConfig.DAIBOO_WORK_ID_BATTERY, DBConfig.DAIBOO_NOTY_CHARGE)
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                isChargeConnect = false
            }
            //解锁
            Intent.ACTION_USER_PRESENT -> {
                LogDB.dpop("ACTION_USER_PRESENT")
//                startOpen(DaiBooUIItem.Items.getPopList()[0].id, DBConfig.DAIBOO_NOTY_UNLOCK, true)
            }
            //卸载
            Intent.ACTION_PACKAGE_REMOVED -> {
                if (intent.getBooleanExtra(Intent.EXTRA_REPLACING,false).not()) {
                    LogDB.dpop("ACTION_PACKAGE_REMOVED")
                    startOpen(DBConfig.DAIBOO_WORK_ID_CLEAN, DBConfig.DAIBOO_NOTY_UNINSTALL)
                }
            }


        }
    }

    var timingJob: Job? = null
    var times: Long = 0L
    fun startTimingAlertJob() {
        timingJob?.cancel()
        timingJob = GlobalScope.launch(Dispatchers.IO) {
            times = 0
            while (true) {
                delay(DBConfig.DAIBOO_POP_TIME_INTERVAL)
                times++
                LogDB.dpop("heart-time task times = $times....")
                if (times % DBConfig.DAIBOO_POP_TIME_INTERVAL_TIMES == 0L) {
                    launch(Dispatchers.Main) {
                        startOpen(DaiBooUIItem.Items.getPopList()[0].id, DBConfig.DAIBOO_NOTY_TIME, true)
                    }

                }

                if (times % 2 == 0L) {
                    val manager: BatteryManager = App.ins.getSystemService(Service.BATTERY_SERVICE) as BatteryManager
                    val currentLevel = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                    LogDB.dpop("heart-Battery currentLevel = $currentLevel%  $isChargeConnect")
                    when (currentLevel) {
                        4, 9, 14, 19, 29, 39 -> {
                            if (isChargeConnect.not()) {
                                startOpen(DBConfig.DAIBOO_WORK_ID_BATTERY, DBConfig.DAIBOO_NOTY_BATTERY)
                            }
                        }
                    }
                }


            }
        }
    }


    private fun startOpen(workId: String, tanID: String, isListPop: Boolean = false, delayTime: Long = 1000) {
        GlobalScope.launch {
            delay(delayTime)
            //FireBLogEventUtils.logTanTrigger(tanID)
            val isSuccess = PopCheckHelper.tryPop(workId, tanID)
            LogDB.dpop("try pop---workId=${workId}  tanID = ${tanID}  is success? = $isSuccess ")
            if (isSuccess && isListPop) {
                DaiBooUIItem.Items.listPop.removeFirst()
            }
        }

    }
}
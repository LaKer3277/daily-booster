package com.daily.clean.booster.core.service

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.IBinder
import com.blankj.utilcode.util.FileUtils
import com.daily.clean.booster.App
import com.daily.clean.booster.base.DBConfig
import com.daily.clean.booster.core.clean.CleanData
import com.daily.clean.booster.core.pop.DaiBooNotifyTool
import com.daily.clean.booster.core.pop.PopCheckHelper
import com.daily.clean.booster.entity.DaiBooCleanEvent
import com.daily.clean.booster.entity.DaiBooUIItem
import com.daily.clean.booster.utils.DaiBooRAMUtils
import com.daily.clean.booster.utils.LogDB
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DaiBooService : Service() {

    var isChargeConnect = false
    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
        initNotification()
        registerReceivers()
        startTimingAlertJob()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        initNotification()
        registerReceivers()
        startTimingAlertJob()
        return START_STICKY
    }


    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        //取消监听注册
        unregisterReceivers()
        timingJob?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(eventDaiBoo: DaiBooCleanEvent?) {
        eventDaiBoo?.let {
            clean(it.isCleanRAM)
        }

    }


    private fun clean(isKillProcess: Boolean) {
        GlobalScope.launch(Dispatchers.IO) {
            if (isKillProcess) DaiBooRAMUtils.clearRAM()
            synchronized(CleanData.cache) {
                CleanData.cache.forEach { cacheParent ->
                    cacheParent.childDaiBooCleans.forEach { cacheChild ->
                        if (cacheChild.isSelected) cacheChild.pathList.forEach { path ->
                            LogDB.dService("---delete---$path")
                            FileUtils.delete(path)
                        }
                    }
                }
                CleanData.cache.clear()
            }
        }
    }

    var notification: Notification? = null
    private fun initNotification() {
        notification = DaiBooNotifyTool.createNotification()
        startForeground(DaiBooNotifyTool.notificationId, notification)
    }


    private var alertReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            doAction(context, intent)
        }
    }

    private var packageRemoveReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            doAction(context, intent)
        }
    }

    private fun registerReceivers() {

        //屏幕点亮 和 关闭的监听
        registerReceiver(alertReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_USER_PRESENT)
        })

        //卸载/安装 app
        registerReceiver(packageRemoveReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addDataScheme("package")
        })

    }


    private fun unregisterReceivers() {
        unregisterReceiver(alertReceiver)
        unregisterReceiver(packageRemoveReceiver)
    }


    fun doAction(context: Context?, intent: Intent?) {
        when (intent?.action) {
            //屏幕亮起的时候
            Intent.ACTION_SCREEN_ON -> {
                App.isReceiverScreenOn = true
            }

            Intent.ACTION_SCREEN_OFF -> {
                App.isReceiverScreenOn = false
                startOpen(DaiBooUIItem.Items.getPopList()[0].id, DBConfig.DAIBOO_NOTY_UNLOCK, true,0)
            }
            //充电
            Intent.ACTION_POWER_CONNECTED -> {
                isChargeConnect = true
                startOpen(DBConfig.DAIBOO_WORK_ID_BATTERY, DBConfig.DAIBOO_NOTY_CHARGE)
            }

            Intent.ACTION_POWER_DISCONNECTED -> {
                isChargeConnect = false
            }

            //解锁
            Intent.ACTION_USER_PRESENT -> {
                startOpen(DaiBooUIItem.Items.getPopList()[0].id, DBConfig.DAIBOO_NOTY_UNLOCK, true)
            }

            //卸载
            Intent.ACTION_PACKAGE_REMOVED -> {
                startOpen(DBConfig.DAIBOO_WORK_ID_CLEAN, DBConfig.DAIBOO_NOTY_UNINSTALL)
            }
        }
    }

    var timingJob: Job? = null
    var times: Long = 0L
    fun startTimingAlertJob() {
        Heart.timingJob?.cancel()
        timingJob?.cancel()
        timingJob = GlobalScope.launch(Dispatchers.IO) {
            times = 0
            while (true) {
                delay(DBConfig.DAIBOO_POP_TIME_INTERVAL)
                times++
                if (times % DBConfig.DAIBOO_POP_TIME_INTERVAL_TIMES == 0L) {
                    launch(Dispatchers.Main) {
                        startOpen(DaiBooUIItem.Items.getPopList()[0].id, DBConfig.DAIBOO_NOTY_TIME, true)
                    }

                }

                if (times % 2 == 0L) {
                    val manager: BatteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager
                    when (manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)) {
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
//            FireBLogEventUtils.logTanTrigger(tanID)
            val isSuccess = PopCheckHelper.tryPop(workId, tanID)
            if (isSuccess && isListPop) {
                DaiBooUIItem.Items.listPop.removeFirst()
            }
        }

    }


}






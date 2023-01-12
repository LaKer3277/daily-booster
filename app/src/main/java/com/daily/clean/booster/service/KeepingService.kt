package com.daily.clean.booster.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.blankj.utilcode.util.FileUtils
import com.daily.clean.booster.core.CleanData
import com.daily.clean.booster.pop.NotifyTools
import com.daily.clean.booster.entity.DaiBooCleanEvent
import com.daily.clean.booster.utils.DaiBooRAMUtils
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class KeepingService : Service() {

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
        initNotification()
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
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
        notification = NotifyTools.createNotification()
        startForeground(NotifyTools.notificationId, notification)
    }

}






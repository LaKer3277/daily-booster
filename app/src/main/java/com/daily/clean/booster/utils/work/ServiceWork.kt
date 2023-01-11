package com.daily.clean.booster.utils.work

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.daily.clean.booster.pop.NotifyTools
import com.daily.clean.booster.service.KeepingService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture

class ServiceWork(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {

        kotlin.runCatching {
            ContextCompat.startForegroundService(applicationContext, Intent(applicationContext, KeepingService::class.java))
        }
        return Result.success()
    }


    override fun getForegroundInfoAsync(): ListenableFuture<ForegroundInfo> {
        val future = SettableFuture.create<ForegroundInfo>()
        future.set(getForegroundInfo())
        return future
    }

    fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(NotifyTools.notificationId, NotifyTools.createNotification())
    }
}
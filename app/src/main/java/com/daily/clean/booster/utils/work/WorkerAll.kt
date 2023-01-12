package com.daily.clean.booster.utils.work

import android.content.Context
import android.content.Intent
import androidx.work.*
import com.daily.clean.booster.service.KeepingService
import java.util.concurrent.TimeUnit

object WorkerAll {

    fun startImmediatelyWork(ctx: Context) {
        val appCtx = ctx.applicationContext
        val request = OneTimeWorkRequestBuilder<ImmediatelyWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        try {
            WorkManager.getInstance(appCtx)
                .enqueue(request)
        } catch (e: java.lang.Exception) {
            runAppService(appCtx)
        }

        //
        try {
            val workRequest =
                PeriodicWorkRequest.Builder(PeriodicWorker::class.java, 15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(appCtx).enqueueUniquePeriodicWork(
                PeriodicWorker::class.java.name,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        } catch (e: Exception) {}
    }

    class ImmediatelyWorker(val appCtx: Context, workParams: WorkerParameters): Worker(appCtx, workParams) {
        override fun doWork(): Result {
            runAppService(appCtx)
            return Result.success()
        }
    }

    class PeriodicWorker(val appCtx: Context, workParams: WorkerParameters): Worker(appCtx, workParams) {
        override fun doWork(): Result {
            runAppService(appCtx)
            return Result.success()
        }
    }

    private fun runAppService(appCtx: Context) {
        appCtx.startService(Intent(appCtx, KeepingService::class.java))
    }

}
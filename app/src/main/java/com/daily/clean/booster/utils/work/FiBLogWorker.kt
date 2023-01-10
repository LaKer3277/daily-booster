package com.daily.clean.booster.utils.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.daily.clean.booster.base.DBConfig
import com.daily.clean.booster.base.FiBLogEvent
import com.daily.clean.booster.core.pop.DaiBooNotifyPop
import com.daily.clean.booster.core.pop.PopCheckHelper
import com.daily.clean.booster.entity.DaiBooUIItem
import com.daily.clean.booster.utils.LogDB
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FiBLogWorker(appContext: Context, workerParams: WorkerParameters):
       Worker(appContext, workerParams) {
   override fun doWork(): Result {
       // Do the work here--in this case, upload the images.
       // Indicate whether the work finished successfully with the Result
       FiBLogEvent.session_sttt()
       startOpen(DaiBooUIItem.Items.getPopList()[0].id, DBConfig.DAIBOO_NOTY_UNLOCK)
       return Result.success()
   }


    fun startOpen(workId: String, tanID: String) {
        GlobalScope.launch {
            delay(1000)
            val isSuccess = PopCheckHelper.tryPop(workId,tanID)
            if (isSuccess){
                DaiBooUIItem.Items.listPop.removeFirst()
            }
            LogDB.dpop("try pop---workId=${workId}  tanID = ${tanID}  is success? = $isSuccess ")
        }

    }

}

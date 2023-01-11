package com.daily.clean.booster.pop

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.daily.clean.booster.App
import com.daily.clean.booster.base.DBConfig

class NotifyToolReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val workID = intent?.getStringExtra(DBConfig.DAIBOO_KEY_WORK_ID) ?: DBConfig.DAIBOO_WORK_ID_BOOSTER
        val tanId = intent?.getStringExtra(DBConfig.DAIBOO_KEY_NOTY_ID) ?: ""
        if (DBConfig.DAIBOO_ACTION_FROM_POP_NOTY_POP_EXIT == intent?.action ?: "") {
            NotificationManagerCompat.from(App.ins).cancel(DBConfig.NOTIFY_POP_ID)
        }
        if (DBConfig.DAIBOO_ACTION_FROM_POP_NOTY_POP_FULLSCREEN == intent?.action ) {
        }
    }


}
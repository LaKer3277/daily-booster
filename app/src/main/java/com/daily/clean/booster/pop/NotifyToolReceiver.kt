package com.daily.clean.booster.pop

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.daily.clean.booster.base.*

class NotifyToolReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val workID = intent?.getStringExtra(Noty_KEY_WORK) ?: NotyWorkBooster
        val tanId = intent?.getStringExtra(Noty_KEY_SOURCE) ?: ""
        if (DB_ACTION_FROM_POP_NOTY_EXIT == intent?.action ?: "") {
            NotifyManager.cancelAlertNotification()
        }
        if (DB_ACTION_FROM_POP_NOTY_FULLSCREEN == intent?.action ) {
        }
    }


}
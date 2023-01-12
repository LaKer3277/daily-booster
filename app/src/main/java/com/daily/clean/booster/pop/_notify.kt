package com.daily.clean.booster.pop

import android.app.PendingIntent
import android.os.Build
import com.daily.clean.booster.base.*

const val Noty_KEY_WORK = "${DB_NAME}_work_id"
const val Noty_KEY_SOURCE = "${DB_NAME}_pop_id"

const val NotyWorkClean = "Clean"
const val NotyWorkBooster = "Booster"
const val NotyWorkCpu = "CPU"
const val NotyWorkBattery = "Battery"

//tan key
const val NotySourceTime = "pop_time"
const val NotySourceUnlock = "pop_unlock"
const val NotySourceUninstall = "pop_uninstall"
const val NotySourceCharge = "pop_charge"
const val NotySourceBattery = "pop_battery"

const val NOTIFY_CHANNEL_ID_TOOL = "${DB_NAME}_clean_channel"
const val NOTIFY_TOOL_ID = 1000

const val NOTIFY_CHANNEL_ID_POP = "${DB_NAME}_worker_channel"
const val NOTIFY_POP_ID = 1021

val validateImmutableFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
} else PendingIntent.FLAG_UPDATE_CURRENT
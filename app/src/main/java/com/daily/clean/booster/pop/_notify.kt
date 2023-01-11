package com.daily.clean.booster.pop

import com.daily.clean.booster.base.DBConfig

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

const val NOTIFY_CHANNEL_ID_TOOL = "${DBConfig.DAIBOO_SPNAME}_clean_channel"
const val NOTIFY_TOOL_ID = 1000
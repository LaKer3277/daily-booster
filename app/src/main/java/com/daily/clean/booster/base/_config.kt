package com.daily.clean.booster.base

import com.daily.clean.booster.BuildConfig

const val CLEAN_TIME_INTERVAL = 4f * 60 * 1000L// 清理间隔时间

const val DB_USE_TBA = true  //使用TBA 功能
const val DB_USE_FB = true  //使用Firebase 功能


//隐私web地址
const val DB_URL_PRIVACY = "https://sites.google.com/view/dailybooster1/home"
const val DB_URL_TERM = "https://sites.google.com/view/dailyboostertos/home"
const val DB_EMAIL = "mariaktaylor283@gmail.com"
const val DB_APPLICATION_ID = BuildConfig.APPLICATION_ID

//配置名称
const val DB_NAME = "daiboo"


//Intent key
const val DB_KEY_IS_FIRST = "${DB_NAME}_is_first"
const val DB_KEY_FROM = "${DB_NAME}_from"
const val DB_KEY_WEB_URL = "${DB_NAME}_weburl"

const val DB_ACTION_FROM_POP_NOTY = "${DB_NAME}_from_pop"
const val DB_ACTION_FROM_POP_NOTY_EXIT = "${DB_NAME}_from_pop_exit"
const val DB_ACTION_FROM_POP_NOTY_FULLSCREEN = "${DB_NAME}_from_pop_fullscreen"
const val DB_ACTION_FROM_NOTY_RESIDENT = "${DB_NAME}_from_notify"


const val DB_PAGE_FROM_MAIN = "${DB_NAME}_from_main"
const val DB_PAGE_FROM_SPLASH = "${DB_NAME}_from_splash"
const val DB_PAGE_FROM_RESULT = "${DB_NAME}_from_result"
const val DB_PAGE_FROM_FIRST = "${DB_NAME}_from_first"


const val DB_KEY_CLEAN_SIZE = "${DB_NAME}_clean_size"

//弹窗测试数据
const val DB_POP_JSON_TEST = """{
    "up_pop":1,
    "booster_avti":1,
    "ref_fer":1,
    "booster_unl":{
        "first":1,
        "up":40,
        "int":10,
        "siz":2,
        "acti_pos":1
    },
    "booster_time":{
        "first":1,
        "up":50,
        "int":10,
        "siz":2,
        "acti_pos":1
    },
    "booster_uni":{
        "first":1,
        "up":40,
        "int":10,
        "siz":2,
        "acti_pos":1
    },
    "booster_bat":{
        "first":1,
        "up":40,
        "int":10,
        "siz":2,
        "acti_pos":1
    }
}"""
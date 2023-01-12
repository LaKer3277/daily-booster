package com.daily.clean.booster.base

import com.daily.clean.booster.BuildConfig

const val CLEAN_TIME_INTERVAL = 4f * 60 * 1000L// 清理间隔时间

const val DB_TEST_POP_REFER = false
const val DB_USE_AD = true //使用广告功能
const val DB_USE_TBA = true  //使用TBA 功能
const val DB_USE_SHOW_UUID = false  //显示TBA上报的UUID
const val DB_USE_FB = true  //使用Firebase 功能
const val DB_USE_LOG_DEBUG: Boolean = false//日志


//remote 配置 广告
const val DB_REMOTE_NAME_AD = "db_ad_config"
//remote配置 弹窗
const val DB_REMOTE_NAME_POP = "booster_up"

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


const val DB_AD_SHOW_TIMES = "${DB_NAME}_adsht"
const val DB_AD_CLICK_TIMES = "${DB_NAME}_adclit"
const val DB_AD_SHOW_DATE = "${DB_NAME}_adsdate"

//ads key
const val DB_AD_OPEN = "db_Open"
const val DB_AD_RESULT_NV = "result_NV"
const val DB_AD_CLEAN_IV = "clean_IV"

//广告测试数据
const val DB_ADS_JSON_TEST =
    """
{
  "db_Open": [
    {
      "Id": "ca-app-pub-3940256099942544/3419835294",
      "Network": "admob",
      "Form": "open",
      "out": 7200,
      "Hierarchy": 2
    },
    {
      "Id": "ca-app-pub-3940256099942544/3419835294",
      "Network": "admob",
      "Form": "open",
      "out": 7200,
      "Hierarchy": 3
    },
    {
      "Id": "ca-app-pub-3940256099942544/3419835294",
      "Network": "admob",
      "Form": "open",
      "out": 7200,
      "Hierarchy": 1
    }
  ],
  
  "clean_IV": [
    {
      "Id": "ca-app-pub-3940256099942544/1033173712",
      "Network": "admob",
      "Form": "interstitial",
      "out": 3000,
      "Hierarchy": 2
    },
    {
      "Id": "ca-app-pub-3940256099942544/1033173712",
      "Network": "admob",
      "Form": "interstitial",
      "out": 3000,
      "Hierarchy": 1
    },
    {
      "Id": "ca-app-pub-3940256099942544/1033173712",
      "Network": "admob",
      "Form": "interstitial",
      "out": 3000,
      "Hierarchy": 3
    }
  ],
 
  "result_NV": [
    {
      "Id": "ca-app-pub-3940256099942544/2247696110",
      "Network": "admob",
      "Form": "native",
      "out": 3000,
      "Hierarchy": 2
    },
    {
      "Id": "ca-app-pub-3940256099942544/2247696110",
      "Network": "admob",
      "Form": "native",
      "out": 3000,
      "Hierarchy": 1
    },
    {
      "Id": "ca-app-pub-3940256099942544/2247696110",
      "Network": "admob",
      "Form": "native",
      "out": 3000,
      "Hierarchy": 3
    }
  ]
}
    """

//弹窗测试数据
const val DB_POP_JSON_TEST = """{
    "up_pop":1,
    "booster_avti":1,
    "ref_fer":0,
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
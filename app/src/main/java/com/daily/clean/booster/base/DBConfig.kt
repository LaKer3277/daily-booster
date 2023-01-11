package com.daily.clean.booster.base

import com.daily.clean.booster.BuildConfig

object DBConfig {

    const val DAIBOO_POP_TIME_INTERVAL = 10 * 1000L// 心跳间隔
    const val CLEAN_TIME_INTETNAL = 4f * 60 * 1000L// 清理间隔时间
    const val DOCMAN_POP_IN_BG_STAY_TIME = 3 * 60 * 1000L //后台等待十分钟后天生效

    const val DAIBOO_POP_TIME_INTERVAL_TIMES = 3L //  每隔 5次心跳， 发出一次定时弹窗
    const val DAIBOO_POP_IS_CHECK_CONFIG = true
    const val DAIBOO_TEST_POP_REFER = false
    const val DAIBOO_USE_AD = true //使用广告功能
    const val DAIBOO_USE_TBA = true  //使用TBA 功能
    const val DAIBOO_USE_SHOW_UUID = false  //显示TBA上报的UUID
    const val DAIBOO_USE_FB = true  //使用Firebase 功能
    const val DAIBOO_USE_LOG_DUBUG: Boolean = false//日志

    //测试数据
//    const val DAIBOO_POP_TIME_INTERVAL_TIMES = 3L // 每2次 发出
//    const val DAIBOO_POP_IS_CHECK_CONFIG = false
//    const val DAIBOO_TEST_POP_REFER = true
//    const val DAIBOO_USE_AD = false
//    const val DAIBOO_USE_TBA = false  //使用TBA 功能
//    const val DAIBOO_USE_SHOW_UUID = false  //显示TBA上报的UUID
//    const val DAIBOO_USE_FB = false  //使用Firebase 功能
//    const val DAIBOO_USE_LOG_DUBUG: Boolean = true//日志


    //    remote 配置 广告
    const val DAIBOO_REMOTE_NAME_AD = "db_ad_config"
//    const val DAIBOO_REMOTE_NAME_AD = "db_ad_config_test" //测试

    //remote配置 弹窗
    const val DAIBOO_REMOTE_NAME_POP = "booster_up"
//    const val DAIBOO_REMOTE_NAME_POP = "test_booster_up"

//    const val DAIBOO_REMOTE_NAME_SCANIV_REFER = "dc_scan_refer"
//    const val DAIBOO_REMOTE_NAME_BACKIV_REFER = "BackIV_refer"
//    const val DAIBOO_REMOTE_NAME_OPEN_CONTROL = "open_control"

    //隐私web地址
    const val DAIBOO_URL_PRIVACY = "https://sites.google.com/view/dailybooster1/home"
    const val DAIBOO_URL_TERM = "https://sites.google.com/view/dailyboostertos/home"
    const val DAIBOO_EMAIL = "mariaktaylor283@gmail.com"
    const val DAIBOO_APPLICATION_ID = BuildConfig.APPLICATION_ID

    //配置名称
    const val DAIBOO_SPNAME = "daiboo"

    const val DAIBOO_WORK_ID_CLEAN = "Clean"
    const val DAIBOO_WORK_ID_BOOSTER = "Booster"
    const val DAIBOO_WORK_ID_CPU = "CPU"
    const val DAIBOO_WORK_ID_BATTERY = "Battery"
    const val DAIBOO_WORK_ID_ClEAN_NOTIFICATION = "Notification"


    //Intent key
    const val DAIBOO_KEY_WORK_ID = "${DAIBOO_SPNAME}_work_id"
    const val DAIBOO_KEY_NOTY_ID = "${DAIBOO_SPNAME}_pop_id"
    const val DAIBOO_KEY_IS_FIRST = "${DAIBOO_SPNAME}_is_first"
    const val DAIBOO_KEY_FROM = "${DAIBOO_SPNAME}_from"
    const val DAIBOO_KEY_WEB_URL = "${DAIBOO_SPNAME}_weburl"

    const val DAIBOO_ACTION_FROM_POP_NOTY_POP = "${DAIBOO_SPNAME}_from_pop"
    const val DAIBOO_ACTION_FROM_POP_NOTY_POP_EXIT = "${DAIBOO_SPNAME}_from_pop_exit"
    const val DAIBOO_ACTION_FROM_POP_NOTY_POP_FULLSCREEN = "${DAIBOO_SPNAME}_from_pop_fullscreen"
    const val DAIBOO_ACTION_FROM_NOTIFYTOOL = "${DAIBOO_SPNAME}_from_notify"
    const val DAIBOO_ACTION_FROM_NOTIFY_NM = "${DAIBOO_SPNAME}_from_notify_manager"
    const val DAIBOO_ACTION_FROM_MAIN = "${DAIBOO_SPNAME}_from_main"
    const val DAIBOO_ACTION_FROM_SPLASH = "${DAIBOO_SPNAME}_from_splash"
    const val DAIBOO_ACTION_FROM_RESUTL = "${DAIBOO_SPNAME}_from_result"
    const val DAIBOO_ACTION_FROM_FIRST = "${DAIBOO_SPNAME}_from_first"


    const val DAIBOO_KEY_CLEAN_SIZE = "${DAIBOO_SPNAME}_clean_size"

    const val NOTIFY_CHANNEL_ID_POP = "${DAIBOO_SPNAME}_worker_channel"
    const val NOTIFY_POP_ID = 1021

    const val NOTIFY_CHANNEL_NM = "Notification_Clean_Channel"
    const val NOTIFY_NM_ID = 1056


    const val DAIBOO_AD_SHOW_TIMES = "${DAIBOO_SPNAME}_adsht"
    const val DAIBOO_AD_CLICK_TIMES = "${DAIBOO_SPNAME}_adclit"
    const val DAIBOO_AD_SHOW_DATE = "${DAIBOO_SPNAME}_adsdate"
    const val DAIBOO_AD_LAST_SHOW_HOME_TIME = "${DAIBOO_SPNAME}_l_sadtim"


    //tan key
    const val DAIBOO_NOTY_TIME = "pop_time"
    const val DAIBOO_NOTY_UNLOCK = "pop_unlock"
    const val DAIBOO_NOTY_UNINSTALL = "pop_uninstall"
    const val DAIBOO_NOTY_CHARGE = "pop_charge"
    const val DAIBOO_NOTY_BATTERY = "pop_battery"
    const val DAIBOO_NOTY_RAM = "pop_ram"


    //ads key
    const val DAIBOO_AD_OPEN = "db_Open"
    const val DAIBOO_AD_RESULT_NV = "result_NV"
    const val DAIBOO_AD_CLEAN_IV = "clean_IV"

    //广告测试数据
    const val DAIBOO_ADS_JSON_TEST =
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
    const val DAIBOO_POP_JSON_TEST = """{
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
}
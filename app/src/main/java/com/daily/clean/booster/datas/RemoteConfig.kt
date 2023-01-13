package com.daily.clean.booster.datas

import com.daily.clean.booster.R
import com.daily.clean.booster.ads.conf.LoaderConf
import com.daily.clean.booster.base.DB_POP_JSON_TEST
import com.daily.clean.booster.entity.DaiBooPopBean
import com.daily.clean.booster.ext.json2Pop
import com.daily.clean.booster.isDebugMode
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

class RemoteConfig {

    companion object {
        val ins: RemoteConfig by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            RemoteConfig()
        }
        // 弹窗数据
        var daiBooPopBean: DaiBooPopBean? = null
    }

    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        if (isDebugMode) {
            val configSetting = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(60)
                .build()
            remoteConfig.setConfigSettingsAsync(configSetting)
        }
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    fun fetchInit() {
        LoaderConf.tryParseLocal()
        daiBooPopBean = DB_POP_JSON_TEST.json2Pop()

        remoteConfig.fetchAndActivate().addOnCompleteListener {
            if (it.isSuccessful) {
                LoaderConf.tryParseRelease()
                //NotifyCenter.ins.initNotifyConf()
                val popConf = getPopConfig()
                if (popConf.isNullOrEmpty()) return@addOnCompleteListener
                daiBooPopBean = getPopConfig().json2Pop()
            }
        }
    }

    fun getAdsConfig(): String {
        return remoteConfig.getString(if (isDebugMode) "db_ad_config_test" else "db_ad_config")
    }

    fun getPopConfig(): String {
        return remoteConfig.getString(if (isDebugMode) "booster_up_test" else "booster_up")
    }

}
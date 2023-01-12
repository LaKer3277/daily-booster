package com.daily.clean.booster.base

import com.daily.clean.booster.R
import com.daily.clean.booster.ad.DaiBooADUtil
import com.daily.clean.booster.entity.DaiBooAdAllBean
import com.daily.clean.booster.entity.DaiBooPopBean
import com.daily.clean.booster.utils.LogDB
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


object FiBRemoteUtil {

    var isInBackList = false
    var isfacebookUser = false
    var isByuser = false

    var referrerConfig = 0L
    var ScanIV_refer = 3L
    var BackIV_refer = 3L

    //    var speedInstallCon: SpeedInstallCon? = null
//    var adltvOneDayConBean: SpeedAdltvOneDayCon? = null
    var ua: String = ""

    // 弹窗数据
    var daiBooPopBean: DaiBooPopBean? = null


    fun updateAllData(tag: String) {
        updateADData(tag)
        updaePopData(tag)
        //TODO 其他
//        ref_fer = mRemoteConfig.getLong(DAIBOO_REMOTE_NAME_REF_FER)
//        LogDB.i("CONFIG-ref_fer---$tag----json=$ref_fer")
//        ScanIV_refer = mRemoteConfig.getLong(MainConfig.DAIBOO_REMOTE_NAME_SCANIV_REFER)
//        LogDM.i("CONFIG-ScanIV_refer----$tag----json=$ScanIV_refer")
//        BackIV_refer = mRemoteConfig.getLong(MainConfig.DAIBOO_REMOTE_NAME_BACKIV_REFER)
//        LogDM.i("CONFIG-BackIV_refer----$tag----json=$BackIV_refer")
//        open_control = mRemoteConfig.getString(MainConfig.DAIBOO_REMOTE_NAME_OPEN_CONTROL).Json2OpenControl()
//        LogDM.i("CONFIG-open_control----$tag----toBean=$open_control")
    }


    val mRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance().apply {
            setDefaultsAsync(R.xml.remote_config_defaults)
            setConfigSettingsAsync(remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            })
        }
    }

    fun initFireBaseData(next: () -> Unit = {}) {

        if (DB_USE_FB.not()) {
            //TODO 本地数据
            DaiBooADUtil.baseAD = DB_ADS_JSON_TEST?.Json2AD()
            LogDB.eAD("CONFIG-ADS-toBean=${DaiBooADUtil.baseAD}")

            daiBooPopBean = DB_POP_JSON_TEST?.Json2TAN()
            LogDB.eAD("CONFIG-POP-toBean=$daiBooPopBean")

            next()
            return
        }

        GlobalScope.launch(Dispatchers.Main) {
            mRemoteConfig.activate().addOnCompleteListener {
                if (it.isSuccessful) {
                    updateAllData("local")
                    next()
                    fetchRemoteConfig()
                }
            }
        }
    }


    private fun fetchRemoteConfig() {
        GlobalScope.launch {
            delay(100)
            mRemoteConfig.fetchAndActivate().addOnCompleteListener {
                if (it.isSuccessful) {
                    LogDB.d("CONFIG-Remote---${it.result}")
                    updateAllData("fetch")
                }
            }
        }
    }

    private fun updateADData(log: String) {
        val jsonAd = mRemoteConfig.getString(DB_REMOTE_NAME_AD)
        LogDB.e("CONFIG-ADS---$log---json=$jsonAd")
        DaiBooADUtil.baseAD = jsonAd?.Json2AD()
        LogDB.e("CONFIG-ADS---$log-toBean=${DaiBooADUtil.baseAD}")
    }

    fun updaePopData(tag: String) {
        //弹窗数据解析
        val popJson = mRemoteConfig.getString(DB_REMOTE_NAME_POP)
        LogDB.i("CONFIG-POP----$tag----${DB_REMOTE_NAME_POP}=$popJson")
        daiBooPopBean = popJson.Json2TAN()
        LogDB.e("CONFIG-POP---$tag-toBean=$daiBooPopBean")
    }

    fun isPop_refer(): Boolean {
        if (DB_TEST_POP_REFER) {
            isByuser = true
            isfacebookUser = false
//            ref_fer = 3L
        }
        val ref_fer = daiBooPopBean?.ref_fer ?: 1
        LogDB.e("CONFIG-USER=${isfacebookUser} $isByuser   $isInBackList  ${daiBooPopBean?.ref_fer}")
        return when (ref_fer) {
            0 -> true
            1 -> isByuser
            2 -> isfacebookUser
            else -> false
        }
    }

    fun isCan_ScanIV_refer(): Boolean {

//        if (ConOpt.DAIBOO_TEST_SCANIV_REFER) {
//            isByuser = false
//            isfacebookUser = true
//            ScanIV_refer = 3L
//        }

//        LogClnopt.e("CONFIG-USER=${isfacebookUser} $isByuser   $isInBackList")
        return when (ScanIV_refer) {
            0L -> true
            1L -> false
            2L -> isfacebookUser
            3L -> isByuser
            else -> false
        }
    }

    fun isCan_BackIV_refer(): Boolean {
        LogDB.e("CONFIG-USER=$isfacebookUser $isByuser  $isInBackList")
//        if (ConOpt.DAIBOO_TEST_BACKIV_REFER) {
//            isByuser = false
//            isfacebookUser = true
//            BackIV_refer = 0L
//        }
        return when (BackIV_refer) {
            0L -> true
            1L -> false
            2L -> isfacebookUser
            3L -> isByuser
            else -> false
        }
    }


    fun String.Json2AD(): DaiBooAdAllBean? {
        if (this.isNullOrEmpty()) return null
        return try {
            Gson().fromJson(this, DaiBooAdAllBean::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun String.Json2TAN(): DaiBooPopBean? {
        if (this.isNullOrEmpty()) return null
        return try {
            Gson().fromJson(this, DaiBooPopBean::class.java)
        } catch (e: Exception) {
            null
        }
    }


}
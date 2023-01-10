package com.daily.clean.booster.ad


import androidx.lifecycle.lifecycleScope
import com.daily.clean.booster.DBApp
import com.daily.clean.booster.base.BaseActivity
import com.daily.clean.booster.base.DBConfig
import com.daily.clean.booster.base.FiBLogEvent
import com.daily.clean.booster.entity.DaiBooAdAllBean
import com.daily.clean.booster.entity.DaiBooAdItemBean
import com.daily.clean.booster.utils.DaiBooMK
import com.daily.clean.booster.utils.LogDB
import com.files.document.ad.DaiBooNatMaxImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

object DaiBooADUtil {


    var baseAD: DaiBooAdAllBean? = null

    var adShowTimes: Int = 0

    var adClickTimes: Int = 0


    /**
     * 是否达到 广告显示当限制
     */
    fun isReachLimit(): Boolean {

        if (DBConfig.DAIBOO_USE_AD.not()) return true

        if (baseAD == null) {
            LogDB.dAD("---Limit null")
            return true
        }
        return false
    }


    private val isLoading: MutableMap<String, Boolean> by lazy {
        mutableMapOf(
            Pair(DBConfig.DAIBOO_AD_OPEN, false),
            Pair(DBConfig.DAIBOO_AD_RESULT_NV, false),
            Pair(DBConfig.DAIBOO_AD_CLEAN_IV, false),
        )
    }

    private val loadStartTime: MutableMap<String, Long> by lazy {
        mutableMapOf(
            Pair(DBConfig.DAIBOO_AD_OPEN, 0L),
            Pair(DBConfig.DAIBOO_AD_RESULT_NV, 0L),
            Pair(DBConfig.DAIBOO_AD_CLEAN_IV, 0L),
        )
    }

    private val cache: MutableMap<String, MutableList<DaiBooBaseAD>> by lazy {
        mutableMapOf(
            Pair(DBConfig.DAIBOO_AD_OPEN, mutableListOf()),
            Pair(DBConfig.DAIBOO_AD_CLEAN_IV, mutableListOf()),
            Pair(DBConfig.DAIBOO_AD_RESULT_NV, mutableListOf()),
        )
    }

    private val showingAD: MutableMap<String, DaiBooBaseAD?> by lazy {
        mutableMapOf(
            Pair(DBConfig.DAIBOO_AD_OPEN, null),
            Pair(DBConfig.DAIBOO_AD_CLEAN_IV, null),
            Pair(DBConfig.DAIBOO_AD_RESULT_NV, null),
        )
    }

    fun getShowingAD(key: String): DaiBooBaseAD? {
        return showingAD[key]
    }


    fun getlistByKey(key: String): MutableList<DaiBooAdItemBean>? {
        return when (key) {
            DBConfig.DAIBOO_AD_OPEN -> baseAD?.db_Open
            DBConfig.DAIBOO_AD_RESULT_NV -> baseAD?.result_NV
            DBConfig.DAIBOO_AD_CLEAN_IV -> baseAD?.clean_IV
            else -> {
                null
            }
        }
    }

    private fun getCacheAD(key: String): DaiBooBaseAD? {
        var list = cache[key]
        list?.let {
            var iter = list.iterator()
            while (iter.hasNext()) {
                var baseAd = iter.next()
                if (baseAd.isAdAvailable()) {
                    return baseAd
                } else {
                    iter.remove()
                }
            }
        }
        return null
    }

    /**
     * 加载广告
     * 会判断是否有缓存，有 ->直接返回
     * @param key 广告类型
     */
    fun load(
        key: String,
        activity: BaseActivity,
        callLoading: () -> Unit = {},
        callEnd: () -> Unit = {},
        callBack: (DaiBooBaseAD) -> Unit = {}
    ) {

        if (!isInit) {
            iniCountDate()
        }


        if (isReachLimit()) {
            LogDB.dAD("--$key----load---limit")
            return
        }

//        if (key == MainConfig.DAIBOO_AD_SCAN_INT && FireBRemoteUtil.isCan_ScanIV_refer().not()) {
//            LogDM.dAD("--$key----load---can not load--")
//            return
//        }

        //获取缓存数据
        var cacheAD = getCacheAD(key)
        if (cacheAD != null) {
            LogDB.dAD("--$key----load---cache")
            callBack.invoke(cacheAD)
            return
        }

        val list = getlistByKey(key)
        if (list == null) {
            LogDB.dAD("--$key----load---list is null")
            return
        }
        list.sortByDescending { it.Hierarchy }
        if (isLoading[key] == false || System.currentTimeMillis() - loadStartTime[key]!! > 60 * 1000L) {
            isLoading[key] = true
            val startTime = Date().time
            loadStartTime[key] = startTime
            loadAD(activity, key, 0, list, startTime, callBack, callEnd)
        } else {
            LogDB.dAD("--$key----load---isLoading")
            callLoading()

        }

    }


    /**
     * 预加载，不判断是否有缓存
     */
//    fun preLoad(key: String, callBack: (BaseAD) -> Unit) {
//
//        if (isReachLimit()) {
//            CHLog.dAD("--$key----preLoad---limit")
//            return
//        }
//        val list = when (key) {
//            AD_OPEN -> baseAD?.prisec_kak ?: mutableListOf()
//            AD_HOME -> baseAD?.prisec_lqx ?: mutableListOf()
//            AD_RESULT -> baseAD?.prisec_ppq ?: mutableListOf()
//            AD_CON -> baseAD?.prisec_isx ?: mutableListOf()
//            AD_BACK -> baseAD?.prisec_mfc ?: mutableListOf()
//            else -> {
//                return
//            }
//        }?.apply {
//            sortByDescending {
//                it.nbm
//            }
//        }
//
//        if (isLoading[key] == false) {
//            isLoading[key] = true
//            loadAD(key, 0, list, callBack)
//        } else {
//            CHLog.dAD("--$key----preLoad---isLoading")
//        }
//
//    }


    private fun loadAD(
        activity: BaseActivity,
        key: String,
        index: Int,
        ads: MutableList<DaiBooAdItemBean>?,
        startTime: Long = 0,
        callBack: (DaiBooBaseAD) -> Unit = {},
        callEnd: () -> Unit = {},
    ) {

        if (ads.isNullOrEmpty()) {
            LogDB.dAD("--$key----load---no data")
            isLoading[key] = false
            callEnd()
            return
        }

        if (index >= ads.size) {
            isLoading[key] = false
            //瀑布流请求完成
            LogDB.eAD("--$key----load---瀑布了结束")
            FiBLogEvent.ad_request(key, (Date().time - startTime) / 1000, false)
            callEnd()
        } else {

            val ad: DaiBooAdItemBean = ads[index]
            if (ad == null) {
                //失败，请求下一个
                loadAD(activity, key, index + 1, ads, startTime, callBack, callEnd)
                return
            }
            val baseAd = when (ad.getFormat()) {
                "open" -> if (ad.isMax()) DaiBooOpenMaxImpl(activity, key, ad) else DaiBooOpInImpl(key, ad)
                "interstitial" -> if (ad.isMax()) DaiBooOpenMaxImpl(activity, key, ad) else DaiBooOpInImpl(key, ad)
                "native" -> if (ad.isMax()) DaiBooNatMaxImpl(activity, key, ad) else DaiBooNatImpl(key, ad)
                else -> {
                    isLoading[key] = false
                    return
                }
            }
            LogDB.dAD("--$key----load---start $index")
            baseAd?.load({
                LogDB.dAD("--$key----load---success")
                isLoading[key] = false
                //加载成功后 添加缓存
                cache[key]?.add(it)
                LogDB.dAD("--$key----load---add cache--- ${cache[key]?.size}")
                callBack(it)
                FiBLogEvent.ad_request(key, (Date().time - startTime) / 1000, true)
            }, {
                //失败，请求下一个
                loadAD(activity, key, index + 1, ads, startTime, callBack, callEnd)
            })
        }
    }

    /**
     * 显示 广告
     */

    fun showAD(
        key: String,
        activity: BaseActivity,
        workId: String = "",
        block: (Boolean) -> Unit
    ) {

        if (activity.isPause || DBApp.isAdActivityResume) {
            LogDB.dAD("--$key----show---ishowing-- ${DBApp.isAdActivityResume}")
            return
        }

//        if (key == ConOpt.DAIBOO_AD_BACK && FireBRemoteUtil.isCan_BackIV_refer().not()) {
//            LogClnopt.dAD("--$key----load---can not load BackIV_refer--")
//            block(false)
//            return
//        }
//        if (key == MainConfig.DAIBOO_AD_SCAN_INT && FireBRemoteUtil.isCan_ScanIV_refer().not()) {
//            LogDM.dAD("--$key----show---can not show--")
//            block(false)
//            return
//        }

        if (isReachLimit()) {
            LogDB.dAD("--$key----show---cancel-return-- onPause")
            block(false)
            return
        }
        val baseAd = getCacheAD(key)
        if (baseAd == null) {
            LogDB.dAD("--$key----show---cancel-null--")
            block(false)
        } else {

            FiBLogEvent.ad_chance(key)

            baseAd.show(activity, object : ClnoptADCallBack {

                override fun onAdShowed(result: Boolean) {
                    if (result) {
                        addShowTimes()
                        if (baseAd.adItem.isNative()) {
                            showingAD[key]?.destroy()
                            showingAD[key] = baseAd
                            cache[key]?.remove(baseAd)
                            block(result)
                        }
                    } else {
                        LogDB.dAD("--$key----show---false:error")
                        block(false)
                        return
                    }

                }

                override fun onAdDismiss(baseAd: DaiBooBaseAD?) {
                    if (baseAd?.adItem?.isOpenOrInter() == true) {
                        //显示之后删除缓存
                        cache[key]?.remove(baseAd)
                        LogDB.dAD("--$key----show---remove cache--${cache[key]?.size}")
                        block(true)
                    }
                }

                override fun onAdClicked(baseAd: DaiBooBaseAD?) {
                    addClickTimes()
                }

            })
        }

    }


    fun addShowTimes() {
        adShowTimes++
        DaiBooMK.encode(DBConfig.DAIBOO_AD_SHOW_TIMES, adShowTimes)
    }

    fun addClickTimes() {
        adClickTimes++
        DaiBooMK.encode(DBConfig.DAIBOO_AD_CLICK_TIMES, adClickTimes)
    }

    private fun initLimt() {
        adShowTimes = DaiBooMK.decode(DBConfig.DAIBOO_AD_SHOW_TIMES, 0)
        adClickTimes = DaiBooMK.decode(DBConfig.DAIBOO_AD_CLICK_TIMES, 0)
        LogDB.dAD("initLimt...$adShowTimes  $adClickTimes")
    }

    private fun reSetLimit() {
        LogDB.dAD("reSetLimit...")
        adShowTimes = 0
        adClickTimes = 0
        DaiBooMK.encode(DBConfig.DAIBOO_AD_SHOW_TIMES, 0)
        DaiBooMK.encode(DBConfig.DAIBOO_AD_CLICK_TIMES, 0)
    }


    /**
     * 检查广告 限制日期
     */
    var isInit: Boolean = false
    fun iniCountDate() {
        //检查广告展示时间 是否在同一天
        val showDateOld: String = DaiBooMK.decode(DBConfig.DAIBOO_AD_SHOW_DATE, "")
        val showDateNew: String = SimpleDateFormat("yyyy-MM-dd").format(Date())
        LogDB.dAD("checkDate.old=$showDateOld  new=$showDateNew")
        //是同一天
        if (showDateOld == showDateNew) {
            initLimt()
        } else {
            DaiBooMK.encode(DBConfig.DAIBOO_AD_SHOW_DATE, showDateNew)
            reSetLimit()
        }
        isInit = true
    }


    fun isCacheAd(key: String): Boolean {
        val baseAd = getCacheAD(key)
        return null != baseAd
    }


}
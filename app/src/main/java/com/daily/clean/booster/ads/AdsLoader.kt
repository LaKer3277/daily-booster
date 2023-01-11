package com.daily.clean.booster.ads

import android.content.Context
import android.util.Log
import com.daily.clean.booster.ads.conf.AdConf
import com.daily.clean.booster.ads.conf.AdPos
import com.daily.clean.booster.ads.conf.LoaderConf
import com.daily.clean.booster.ads.loader.AdmobLoader
import com.daily.clean.booster.ads.model.BaseAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


object AdsLoader: LoaderConf(), CoroutineScope by MainScope() {

    const val adTag = "AdsLoader"

    private val syncRequesting = HashMap<String, String>()
    private val _ads = hashMapOf<String, ArrayList<BaseAd>>()

    private fun getCache(pos: AdPos): BaseAd? {
        val ads = _ads[pos.adPos] ?: return null
        if (ads.isNullOrEmpty()) return null
        var ad: BaseAd? = null
        for (i in ads.size - 1 downTo 0) {
            ad = ads.removeAt(i)
            if (!ad.isExpired()) break
        }
        Log.e(adTag, "$pos getCache:$ad, container: $_ads")
        return ad
    }

    private fun isCached(pos: AdPos): Boolean {
        val ads = _ads[pos.adPos] ?: return false
        if (ads.isNullOrEmpty()) return false
        var validCache = false
        for (i in ads.size - 1 downTo 0) {
            validCache = !ads[i].isExpired()
            if (validCache) break
        }
        return validCache
    }

    @Synchronized
    fun add2Cache(pos: AdPos, ad: BaseAd) {
        var list = _ads[pos.adPos]
        if (list.isNullOrEmpty()) {
            list = arrayListOf()
            _ads[pos.adPos] = list
        }
        list.add(ad)
        Collections.sort(list, object : Comparator<BaseAd> {
            override fun compare(o1: BaseAd?, o2: BaseAd?): Int {
                return (o1?.adConf?.Hierarchy?.compareTo(o2?.adConf?.Hierarchy ?: 0) ?: 0) * -1
            }
        })
    }

    fun preloadAd(ctx: Context, adPos: AdPos) {
        if (isCached(adPos)) {
            Log.i(adTag, "$adPos preloadAd but cached")
            return
        }
        Log.i(adTag, "$adPos preloadAd")

        loadAd(ctx.applicationContext, adPos, object : AdsListener() {
            override fun onLoaded(ad: BaseAd) {
                add2Cache(adPos, ad)
            }
        }, onlyCache = false)
    }

    /**
     * @param ctx 可能存在load 了之后又没有及时展示的广告,这里除激励视频的加载之外，统一使用applicationContext
     * @param onlyCache 仅使用缓存,如果没有缓存就报错
     * @param newLoad 不论当前是否正在请求广告,都一律重新请求
     * */
    fun loadAd(
        ctx: Context,
        adPos: AdPos,
        adsListener: AdsListener,
        onlyCache: Boolean = false,
        newLoad: Boolean = false) {
        Log.i(adTag, "$adPos loadAd")

        val cache = getCache(adPos)
        if (cache != null) {
            cache.redefineListener(adsListener)
            adsListener.onLoaded(cache)
            return
        }
        if (onlyCache) {
            adsListener.onError("No Cache")
            return
        }
        if (!newLoad) {
            synchronized(syncRequesting) {
                if (syncRequesting[adPos.adPos] != null) return
            }
        }
        val configs = configByPos(adPos)
        if (configs.ads.isNullOrEmpty()) {
            adsListener.onError("No conf")
            return
        }
        val idL = arrayListOf<AdConf>()
        idL.addAll(configs.ads)
        launch {
            innerLoad(ctx.applicationContext, adPos, idL, adsListener)
        }
    }

    private var admobLoader: AdmobLoader? = null
    private fun innerLoad(ctx: Context, adPos: AdPos, idList: ArrayList<AdConf>, adsListener: AdsListener) {
        if (idList.isNullOrEmpty()) {
            adsListener.onError()
            return
        }

        val config = idList.removeAt(0)
        val (source, weight, type, id) = config
        val loader = when (source) {
            "admob" -> {
                if (admobLoader == null) admobLoader = AdmobLoader()
                admobLoader
            }

            /*"min" -> {
                if (mintegralLoader == null) mintegralLoader = MintegralLoader()
                mintegralLoader
            }*/

            else -> {
                adsListener.onError("$source not supported!")
                return
            }
        }

        fun putImpressJson(ad: BaseAd?) {
            /*ad?.adConf?.impressJson?.apply {
                val adId = if ("admob" == ad.adConf.source) ad.adConf.id else ad.adConf.placementId
                put("eianz_p", "ad_impression")
                //put("iemzc_q", "")
                //put("oiwz_v", "USD")
                put("ioez_w", ad.adConf.source)
                put("kjs_r", adId)
                put("mksz_t", adPos)
                put("oiem_y", adId)
                put("iunnz_u", ad.adConf.format)
                put("kune_i", "")
                put("ewaz_a", 0)
                put("kijn_s", ImpressionUp.getCurIp())
                put("omcv_f", "ss")
            }*/
        }

        fun checkResult(ad: BaseAd? = null) {
            synchronized(syncRequesting) {
                syncRequesting.remove(adPos.adPos)
            }
            putImpressJson(ad)
            Log.e(adTag, "$adPos requested ad: $ad")
            if (ad == null) {
                val cache = getCache(adPos)
                if (cache != null) {
                    adsListener.onLoaded(cache)
                    return
                }
                innerLoad(ctx, adPos, idList, adsListener)
                return
            }
            adsListener.onLoaded(ad)
        }

        when (type) {
            "open" -> {
                Log.i(adTag, "$adPos loadOpen: ${config.Hierarchy} ${config.Id}")
                syncRequesting[adPos.adPos] = adPos.adPos
                loader?.loadOpen(ctx, adPos, config, adsListener) {
                    checkResult(it)
                }
            }

            "inter" -> {
                Log.i(adTag, "$adPos loadInterstitial: ${config.Hierarchy} ${config.Id}")
                syncRequesting[adPos.adPos] = adPos.adPos
                loader?.loadInterstitial(ctx, adPos, config, adsListener) {
                    checkResult(it)
                }
            }

            "native" -> {
                Log.i(adTag, "$adPos loadNative: ${config.Hierarchy} ${config.Id}")
                syncRequesting[adPos.adPos] = adPos.adPos
                loader?.loadNative(ctx, adPos, config, adsListener) {
                    checkResult(it)
                }
            }
        }
    }


}
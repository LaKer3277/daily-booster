package com.daily.clean.booster.ad.mode

import androidx.appcompat.app.AppCompatActivity
import com.daily.clean.booster.ad.base.IAdShowCallBack
import com.daily.clean.booster.ad.base.BaseLoader
import com.daily.clean.booster.appIns
import com.daily.clean.booster.base.FirebaseEvent
import com.daily.clean.booster.entity.DaiBooAdEvent
import com.daily.clean.booster.entity.AdConf
import com.daily.clean.booster.tba.HttpTBA
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.*


class DaiBooOpInImpl(var tag: String, conf: AdConf) : BaseLoader(conf) {
    //    var mAd_inter: InterstitialAd? = null
//    var mAd_open: AppOpenAd? = null
    var mAd: Any? = null

    private fun getAdRequest() = AdRequest.Builder().build();

    override fun load(success: (BaseLoader) -> Unit, failed: () -> Unit) {

        adItem?.let {
            when (it.getFormat()) {
                "interstitial" -> {

                    InterstitialAd.load(
                        appIns,
                        adItem.Id,
                        getAdRequest(),
                        object : InterstitialAdLoadCallback() {
                            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                                //                                this@ADProsecImpl.mAd_inter = interstitialAd
                                mAd = interstitialAd
                                this@DaiBooOpInImpl.loadTime = Date().time
                                success.invoke(this@DaiBooOpInImpl)
                                adEvent = DaiBooAdEvent(
                                    tag,
                                    getNetWork(interstitialAd),
                                    daiboo_ad_item = adItem
                                )

                                interstitialAd.setOnPaidEventListener { v ->
                                    adEvent?.daiboo_value_micros = (v.valueMicros)
                                    adEvent?.daiboo_precision_type = "${v.precisionType}"
                                }
                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                failed.invoke()
                            }
                        })
                }
                "open" -> {
                    AppOpenAd.load(
                        appIns,
                        adItem.Id,
                        getAdRequest(),
                        AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                        object : AppOpenAd.AppOpenAdLoadCallback() {
                            override fun onAdLoaded(openAD: AppOpenAd) {
                                //                                mAd_open = openAD
                                mAd = openAD
                                loadTime = Date().time
                                success.invoke(this@DaiBooOpInImpl)
                                adEvent = DaiBooAdEvent(
                                    tag,
                                    daiboo_ad_network = getNetWork(openAD),
                                    daiboo_ad_item = adItem
                                )

                                openAD.setOnPaidEventListener { v ->
                                    adEvent?.daiboo_value_micros = (v.valueMicros)
                                    adEvent?.daiboo_precision_type = "${v.precisionType}"
                                }
                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                failed.invoke()
                            }
                        })
                }
            }
        }


    }

    /**
     * Shows the ad if one isn't already showing.
     */
    override fun show(activity: AppCompatActivity, callback: IAdShowCallBack?) {
        if (isAdAvailable()) {
            val fullScreenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        mAd = null
                        callback?.onAdDismiss(this@DaiBooOpInImpl)
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        mAd = null
                        callback?.onAdShowed(false)
                    }

                    override fun onAdShowedFullScreenContent() {
                        callback?.onAdShowed(true)
                    }

                    override fun onAdClicked() {
                        callback?.onAdClicked(this@DaiBooOpInImpl)
                        FirebaseEvent.ad_click(tag)
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()

                        adEvent?.let {
                            HttpTBA.doReportAd(adEvent = it)
                        }
                        FirebaseEvent.dm_ad_impression(tag)

                    }
                }

            if (mAd is InterstitialAd) {
                (mAd as InterstitialAd).fullScreenContentCallback = fullScreenContentCallback
                (mAd as InterstitialAd).show(activity)
            } else if (mAd is AppOpenAd) {
                (mAd as AppOpenAd).fullScreenContentCallback = fullScreenContentCallback
                (mAd as AppOpenAd).show(activity)
            }
        } else {
            callback?.onAdDismiss(this@DaiBooOpInImpl)
        }
    }


    /**
     * Utility method that checks if ad exists and can be shown.
     */
    override fun isAdAvailable(): Boolean {
        return mAd != null && wasTimeIn(3000)
    }

    /**
     * 获取
     * 广告平台: admob, facebook, applovin, 穿山甲，广点通
     */
    fun getNetWork(ad: Any): String {
        if (ad == null) return "admob"

        return when (ad) {
            is InterstitialAd -> {
                if (ad.responseInfo.mediationAdapterClassName != null && "com.google.ads.mediation.admob.AdMobAdapter" != ad.responseInfo.mediationAdapterClassName) {
                    ad.responseInfo.mediationAdapterClassName ?: "admob"
                } else {
                    "admob"
                }
            }
            is AppOpenAd -> {
                if (ad.responseInfo.mediationAdapterClassName != null && "com.google.ads.mediation.admob.AdMobAdapter" != ad.responseInfo.mediationAdapterClassName) {
                    ad.responseInfo.mediationAdapterClassName ?: "admob"
                } else {
                    "admob"
                }
            }
            else -> {
                "admob"
            }
        }


    }


}
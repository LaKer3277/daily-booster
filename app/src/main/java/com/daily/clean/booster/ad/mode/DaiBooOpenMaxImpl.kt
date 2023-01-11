package com.daily.clean.booster.ad.mode

import androidx.appcompat.app.AppCompatActivity
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.daily.clean.booster.ad.base.IAdShowCallBack
import com.daily.clean.booster.ad.base.BaseLoader
import com.daily.clean.booster.base.FiBLogEvent
import com.daily.clean.booster.entity.DaiBooAdEvent
import com.daily.clean.booster.entity.AdConf
import com.daily.clean.booster.tba.HttpTBA
import com.daily.clean.booster.utils.LogDB
import java.util.*

class DaiBooOpenMaxImpl(var activity: AppCompatActivity, var tag: String, conf: AdConf) : BaseLoader(conf) {

    private var retryAttempt = 0.0
    private lateinit var interstitialAd: MaxInterstitialAd
    var mAd: MaxAd? = null
    var mAdShowCallBack: IAdShowCallBack? = null

    //
    override fun show(activity: AppCompatActivity, callback: IAdShowCallBack?) {
        if (isAdAvailable().not()) return
        mAdShowCallBack = callback
        interstitialAd?.run {
            showAd(tag)
        }
    }

    override fun load(success: (BaseLoader) -> Unit, failed: () -> Unit) {
        LogDB.dAD("--$tag-MAX---load---start--$adItem")
        interstitialAd = MaxInterstitialAd(adItem.Id, activity)
        interstitialAd?.setRevenueListener {
            if (it.revenue >0){
                adEvent?.daiboo_value_micros = ((it.revenue *1000000L).toLong())
                adEvent?.daiboo_precision_type = it.revenuePrecision
            }
        }
        interstitialAd?.setListener(object : MaxAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                // Interstitial ad is ready to be shown. interstitialAd.isReady() will now return 'true'
                // Reset retry attempt
                retryAttempt = 0.0
                isClicked = false
                LogDB.dAD("--$tag-MAX---load---success--" + ad.hashCode())
                loadTime = Date().time
                mAd = ad
                success(this@DaiBooOpenMaxImpl)
                adEvent = DaiBooAdEvent(
                    daiboo_key = tag,
                    daiboo_ad_network = mAd?.networkName ?:"Max",
                    daiboo_ad_item = adItem
                )


            }

            override fun onAdDisplayed(ad: MaxAd?) {
                LogDB.dAD("--$tag-MAX---show---onAdDisplayed--")
                mAdShowCallBack?.onAdShowed(true)
                adEvent?.let {
                    HttpTBA.doReport(HttpTBA.EVENT_AD, adevent = it)
                }
                FiBLogEvent.dm_ad_impression(tag)
            }

            override fun onAdHidden(ad: MaxAd?) {
                LogDB.dAD("--$tag-MAX---show---onAdHidden--")
                mAdShowCallBack?.onAdDismiss(this@DaiBooOpenMaxImpl)
            }

            override fun onAdClicked(ad: MaxAd?) {
                LogDB.dAD("--$tag-MAX---show---onAdClicked--")
                isClicked = true
                mAdShowCallBack?.onAdClicked(this@DaiBooOpenMaxImpl)
                FiBLogEvent.ad_click(tag)
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                LogDB.dAD("--$tag-MAX---load---failed--$error")
                failed()
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                LogDB.dAD("--$tag-MAX---show---onAdDisplayFailed--$error")
                mAdShowCallBack?.onAdShowed(false)
            }

        })

        interstitialAd.loadAd()
    }

    override fun isAdAvailable(): Boolean {
        return mAd != null && interstitialAd.isReady && wasTimeIn(3000)
    }


}
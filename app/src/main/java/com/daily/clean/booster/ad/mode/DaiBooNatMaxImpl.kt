package com.daily.clean.booster.ad.mode

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder
import com.daily.clean.booster.R
import com.daily.clean.booster.ad.base.IAdShowCallBack
import com.daily.clean.booster.ad.base.BaseLoader
import com.daily.clean.booster.base.FiBLogEvent
import com.daily.clean.booster.base.*
import com.daily.clean.booster.entity.DaiBooAdEvent
import com.daily.clean.booster.entity.AdConf
import com.daily.clean.booster.tba.HttpTBA
import com.daily.clean.booster.utils.LogDB
import java.util.*


class DaiBooNatMaxImpl(val activity: AppCompatActivity, var tag: String, conf: AdConf) : BaseLoader(conf) {


    var ADCallBack: IAdShowCallBack? = null

    private var nativeAdLoader: MaxNativeAdLoader? = null
    private var mAd: MaxAd? = null
    private var mNativeAdView: MaxNativeAdView? = null


    override fun load(success: (BaseLoader) -> Unit, failed: () -> Unit) {

        LogDB.dAD("--$tag-MAX---load---start--$adItem")

        nativeAdLoader = MaxNativeAdLoader(adItem.Id, activity)
        nativeAdLoader?.setRevenueListener {
            if (it.revenue > 0) {
                adEvent?.daiboo_value_micros = ((it.revenue * 1000000).toLong())
                adEvent?.daiboo_precision_type = it.revenuePrecision
            }
        }
        nativeAdLoader?.setNativeAdListener(object : MaxNativeAdListener() {
            override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, nativeAd: MaxAd) {
                LogDB.dAD("--$tag-MAX---load---success--$nativeAdView")
                mAd = nativeAd
                val nativeAd = nativeAd.nativeAd
                nativeAd?.let {
                    val aspectRatio = nativeAd.mediaContentAspectRatio
                    LogDB.dAD("--$tag-MAX---load---success--aspectRatio=$aspectRatio")
                }
                loadTime = Date().time
                mNativeAdView = nativeAdView
                isClicked = false
                success.invoke(this@DaiBooNatMaxImpl)
                adEvent = DaiBooAdEvent(
                    daiboo_key = tag,
                    daiboo_ad_network = mAd?.networkName ?: "Max",
                    daiboo_ad_item = adItem
                )
            }

            override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                LogDB.dAD("--$tag-MAX---load---error:${error.code}")
                failed.invoke()
            }

            override fun onNativeAdClicked(nativeAd: MaxAd) {
                LogDB.dAD("--$tag-MAX---load---onAdClicked-${nativeAd?.hashCode()}")
                isClicked = true
                ADCallBack?.onAdClicked(this@DaiBooNatMaxImpl)
                FiBLogEvent.ad_click(tag)
            }

        })

        nativeAdLoader?.placement = tag
        nativeAdLoader?.loadAd(createNativeAdView(tag))
    }

    /**
     * Shows the ad if one isn't already showing.
     */
    override fun show(activity: AppCompatActivity, callback: IAdShowCallBack?) {
        this.ADCallBack = callback
        LogDB.dAD("--$tag-MAX---show---start--")
        when (activity) {
//            is MainActivity -> showNativeAD(activity.getCurrentFragmentADLayout())
//            is JunkSelectActivity -> showNativeAD(activity.scan_nat_ad_layout)
//            is CleanResultActivity -> showNativeAD(activity.result_nat_ad_layout)
        }


    }

    fun createNativeAdView(tag: String): MaxNativeAdView? {

        val layoutId = when (tag) {
//            MainConfig.DAIBOO_AD_HOME_NAT -> R.layout.layout_ad_max_home
//            MainConfig.DAIBOO_AD_CLEAN_NAT -> R.layout.layout_admob_clean
            DB_AD_RESULT_NV -> R.layout.ad_max_middle
//            MainConfig.DAIBOO_AD_SCAN_NAT-> R.layout.layout_ad_max_scan
            else -> R.layout.ad_max_middle
        }
        val binder: MaxNativeAdViewBinder = MaxNativeAdViewBinder.Builder(layoutId)
            .setTitleTextViewId(R.id.ad_head)
            .setBodyTextViewId(R.id.ad_body)
            .setIconImageViewId(R.id.ad_icon)
            .setAdvertiserTextViewId(R.id.advertiser_textView)
            .setMediaContentViewGroupId(R.id.media_view_container)
            .setOptionsContentViewGroupId(R.id.ad_options_view)
            .setCallToActionButtonId(R.id.ad_view_cta)
            .build()
        return MaxNativeAdView(binder, activity)
    }

    private fun showNativeAD(frameLayout: ViewGroup?) {
            LogDB.dAD("--$tag-MAX---show--try-${mAd?.hashCode()}  ${mNativeAdView}  $frameLayout")
            if (mNativeAdView != null && frameLayout != null) {
                frameLayout.removeAllViews()
                frameLayout.addView(mNativeAdView)
                LogDB.dAD("--$tag-MAX---show---success-${mAd?.hashCode()}")
                ADCallBack?.onAdShowed(true)
                adEvent?.let {
                    HttpTBA.doReportAd(adEvent = it)
                }
                FiBLogEvent.dm_ad_impression(tag)
            }
    }


    override fun destroy() {
        super.destroy()
        LogDB.dAD("--$tag-MAX---show---destroy ${mAd?.hashCode()}")
        if (mAd != null) {
            nativeAdLoader?.destroy(mAd)
            mAd = null
            mNativeAdView = null
        }

    }

    /**
     * Utility method that checks if ad exists and can be shown.
     */
    override fun isAdAvailable(): Boolean {
        return mAd != null && nativeAdLoader != null && wasTimeIn(3000)
    }


}
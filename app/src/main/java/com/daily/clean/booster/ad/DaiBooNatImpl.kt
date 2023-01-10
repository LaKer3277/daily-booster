package com.daily.clean.booster.ad

import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.daily.clean.booster.App
import com.daily.clean.booster.R
import com.daily.clean.booster.base.BaseActivity
import com.daily.clean.booster.base.FiBLogEvent
import com.daily.clean.booster.entity.DaiBooAdEvent
import com.daily.clean.booster.entity.DaiBooAdItemBean
import com.daily.clean.booster.utils.LogDB
import com.daily.clean.booster.tba.HttpTBA
import com.daily.clean.booster.ui.CleanResultActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.formats.NativeAdOptions.ADCHOICES_TOP_RIGHT
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import java.util.*


class DaiBooNatImpl(var tag: String, itemBean: DaiBooAdItemBean) : DaiBooBaseAD(itemBean) {

    var mAd: NativeAd? = null

    var ADCallBack: ClnoptADCallBack? = null


    private fun getAdRequest() = AdRequest.Builder().build()

    override fun load(success: (DaiBooBaseAD) -> Unit, failed: () -> Unit) {

        LogDB.dAD("--$tag----load---start--$adItem")

        AdLoader.Builder(App.ins, adItem.Id).forNativeAd {
            mAd = it
            isClicked = false
            LogDB.dAD("--$tag----load---success--" + it.hashCode())
            this@DaiBooNatImpl.loadTime = Date().time
            success.invoke(this@DaiBooNatImpl)
            adEvent = DaiBooAdEvent(
                daiboo_key = tag,
                daiboo_ad_network = getNetWork(it),
                daiboo_ad_item = adItem
            )
            mAd?.setOnPaidEventListener { v ->
                LogDB.dAD("--$tag----show---pay--${v.valueMicros}")
                adEvent?.daiboo_value_micros = (v.valueMicros)
                adEvent?.daiboo_precision_type = "${v.precisionType}"
            }

        }.withAdListener(object : AdListener() {

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                LogDB.dAD("--$tag----load---error:${loadAdError.message}")
                failed.invoke()
            }

            override fun onAdClosed() {
                LogDB.dAD("--$tag----load---onAdClosed")
            }

            override fun onAdOpened() {
                LogDB.dAD("--$tag----load---onAdOpened")
            }


            override fun onAdClicked() {
                LogDB.dAD("--$tag----load---onAdClicked-${mAd?.hashCode()}")
                isClicked = true
                ADCallBack?.onAdClicked(this@DaiBooNatImpl)
                FiBLogEvent.ad_click(tag)
            }

            override fun onAdImpression() {
                LogDB.dAD("$tag----load---onAdImpression-${mAd?.hashCode()}")
                adEvent?.let {
                    HttpTBA.doReport(HttpTBA.EVENT_AD, adevent = it)
                }
                FiBLogEvent.dm_ad_impression(tag)

            }
        }).withNativeAdOptions(
            NativeAdOptions.Builder().setAdChoicesPlacement(ADCHOICES_TOP_RIGHT).build()
        ).build().loadAd(getAdRequest())

    }

    /**
     * Shows the ad if one isn't already showing.
     */
    override fun show(activity: AppCompatActivity, callback: ClnoptADCallBack?) {
        this.ADCallBack = callback
        LogDB.dAD("--$tag----show---start--")

        if (isAdAvailable()) {
            when (activity) {
//                is HomeActivity ->
//                    showNativeAD(
//                        activity,
//                        activity.getCurrentFragmentADLayout(),
//                        if (MainConfig.DAIBOO_AD_CLEAN_NAT == tag) R.layout.layout_admob_clean else R.layout.layout_admob_home
//                    )
                is CleanResultActivity -> showNativeAD(
                    activity,
                    activity.binding.resultNatAdLayout,
                    R.layout.layout_admob_result
                )
//                is JunkSelectActivity -> showNativeAD(activity, activity.scan_nat_ad_layout, R.layout.layout_admob_scan)
            }
        } else {
            callback?.onAdDismiss(this)
        }
    }

    private fun showNativeAD(context: BaseActivity<*>, frameLayout: ViewGroup?, layoutId: Int) {
        val nativeAd = mAd
        val adView = context.layoutInflater.inflate(layoutId, null) as NativeAdView
        if (nativeAd != null) {

            val mediaView: MediaView? = adView.findViewById(R.id.ad_media)
            adView.mediaView = mediaView
            if (nativeAd.mediaContent != null) {
                mediaView?.setMediaContent(nativeAd.mediaContent!!)
                mediaView?.visibility = VISIBLE
            } else {
                mediaView?.visibility = INVISIBLE
            }

            val tv_head = adView.findViewById<TextView>(R.id.ad_head)
            adView.headlineView = tv_head
            if (nativeAd.headline != null) {
                tv_head.text = nativeAd.headline
                tv_head.visibility = VISIBLE
            } else {
                tv_head.visibility = INVISIBLE
            }

            val tv_body = adView.findViewById<TextView>(R.id.ad_body)
            adView.bodyView = tv_body
            if (nativeAd.body != null) {
                tv_body.text = nativeAd.body
                tv_body.visibility = VISIBLE
            } else {
                tv_body.visibility = INVISIBLE
            }

            val view_cta = adView.findViewById<TextView>(R.id.ad_view_cta)
            adView.callToActionView = view_cta
            if (nativeAd.callToAction != null) {
                view_cta.text = nativeAd.getCallToAction()
                view_cta.visibility = VISIBLE
            } else {
                view_cta.visibility = INVISIBLE
            }


            val iv_icon = adView.findViewById<ImageView>(R.id.ad_icon)
            adView.iconView = iv_icon
            if (nativeAd.icon != null && nativeAd.icon!!.drawable != null) {
                iv_icon.setImageDrawable(nativeAd.icon!!.drawable)
                iv_icon.visibility = VISIBLE
            } else {
                iv_icon.visibility = INVISIBLE
            }
            adView.setNativeAd(nativeAd)
            frameLayout?.removeAllViews()
            frameLayout?.addView(adView)
            LogDB.dAD("--$tag----show---success-${mAd?.hashCode()}")
            ADCallBack?.onAdShowed(true)
        }
    }


    override fun destroy() {
        super.destroy()
        LogDB.dAD("--$tag----show---destroy ${mAd?.hashCode()}")
        mAd?.destroy()
        mAd = null

    }

    /**
     * Utility method that checks if ad exists and can be shown.
     */
    override fun isAdAvailable(): Boolean {
        return mAd != null && wasTimeIn(3000)
    }

    fun getNetWork(ad: NativeAd): String {
        if (ad == null) return "admob"
        return if (ad.responseInfo?.mediationAdapterClassName != null && "com.google.ads.mediation.admob.AdMobAdapter" != ad.responseInfo?.mediationAdapterClassName) {
            ad.responseInfo?.mediationAdapterClassName ?: "admob"
        } else {
            "admob"
        }

    }

}
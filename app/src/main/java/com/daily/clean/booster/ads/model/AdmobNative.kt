package com.daily.clean.booster.ads.model

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.daily.clean.booster.R
import com.daily.clean.booster.ads.AdsListener
import com.daily.clean.booster.ads.conf.AdConf
import com.daily.clean.booster.ads.conf.AdPos
import com.daily.clean.booster.ext.loggerAds
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

class AdmobNative(val adPos: AdPos, adCong: AdConf): BaseNav(adPos, adCong), OnPaidEventListener {

    private var navAd: NativeAd? = null
    private var loadErrorAction: (error: String) -> Unit = {}
    override fun loaded(ad: Any, adsListener: AdsListener) {
        super.loaded(ad, adsListener)
        if (ad !is NativeAd) return
        navAd = ad
        ad.setOnPaidEventListener(this)
    }

    override fun onPaidEvent(p0: AdValue) {
        //taichiEvent(p0, navAd?.responseInfo)
    }

    fun setLoadErrAction(loadAction: (error: String) -> Unit) {
        loadErrorAction = loadAction
    }

    val listener = object : AdListener() {
        override fun onAdFailedToLoad(p0: LoadAdError) {
            loggerAds("NativeAdLoadError: ${p0.message}")
            loadErrorAction.invoke(p0.message)
        }

        override fun onAdImpression() {
            unitAdShown.invoke()
        }

        override fun onAdClicked() {
            unitAdClick.invoke()
        }

        override fun onAdClosed() {
            unitAdClose.invoke()
        }
    }

    override fun adLayoutId(): Int {
        return R.layout.ad_admob_standard
    }

    override fun show(activity: Activity, rootLayout: ViewGroup, inflateLayout: Int): Boolean {
        return showNativeAD(activity, rootLayout, inflateLayout)
    }

    override fun onDestroy() {
        navAd?.destroy()
    }

    private fun showNativeAD(context: Activity, frameLayout: ViewGroup?, layoutId: Int): Boolean {
        val nativeAd = navAd ?: return false

        val adView = context.layoutInflater.inflate(layoutId, null) as NativeAdView
        val mediaView: MediaView? = adView.findViewById(R.id.ad_media)
        adView.mediaView = mediaView
        if (nativeAd.mediaContent != null) {
            mediaView?.mediaContent = nativeAd.mediaContent!!
            mediaView?.visibility = View.VISIBLE
        } else {
            mediaView?.visibility = View.INVISIBLE
        }

        val tvHead = adView.findViewById<TextView>(R.id.ad_head)
        adView.headlineView = tvHead
        if (nativeAd.headline != null) {
            tvHead.text = nativeAd.headline
            tvHead.visibility = View.VISIBLE
        } else {
            tvHead.visibility = View.INVISIBLE
        }

        val tvBody = adView.findViewById<TextView>(R.id.ad_body)
        adView.bodyView = tvBody
        if (nativeAd.body != null) {
            tvBody.text = nativeAd.body
            tvBody.visibility = View.VISIBLE
        } else {
            tvBody.visibility = View.INVISIBLE
        }

        val viewCta = adView.findViewById<TextView>(R.id.ad_view_cta)
        adView.callToActionView = viewCta
        if (nativeAd.callToAction != null) {
            viewCta.text = nativeAd.callToAction
            viewCta.visibility = View.VISIBLE
        } else {
            viewCta.visibility = View.INVISIBLE
        }

        val ivIcon = adView.findViewById<ImageView>(R.id.ad_icon)
        adView.iconView = ivIcon
        if (nativeAd.icon != null && nativeAd.icon!!.drawable != null) {
            ivIcon.setImageDrawable(nativeAd.icon!!.drawable)
            ivIcon.visibility = View.VISIBLE
        } else {
            ivIcon.visibility = View.INVISIBLE
        }
        adView.setNativeAd(nativeAd)
        frameLayout?.removeAllViews()
        frameLayout?.addView(adView)
        return true
    }
}
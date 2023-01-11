package com.daily.clean.booster.ads

import com.daily.clean.booster.ads.model.BaseAd

open class AdsListener {

    open fun onLoaded(ad: BaseAd) {}

    open fun onError(error: String = "") {}

    open fun onShown() {}

    open fun onDismiss() {}

    open fun onClick() {}
}
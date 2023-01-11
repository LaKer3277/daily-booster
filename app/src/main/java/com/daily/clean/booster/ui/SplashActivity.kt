package com.daily.clean.booster.ui

import android.animation.Animator
import android.animation.ValueAnimator
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.daily.clean.booster.App
import com.daily.clean.booster.ad.DaiBooADUtil
import com.daily.clean.booster.ads.AdsListener
import com.daily.clean.booster.ads.AdsLoader
import com.daily.clean.booster.ads.conf.AdPos
import com.daily.clean.booster.ads.model.BaseAd
import com.daily.clean.booster.ads.model.BaseIns
import com.daily.clean.booster.appIns
import com.daily.clean.booster.base.*
import com.daily.clean.booster.databinding.ActivitySplashBinding
import com.daily.clean.booster.ext.currentTms
import com.daily.clean.booster.tba.HttpTBA
import com.daily.clean.booster.utils.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    override fun dailyBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun dailyData() {
        val tanId = intent.getStringExtra(DBConfig.DAIBOO_KEY_NOTY_ID) ?: ""
        val workID = intent.getStringExtra(DBConfig.DAIBOO_KEY_WORK_ID) ?: ""
        val action = intent?.action ?: ""
        if (action == DBConfig.DAIBOO_ACTION_FROM_POP_NOTY_POP ) {
            NotificationManagerCompat.from(App.ins).cancel(DBConfig.NOTIFY_POP_ID)
            FiBLogEvent.pop_log(tanId, 2)
            FiBLogEvent.up_all_start()
        }

        if (action == DBConfig.DAIBOO_ACTION_FROM_NOTIFYTOOL ){
            FiBLogEvent.notifi_click(workID)
        }
        FiBLogEvent.start_page()
        FiBLogEvent.user_rent()
        HttpTBA.reportFirst()
    }

    override fun dailyLoad() {
        val openNextLogic = {
            goNextByIntent()
        }
        runAnim(10_000L) {
            if (isAdImpression) return@runAnim
            if (isActivityPaused) {
                return@runAnim
            }
            openNextLogic.invoke()
        }
        lifecycleScope.launch {
            delay(200L)
            openAdLogic() {
                openNextLogic.invoke()
            }
            delay(600L)
            DaiBooADUtil.load(DBConfig.DAIBOO_AD_RESULT_NV, this@SplashActivity)
            DaiBooADUtil.load(DBConfig.DAIBOO_AD_CLEAN_IV, this@SplashActivity)
        }
    }

    override fun onBackPressed() {}

    private var valueAni: ValueAnimator? = null
    private fun runAnim(long: Long, action: () -> Unit) {
        valueAni?.cancel()
        valueAni = ValueAnimator.ofInt(binding.progressbar.progress, 100)
        valueAni?.duration = long
        valueAni?.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener{
            override fun onAnimationUpdate(p0: ValueAnimator) {
                (p0.animatedValue as? Int)?.apply {
                    binding.progressbar.progress = this
                }
            }
        })
        valueAni?.addListener(object : AnimatorListener() {
            private var isCanceled = false
            override fun onAnimationCancel(animation: Animator) {
                isCanceled = true
            }

            override fun onAnimationEnd(animation: Animator) {
                if (!isCanceled) {
                    action.invoke()
                }
            }
        })
        valueAni?.start()
    }

    private var startTms = 0L
    private var isAdImpression = false
    private fun openAdLogic(adNextAction: () -> Unit) {
        isAdImpression = false
        startTms = currentTms()

        fun preloadOpen() {
            AdsLoader.preloadAd(appIns, AdPos.Open)
        }

        AdsLoader.loadAd(appIns, AdPos.Open, object : AdsListener() {
            override fun onLoaded(ad: BaseAd) {
                if (isActivityPaused) {
                    AdsLoader.add2Cache(AdPos.Open, ad)
                    return
                }
                preloadOpen()
                if (ad !is BaseIns || !ad.show(this@SplashActivity)) {
                    adNextAction.invoke()
                }
            }

            override fun onError(msg: String) {
                preloadOpen()
                if (isActivityPaused) return
                adNextAction.invoke()
            }

            override fun onDismiss() {
                if (appIns.isAtForeground()) adNextAction.invoke() else finish()
            }

            override fun onShown() {
                isAdImpression = true
            }
        })
    }

    private fun goNextByIntent() {
        val workId = intent?.getStringExtra(DBConfig.DAIBOO_KEY_WORK_ID) ?: ""
        when (workId) {
            DBConfig.DAIBOO_WORK_ID_CLEAN -> {
                goJunkCleanScanning(intent.action)
            }

            DBConfig.DAIBOO_WORK_ID_BOOSTER,
            DBConfig.DAIBOO_WORK_ID_CPU,
            DBConfig.DAIBOO_WORK_ID_BATTERY,
            DBConfig.DAIBOO_WORK_ID_ClEAN_NOTIFICATION -> {
                goBoosting(work_id = workId, actionStr = intent.action)
            }

            else -> {
                goMain(DBConfig.DAIBOO_ACTION_FROM_SPLASH)
            }
        }

        finish()

    }

}
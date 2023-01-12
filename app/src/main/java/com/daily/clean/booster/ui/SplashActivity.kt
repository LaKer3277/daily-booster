package com.daily.clean.booster.ui

import android.animation.Animator
import android.animation.ValueAnimator
import androidx.lifecycle.lifecycleScope
import com.daily.clean.booster.ads.AdsListener
import com.daily.clean.booster.ads.AdsLoader
import com.daily.clean.booster.ads.conf.AdPos
import com.daily.clean.booster.ads.model.BaseAd
import com.daily.clean.booster.ads.model.BaseIns
import com.daily.clean.booster.appIns
import com.daily.clean.booster.base.*
import com.daily.clean.booster.databinding.ActivitySplashBinding
import com.daily.clean.booster.pop.*
import com.daily.clean.booster.ext.currentTms
import com.daily.clean.booster.ext.goBoosting
import com.daily.clean.booster.ext.goJunkCleanScanning
import com.daily.clean.booster.ext.goMain
import com.daily.clean.booster.tba.HttpTBA
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    override fun dailyBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun dailyData() {
        val tanId = intent.getStringExtra(Noty_KEY_SOURCE) ?: ""
        val workID = intent.getStringExtra(Noty_KEY_WORK) ?: ""
        val action = intent?.action ?: ""
        if (action == DB_ACTION_FROM_POP_NOTY_POP ) {
            NotifyManager.cancelAlertNotification()
            FiBLogEvent.pop_log(tanId, 2)
            FiBLogEvent.up_all_start()
        }

        if (action == DB_ACTION_FROM_NOTIFYTOOL ){
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
            AdsLoader.preloadAd(this@SplashActivity, AdPos.NavResult)
            AdsLoader.preloadAd(this@SplashActivity, AdPos.InsClean)
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
        val workId = intent?.getStringExtra(Noty_KEY_WORK) ?: ""
        when (workId) {
            NotyWorkClean -> {
                goJunkCleanScanning(intent.action)
            }

            NotyWorkBooster,
            NotyWorkCpu,
            NotyWorkBattery -> {
                goBoosting(work_id = workId, actionStr = intent.action)
            }

            else -> {
                goMain(DB_ACTION_FROM_SPLASH)
            }
        }

        finish()

    }

}
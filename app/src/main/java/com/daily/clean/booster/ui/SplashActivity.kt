package com.daily.clean.booster.ui

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    override fun dailyBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(layoutInflater)
    }

    private var tanId: String = ""
    private var workID = ""
    private var intentAction = ""
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        dispatchIntent(intent)
    }

    override fun statusColor(): Int {
        return Color.parseColor("#FAF9FC")
    }

    private fun dispatchIntent(intent: Intent?) {
        tanId = intent?.getStringExtra(Noty_KEY_SOURCE) ?: ""
        workID = intent?.getStringExtra(Noty_KEY_WORK) ?: ""
        intentAction = intent?.action ?: ""
        when (intentAction) {
            DB_ACTION_FROM_POP_NOTY -> {
                NotifyManager.cancelAlertNotification()
                FirebaseEvent.popLog(tanId, 2)
                FirebaseEvent.logEvent("up_all_start")
            }

            DB_ACTION_FROM_NOTY_RESIDENT -> {
                FirebaseEvent.notifyResidentClick(workID)
            }
        }
    }

    override fun dailyData() {
        dispatchIntent(intent)
        FirebaseEvent.logEvent("start_page")
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
        FirebaseEvent.adChance(AdPos.Open)
        lifecycleScope.launch {
            delay(200L)
            openAdLogic {
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
                FirebaseEvent.adImpression(AdPos.Open)
            }
        })
    }

    private fun goNextByIntent() {
        val workId = workID
        when (workId) {
            NotyWorkClean -> {
                goJunkCleanScanning(intentAction)
            }

            NotyWorkBooster,
            NotyWorkCpu,
            NotyWorkBattery -> {
                goBoosting(work_id = workId, actionStr = intentAction)
            }

            else -> {
                goMain(DB_PAGE_FROM_SPLASH)
            }
        }

        finish()

    }

}
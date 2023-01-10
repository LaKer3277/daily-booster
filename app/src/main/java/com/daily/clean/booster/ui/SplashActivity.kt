package com.daily.clean.booster.ui

import android.view.View
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.daily.clean.booster.DBApp
import com.daily.clean.booster.ad.DaiBooADUtil
import com.daily.clean.booster.base.BaseActivity
import com.daily.clean.booster.base.FiBLogEvent
import com.daily.clean.booster.base.FiBRemoteUtil
import com.daily.clean.booster.base.DBConfig
import com.daily.clean.booster.databinding.ActivitySplashBinding
import com.daily.clean.booster.tba.HttpTBA
import com.daily.clean.booster.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    override fun daibooLayoutId(): View {
        binding = ActivitySplashBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun daibooData() {

        initLocalData()

        val tanId = intent.getStringExtra(DBConfig.DAIBOO_KEY_NOTY_ID) ?: ""
        val workID = intent.getStringExtra(DBConfig.DAIBOO_KEY_WORK_ID) ?: ""
        val action = intent?.action ?: ""
        if (action == DBConfig.DAIBOO_ACTION_FROM_POP_NOTY_POP ) {
            NotificationManagerCompat.from(DBApp.ins).cancel(DBConfig.NOTIFY_POP_ID)
            FiBLogEvent.pop_log(tanId, 2)
            FiBLogEvent.up_all_start()
        }

        if (action == DBConfig.DAIBOO_ACTION_FROM_NOTIFYTOOL ){
            FiBLogEvent.notifi_click(workID)
        }
        FiBLogEvent.start_page()
        FiBLogEvent.user_rent()

    }


    override fun daibooView() {

        initStartView()

    }

    override fun daibooLoad() {

        HttpTBA.reportFirst()

    }


    fun initStartView() {
        isShowedAD = false
        currentProgress
    }


    var isShowedAD = false
    override fun onResume() {
        super.onResume()
        if (isShowedAD.not()) {
            loadADS()
            startJob()
        }

    }

    override fun onPause() {
        super.onPause()
        jobToHome?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        jobLoadOpen?.cancel()
    }


    var jobToHome: Job? = null
    var currentProgress = 0f
    fun startJob(faster: Boolean = false) {
        jobToHome?.cancel()
        var delayTime: Long = 20L
        delayTime = if (!DaiBooADUtil.isReachLimit()) {
            70L
        } else {
            20L
        }
        if (faster) delayTime = 10L
        var startProcess = currentProgress

        LogDB.d("Process= $startProcess  $delayTime")

        jobToHome = lifecycleScope.launch {
            while (startProcess < 100) {
                delay(delayTime)
                startProcess++
                binding.progressbar.progress = startProcess.toInt()
                currentProgress = startProcess
//                progress_circle.setValue(startProcess)
//                LogDM.d("Process=$startProcess -  ---  $delayTime")
            }
            if (startProcess == 100f) {
                LogDB.d("showAD")
                showAD()
            }
        }

    }


    override fun onBackPressed() {
    }


    fun initLocalData() {
        FiBRemoteUtil.initFireBaseData {
            startJob()
            loadADS()
        }
    }


    var isCacheOpenAd = false
    var jobLoadOpen: Job? = null
    var loadTimes = 0
    fun loadADS() {
        loadTimes = 0
        DaiBooADUtil.load(DBConfig.DAIBOO_AD_RESULT_NV, this)
        DaiBooADUtil.load(DBConfig.DAIBOO_AD_CLEAN_IV, this)
        isCacheOpenAd = false
        jobLoadOpen?.cancel()
        jobLoadOpen = lifecycle.coroutineScope.launch {
            while (isCacheOpenAd.not()) {
                delay(300)
                DaiBooADUtil.load(
                    DBConfig.DAIBOO_AD_OPEN,
                    this@SplashActivity,
                    callLoading = {
                    },
                    callEnd = {
                        loadTimes++
                        if (loadTimes > 1) {
                            jobLoadOpen?.cancel()
                            startJob(true)
                        }
                    },
                    callBack = {
                        isCacheOpenAd = true
                        jobToHome?.cancel()
                        startJob(true)
                    }
                )
            }
        }
    }


    fun showAD() {
        lifecycleScope.launch {
            DaiBooADUtil.showAD(DBConfig.DAIBOO_AD_OPEN, this@SplashActivity) {
                isShowedAD = it
                DaiBooADUtil.load(DBConfig.DAIBOO_AD_OPEN, this@SplashActivity)
                lifecycleScope.launch {
                    delay(120)
                    if (DBApp.ins.isAtForeground() && isPause.not()) {
                        goNextByIntent()
                    }
                    finish()
                }
            }
        }

    }


    fun goNextByIntent() {
        val workId = intent?.getStringExtra(DBConfig.DAIBOO_KEY_WORK_ID) ?: ""
        LogDB.d("workId---$workId")
        when (workId) {
            DBConfig.DAIBOO_WORK_ID_CLEAN -> {
                goJunkCleanScanning(intent.action)
            }
            DBConfig.DAIBOO_WORK_ID_BOOSTER,
            DBConfig.DAIBOO_WORK_ID_CPU,
            DBConfig.DAIBOO_WORK_ID_BATTERY,
            DBConfig.DAIBOO_WORK_ID_ClEAN_NOTIFICATION,
            -> {
                goBoosting(work_id = workId, actionStr = intent.action)
            }
            else -> {
                goMain(DBConfig.DAIBOO_ACTION_FROM_SPLASH)
//                        MediationTestSuite.launch(this@SplashActivity)
            }
        }

    }

}
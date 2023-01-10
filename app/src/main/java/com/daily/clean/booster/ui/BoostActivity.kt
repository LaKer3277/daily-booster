package com.daily.clean.booster.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.lifecycle.lifecycleScope
import com.daily.clean.booster.BuildConfig
import com.daily.clean.booster.App
import com.daily.clean.booster.R
import com.daily.clean.booster.ad.DaiBooADUtil
import com.daily.clean.booster.base.BaseActivity
import com.daily.clean.booster.base.DBConfig
import com.daily.clean.booster.base.FiBLogEvent
import com.daily.clean.booster.base.FiBRemoteUtil
import com.daily.clean.booster.core.clean.CleanData
import com.daily.clean.booster.databinding.ActivityBoostBinding
import com.daily.clean.booster.utils.*
import com.lzp.dslanimator.PlayMode
import com.lzp.dslanimator.animSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.random.Random

class BoostActivity : BaseActivity<ActivityBoostBinding>() {

    override fun dailyBinding(): ActivityBoostBinding {
        return ActivityBoostBinding.inflate(layoutInflater)
    }

    override fun dailyData() {
        workId = intent.getStringExtra(DBConfig.DAIBOO_KEY_WORK_ID) ?: DBConfig.DAIBOO_WORK_ID_BOOSTER
        popId = intent.getStringExtra(DBConfig.DAIBOO_KEY_NOTY_ID)
        isFirst = intent.getBooleanExtra(DBConfig.DAIBOO_KEY_IS_FIRST, false)
        queryApps()
        fbLog()
    }


    var currentStatus = 0
    override fun dailyView() {
        binding.titleBack.setOnClickListener {
            onBackPressed()
        }

        binding.titleText.text = workId.getTitleText()

        currentStatus = if (isFirst) {
            1
        } else {
            0
        }
    }

    override fun onResume() {
        super.onResume()
        when (currentStatus) {
            0 -> scanning()
            1 -> cleaning()
            2 -> showCompleteView()
        }
    }

    override fun onPause() {
        super.onPause()
        scaningJob?.cancel()
        scanningAppIconJob?.cancel()
        cleanJob?.cancel()

    }

    var scaningJob: Job? = null
    var cleanJob: Job? = null
    var completeJob: Job? = null

    private fun scanning() {
        currentStatus = 0
        //一阶段扫描动画
        startAppAnim()
        scaningJob?.cancel()
        scaningJob = lifecycleScope.launch {
            if (isFirst.not()) FiBLogEvent.page_scan_show(workId)
            binding.lotAnimScan.setAnimation(getScanAnimal())
            binding.lotAnimScan.playAnimation()
            doCycle { binding.tvDes.text = getScanningText("${it}%") }
            cleaning()
        }

    }


    //二阶段 清理动画
    private fun cleaning() {
        currentStatus = 1
        cleanJob?.cancel()
        cleanJob = lifecycleScope.launch {
            if (isFirst.not()) FiBLogEvent.page_clean_show(workId)
            binding.lotAnimScan.run {
                setAnimation(getCleanAnimal())
                playAnimation()
            }
            doCycle { binding.tvDes.text = "${getWorkingText()}${it}%" }
            showCompleteView()
        }
    }


    private fun showCompleteView() {
        currentStatus = 2
        binding.lotAnimScan.visibility = View.INVISIBLE
        binding.ivEnd.visibility = View.VISIBLE
        binding.ivAppIcon.visibility = View.GONE

        animComplete.run {
            start()
        }
        completeJob?.cancel()
        completeJob = lifecycleScope.launch {
            delay(1000)
            showAdClean()
        }
        binding.tvDes.text = ""
    }

    override fun dailyLoad() {
        loadADS()
    }

    override fun onBackPressed() {
        goMain()
        finish()
    }

    private val animComplete by lazy {
        animSet {
            scaleXAnim {
                target = binding.ivEnd
                values = floatArrayOf(0f, 1.1f, 0.9f, 1f)
                repeatCount = 0
            }
            scaleYAnim {
                target = binding.ivEnd
                values = floatArrayOf(0f, 1.1f, 0.9f, 1f)
                repeatCount = 0
            }

            duration = 1000
            interpolator = LinearInterpolator()
            playMode = PlayMode.TOGETHER

        }

    }


    private fun delayFlow() = flow {
        delay(200)
        while (true) {
            emit(Unit)
            delay((200L..400L).random())
        }
    }

    private var scanningAppIconJob: Job? = null
    @SuppressLint("SetTextI18n")
    private fun startAppAnim() {
        scanningAppIconJob = lifecycleScope.launch(Dispatchers.Main) {
            binding.tvDes.text = "Scanning..."

            delayFlow()
                .map {
                    if (CleanData.appList.isNotEmpty()) {
                        try {
                            CleanData.appList[Random.nextInt(0, CleanData.appList.size - 1)]
                        } catch (e: Exception) {
                            null
                        }
                    } else null
                }
                .filter { null != it }
                .flowOn(Dispatchers.IO)
                .collect {
                    binding.ivAppIcon.setImageDrawable(it)
                }

        }
        scanningAppIconJob?.start()
    }


    private fun queryApps() {
        val intent =
            Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        val infoList = App.ins.packageManager.queryIntentActivities(intent, 0)
        CleanData.appList.clear()
        infoList.forEach {
            with(it) {
                if (activityInfo.packageName != BuildConfig.APPLICATION_ID) {
                    CleanData.appList.add(loadIcon(App.ins.packageManager))
                }
            }
        }
    }


    var workId = DBConfig.DAIBOO_WORK_ID_BOOSTER
    var popId: String? = null
    var isFirst = false


    private fun getScanningText(text: String): String {
        return when (workId) {
            DBConfig.DAIBOO_WORK_ID_BOOSTER -> R.string.scanning_boost_xx.getString(text)
            DBConfig.DAIBOO_WORK_ID_CPU -> R.string.scanning_cpu_xx.getString(text)
            DBConfig.DAIBOO_WORK_ID_BATTERY -> R.string.scanning_battery_xx.getString(text)
            else -> ""
        }
    }

    private fun getWorkingText(): String {
        return when (workId) {
            DBConfig.DAIBOO_WORK_ID_BOOSTER -> R.string.is_boosting.getString()
            DBConfig.DAIBOO_WORK_ID_CPU -> R.string.is_cooling_down.getString()
            DBConfig.DAIBOO_WORK_ID_BATTERY -> R.string.is_optimizing.getString()
            DBConfig.DAIBOO_WORK_ID_ClEAN_NOTIFICATION -> R.string.is_notification_cleaning.getString()
            else -> ""
        }
    }

    private fun getScanAnimal(): String {
        return when (workId) {
            DBConfig.DAIBOO_WORK_ID_BOOSTER -> "scan_booster.json"
            DBConfig.DAIBOO_WORK_ID_CPU -> "scan_cpu.json"
            DBConfig.DAIBOO_WORK_ID_BATTERY -> "scan_battery.json"
            else -> ""
        }
    }

    private fun getCleanAnimal(): String {
        return when (workId) {
            DBConfig.DAIBOO_WORK_ID_BOOSTER -> "clean_booster.json"
            DBConfig.DAIBOO_WORK_ID_CPU -> "clean_cpu.json"
            DBConfig.DAIBOO_WORK_ID_BATTERY -> "clean_battery.json"
            else -> ""
        }
    }


    private fun fbLog() {
        val tanId = intent.getStringExtra(DBConfig.DAIBOO_KEY_NOTY_ID)
        if (intent?.action == DBConfig.DAIBOO_ACTION_FROM_POP_NOTY_POP) {
            FiBLogEvent.up_all_page()
        }

        if (isFirst) {
            FiBLogEvent.start_first_clean()
        }
    }


    private fun goResult() {
        goCleanResult(workId, from = intent.action, isFirst = isFirst)
    }


    var isShowedCleanIV = false
    private fun showAdClean() {
        if ((FiBRemoteUtil.open_control?.first == 1 && isFirst)) {
            goResult()
        } else {
            FiBLogEvent.clean_page_to_result_start(workId)
            DaiBooADUtil.showAD(DBConfig.DAIBOO_AD_CLEAN_IV, this, workId = workId) {
                isShowedCleanIV = it
                DaiBooADUtil.load(DBConfig.DAIBOO_AD_CLEAN_IV, this)
                lifecycleScope.launch {
                    delay(90)
                    completeJob?.cancel()
                    if (isPaused.not()) {
                        goResult()
                        FiBLogEvent.clean_page_to_result_end(workId)
                    }
                    finish()
                }

            }
        }
    }


    private fun loadADS() {
        DaiBooADUtil.load(DBConfig.DAIBOO_AD_CLEAN_IV, this)
        DaiBooADUtil.load(DBConfig.DAIBOO_AD_RESULT_NV, this)
    }


}
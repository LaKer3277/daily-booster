package com.daily.clean.booster.ui.clean

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.lifecycle.lifecycleScope
import com.daily.clean.booster.BuildConfig
import com.daily.clean.booster.App
import com.daily.clean.booster.R
import com.daily.clean.booster.ads.AdsListener
import com.daily.clean.booster.ads.AdsLoader
import com.daily.clean.booster.ads.conf.AdPos
import com.daily.clean.booster.ads.model.BaseAd
import com.daily.clean.booster.ads.model.BaseIns
import com.daily.clean.booster.base.BaseActivity
import com.daily.clean.booster.base.DBConfig
import com.daily.clean.booster.base.FiBLogEvent
import com.daily.clean.booster.core.clean.CleanData
import com.daily.clean.booster.databinding.ActivityBoostBinding
import com.daily.clean.booster.utils.*
import com.lzp.dslanimator.PlayMode
import com.lzp.dslanimator.animSet
import kotlinx.coroutines.Dispatchers
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

    private var workingMode = DBConfig.DAIBOO_WORK_ID_BOOSTER
    private var popId: String? = null
    private var isFirst = false

    override fun dailyData() {
        workingMode = intent.getStringExtra(DBConfig.DAIBOO_KEY_WORK_ID) ?: DBConfig.DAIBOO_WORK_ID_BOOSTER
        popId = intent.getStringExtra(DBConfig.DAIBOO_KEY_NOTY_ID)
        isFirst = intent.getBooleanExtra(DBConfig.DAIBOO_KEY_IS_FIRST, false)

        queryApps()
        fbLog()
        binding.titleBack.setOnClickListener {
            onBackPressed()
        }

        binding.titleText.text = workingMode.getTitleText()
        if (isFirst) startCleaning() else startScanning()
    }

    private fun startScanning() {
        if (isFirst.not()) FiBLogEvent.page_scan_show(workingMode)
        startAppAnim()
        binding.lotAnimScan.apply {
            setAnimation(getScanAnimal())
            playAnimation()
        }
        lifecycleScope.launch {
            doCycle { binding.tvDes.text = getScanningText("${it}%") }
            startCleaning()
        }
    }

    //二阶段 清理动画
    @SuppressLint("SetTextI18n")
    private fun startCleaning() {
        lifecycleScope.launch {
            if (isFirst.not()) FiBLogEvent.page_clean_show(workingMode)
            binding.lotAnimScan.run {
                setAnimation(getCleanAnimal())
                playAnimation()
            }
            doCycle { binding.tvDes.text = "${getWorkingText()}${it}%" }
            showCompleteView()
        }
    }

    private fun showCompleteView() {
        binding.lotAnimScan.visibility = View.INVISIBLE
        binding.ivEnd.visibility = View.VISIBLE
        binding.ivAppIcon.visibility = View.GONE

        animComplete.run {
            start()
        }
        binding.tvDes.text = ""
        showAdClean()
    }

    override fun dailyLoad() {
        AdsLoader.preloadAd(this, AdPos.InsClean)
        AdsLoader.preloadAd(this, AdPos.NavResult)
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

    @SuppressLint("SetTextI18n")
    private fun startAppAnim() {
        lifecycleScope.launch(Dispatchers.Main) {
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
    }

    private fun queryApps() {
        val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
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

    private fun getScanningText(text: String): String {
        return when (workingMode) {
            DBConfig.DAIBOO_WORK_ID_BOOSTER -> R.string.scanning_boost_xx.getString(text)
            DBConfig.DAIBOO_WORK_ID_CPU -> R.string.scanning_cpu_xx.getString(text)
            DBConfig.DAIBOO_WORK_ID_BATTERY -> R.string.scanning_battery_xx.getString(text)
            else -> ""
        }
    }

    private fun getWorkingText(): String {
        return when (workingMode) {
            DBConfig.DAIBOO_WORK_ID_BOOSTER -> R.string.is_boosting.getString()
            DBConfig.DAIBOO_WORK_ID_CPU -> R.string.is_cooling_down.getString()
            DBConfig.DAIBOO_WORK_ID_BATTERY -> R.string.is_optimizing.getString()
            DBConfig.DAIBOO_WORK_ID_ClEAN_NOTIFICATION -> R.string.is_notification_cleaning.getString()
            else -> ""
        }
    }

    private fun getScanAnimal(): String {
        return when (workingMode) {
            DBConfig.DAIBOO_WORK_ID_BOOSTER -> "scan_booster.json"
            DBConfig.DAIBOO_WORK_ID_CPU -> "scan_cpu.json"
            DBConfig.DAIBOO_WORK_ID_BATTERY -> "scan_battery.json"
            else -> ""
        }
    }

    private fun getCleanAnimal(): String {
        return when (workingMode) {
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

    private fun showAdClean() {
        FiBLogEvent.clean_page_to_result_start(workingMode)
        AdsLoader.loadAd(this, AdPos.InsClean, object :AdsListener() {
            override fun onLoaded(ad: BaseAd) {
                if (isActivityPaused) {
                    AdsLoader.add2Cache(AdPos.InsClean, ad)
                    finish()
                    return
                }
                if (ad !is BaseIns) {
                    goNextPage()
                    return
                }
                if (!ad.show(this@BoostActivity)) {
                    goNextPage()
                }
            }

            override fun onError(error: String) {
                goNextPage()
            }

            override fun onDismiss() {
                goNextPage()
            }
        }, onlyCache = true)
    }

    private fun goNextPage() {
        goCleanResult(workingMode, from = intent.action, isFirst = isFirst)
        finish()
    }

}
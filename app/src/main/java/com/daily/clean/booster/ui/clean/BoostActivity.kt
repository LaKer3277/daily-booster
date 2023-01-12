package com.daily.clean.booster.ui.clean

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.lifecycle.lifecycleScope
import com.daily.clean.booster.BuildConfig
import com.daily.clean.booster.R
import com.daily.clean.booster.ads.AdsListener
import com.daily.clean.booster.ads.AdsLoader
import com.daily.clean.booster.ads.conf.AdPos
import com.daily.clean.booster.ads.model.BaseAd
import com.daily.clean.booster.ads.model.BaseIns
import com.daily.clean.booster.appIns
import com.daily.clean.booster.base.BaseActivity
import com.daily.clean.booster.base.*
import com.daily.clean.booster.base.FirebaseEvent
import com.daily.clean.booster.core.CleanData
import com.daily.clean.booster.databinding.ActivityBoostBinding
import com.daily.clean.booster.ext.doCycle
import com.daily.clean.booster.ext.getString
import com.daily.clean.booster.ext.getTitleText
import com.daily.clean.booster.ext.goCleanResult
import com.daily.clean.booster.pop.*
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

    private var workingMode = NotyWorkBooster
    private var popSource: String? = null
    private var isFirst = false

    override fun dailyData() {
        workingMode = intent.getStringExtra(Noty_KEY_WORK) ?: NotyWorkBooster
        popSource = intent.getStringExtra(Noty_KEY_SOURCE)
        isFirst = intent.getBooleanExtra(DB_KEY_IS_FIRST, false)

        queryApps()
        fbLog()
        binding.titleBack.setOnClickListener {
            onBackPressed()
        }

        binding.titleText.text = workingMode.getTitleText()
        if (isFirst) startCleaning() else startScanning()
    }

    private fun startScanning() {
        if (isFirst.not()) FirebaseEvent.pageScanShow(workingMode)
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
            if (isFirst.not()) FirebaseEvent.pageCleanShow(workingMode)
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
        val infoList = appIns.packageManager.queryIntentActivities(intent, 0)
        CleanData.appList.clear()
        infoList.forEach {
            with(it) {
                if (activityInfo.packageName != BuildConfig.APPLICATION_ID) {
                    CleanData.appList.add(loadIcon(appIns.packageManager))
                }
            }
        }
    }

    private fun getScanningText(text: String): String {
        return when (workingMode) {
            NotyWorkBooster -> R.string.scanning_boost_xx.getString(text)
            NotyWorkCpu -> R.string.scanning_cpu_xx.getString(text)
            NotyWorkBattery -> R.string.scanning_battery_xx.getString(text)
            else -> ""
        }
    }

    private fun getWorkingText(): String {
        return when (workingMode) {
            NotyWorkBooster -> R.string.is_boosting.getString()
            NotyWorkCpu -> R.string.is_cooling_down.getString()
            NotyWorkBattery -> R.string.is_optimizing.getString()
            else -> ""
        }
    }

    private fun getScanAnimal(): String {
        return when (workingMode) {
            NotyWorkBooster -> "scan_booster.json"
            NotyWorkCpu -> "scan_cpu.json"
            NotyWorkBattery -> "scan_battery.json"
            else -> ""
        }
    }

    private fun getCleanAnimal(): String {
        return when (workingMode) {
            NotyWorkBooster -> "clean_booster.json"
            NotyWorkCpu -> "clean_cpu.json"
            NotyWorkBattery -> "clean_battery.json"
            else -> ""
        }
    }


    private fun fbLog() {
        val tanId = intent.getStringExtra(Noty_KEY_SOURCE)
        if (intent?.action == DB_ACTION_FROM_POP_NOTY) {
            FirebaseEvent.logEvent("up_all_page")
        }

        if (isFirst) {
            FirebaseEvent.logEvent("start_first_clean")
        }
    }

    private fun showAdClean() {
        FirebaseEvent.adChance(AdPos.InsClean)
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

            override fun onShown() {
                FirebaseEvent.adImpression(AdPos.InsClean)
            }
        }, onlyCache = true)
    }

    private fun goNextPage() {
        goCleanResult(workingMode, from = intent.action, isFirst = isFirst)
        finish()
    }

}
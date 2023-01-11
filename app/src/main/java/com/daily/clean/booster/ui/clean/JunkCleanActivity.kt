package com.daily.clean.booster.ui.clean

import android.view.View
import android.view.animation.LinearInterpolator
import androidx.lifecycle.lifecycleScope
import com.daily.clean.booster.R
import com.daily.clean.booster.ad.DaiBooADUtil
import com.daily.clean.booster.ads.AdsLoader
import com.daily.clean.booster.ads.conf.AdPos
import com.daily.clean.booster.base.BaseActivity
import com.daily.clean.booster.base.DBConfig
import com.daily.clean.booster.base.FiBLogEvent
import com.daily.clean.booster.databinding.ActivityCleanBinding
import com.daily.clean.booster.entity.DaiBooCleanEvent
import com.daily.clean.booster.utils.DaiBooMK
import com.daily.clean.booster.utils.doCycle
import com.daily.clean.booster.utils.goCleanResult
import com.lzp.dslanimator.PlayMode
import com.lzp.dslanimator.animSet
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus

class JunkCleanActivity : BaseActivity<ActivityCleanBinding>() {

    override fun dailyBinding(): ActivityCleanBinding {
        return ActivityCleanBinding.inflate(layoutInflater)
    }

    override fun dailyData() {
        binding.titleText.text = getString(R.string.junk_clean)
        binding.titleBack.setOnClickListener {
            finish()
        }
    }

    override fun dailyLoad() {
        loadADs()
    }

    override fun onResume() {
        super.onResume()
        if (isCleaned.not()) {
            cleanJob()
        }
    }


    var jobClean: Job? = null
    var isCleaned = false
    private fun cleanJob() {
        jobClean?.cancel()
        jobClean = lifecycleScope.launch {
            FiBLogEvent.page_clean_show(DBConfig.DAIBOO_WORK_ID_CLEAN)
            EventBus.getDefault().post(DaiBooCleanEvent(true))
            animClean.run { start() }
            doCycle {
                binding.tvDes.text = "${getString(R.string.is_cleaning)}${it}%"
            }
            isCleaned = true
            showComplete()
        }
    }


    private fun showComplete() {
        binding.ivAnimal.visibility = View.INVISIBLE
        animComplete.run {
            start()
        }
        lifecycleScope.launch(Dispatchers.Main) {
            delay(1000)
            DaiBooMK.saveAppCacheSize(0)
            DaiBooMK.reSetCleanTime()
            showAdOrInvokeNext()
        }
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

            duration = 500
            interpolator = LinearInterpolator()
            playMode = PlayMode.TOGETHER
        }
    }

    private val animClean by lazy {
        animSet {
            scaleXAnim {
                target = binding.ivAnimal
                values = floatArrayOf(1.0f, 0.95f, 1.05f, 1.0f)
                repeatCount = -1
            }
            scaleYAnim {
                target = binding.ivAnimal
                values = floatArrayOf(1.0f, 0.95f, 1.05f, 1.0f)
                repeatCount = -1
            }

            rotationAnim {
                target = binding.ivAnimal
                values = floatArrayOf(0f, 360f)
                repeatCount = -1
            }

            duration = 1000
            interpolator = LinearInterpolator()
            playMode = PlayMode.TOGETHER
        }
    }

    private fun loadADs() {
        AdsLoader.preloadAd(this, AdPos.InsClean)
        AdsLoader.preloadAd(this, AdPos.NavResult)
    }


    private fun showAdOrInvokeNext() {
        FiBLogEvent.clean_page_to_result_start(DBConfig.DAIBOO_WORK_ID_CLEAN)
        DaiBooADUtil.showAD(DBConfig.DAIBOO_AD_CLEAN_IV, this@JunkCleanActivity, workId = DBConfig.DAIBOO_WORK_ID_CLEAN) {
            DaiBooADUtil.load(DBConfig.DAIBOO_AD_CLEAN_IV, this@JunkCleanActivity)
            lifecycleScope.launch {
                delay(90)
                if (isActivityPaused.not()) {
                    val extra = intent.getStringExtra(DBConfig.DAIBOO_KEY_CLEAN_SIZE) ?: "0B"
                    goCleanResult(DBConfig.DAIBOO_WORK_ID_CLEAN, extra = extra, from = intent.action)
                    FiBLogEvent.clean_page_to_result_end(DBConfig.DAIBOO_WORK_ID_CLEAN)
                }
                finish()
            }
        }

    }
}



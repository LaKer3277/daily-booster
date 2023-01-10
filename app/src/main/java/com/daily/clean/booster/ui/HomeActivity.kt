package com.daily.clean.booster.ui

import android.animation.ArgbEvaluator
import android.graphics.Color
import android.os.Environment
import android.text.format.Formatter
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.daily.clean.booster.R
import com.daily.clean.booster.ad.DaiBooADUtil
import com.daily.clean.booster.base.BaseActivity
import com.daily.clean.booster.base.DBConfig
import com.daily.clean.booster.base.FiBLogEvent
import com.daily.clean.booster.core.clean.CleanData
import com.daily.clean.booster.core.clean.DaiBooCleaner
import com.daily.clean.booster.databinding.ActivityHomeBinding
import com.daily.clean.booster.utils.*
import com.lzp.dslanimator.PlayMode
import com.lzp.dslanimator.animSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class HomeActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeBinding
    override fun daibooLayoutId(): View {
        binding = ActivityHomeBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun daibooData() {
        FiBLogEvent.page_home_show()
    }

    override fun daibooView() {
        initHome()
        initMenu()
    }

    override fun daibooLoad() {
    }

    override fun onResume() {
        super.onResume()
        updateView()
    }

    override fun onPause() {
        super.onPause()
        jobScanJunk?.cancel()
        animBallJump.cancel()
    }


    fun initMenu() {
        binding.contentSetting.ivMenuBg.setOnClickListener { }
        binding.contentSetting.llContact.setOnClickListener { goContactUs() }
        binding.contentSetting.llPrivacy.setOnClickListener { goPrivacy() }
        binding.contentSetting.llRateUs.setOnClickListener { updateApp() }
        binding.contentSetting.llUpdate.setOnClickListener { updateApp() }
    }

    fun initHome() {
        binding.contentHome.titleMenu.setOnClickListener {
            binding.drawerLayout.open()
        }

        binding.contentHome.cardClean.setOnClickListener {
            FiBLogEvent.page_home_clean2()
            clean(DBConfig.DAIBOO_WORK_ID_CLEAN) }
        binding.contentHome.cardBattry.setOnClickListener {
            FiBLogEvent.page_home_battery()
            clean(DBConfig.DAIBOO_WORK_ID_BATTERY) }
        binding.contentHome.cardBoost.setOnClickListener {
            FiBLogEvent.page_home_boost()
            clean(DBConfig.DAIBOO_WORK_ID_BOOSTER)
        }
        binding.contentHome.cardCpu.setOnClickListener {
            FiBLogEvent.page_home_cpu()
            clean(DBConfig.DAIBOO_WORK_ID_CPU)
        }
        binding.contentHome.ivBall.setOnClickListener {
            FiBLogEvent.page_home_clean()
            clean(DBConfig.DAIBOO_WORK_ID_CLEAN)
        }


    }

    fun clean(workId: String) {
        when (workId) {
            DBConfig.DAIBOO_WORK_ID_CLEAN -> {
                work_junkClean()
            }

            DBConfig.DAIBOO_WORK_ID_BOOSTER,
            DBConfig.DAIBOO_WORK_ID_CPU,
            DBConfig.DAIBOO_WORK_ID_BATTERY -> {
                work_boost(workId)
            }
        }
    }

    fun main() = this@HomeActivity

    fun work_junkClean() {
        DaiBooADUtil.load(DBConfig.DAIBOO_AD_RESULT_NV, main())
        DaiBooADUtil.load(DBConfig.DAIBOO_AD_CLEAN_IV, main())
        main().goJunkCleanScanning(DBConfig.DAIBOO_ACTION_FROM_MAIN)
    }

    fun work_boost(id: String) {
        DaiBooADUtil.load(DBConfig.DAIBOO_AD_RESULT_NV, main())
        DaiBooADUtil.load(DBConfig.DAIBOO_AD_CLEAN_IV, main())
        main().goBoosting(id, actionStr = DBConfig.DAIBOO_ACTION_FROM_MAIN)
    }


    var lastScanTime = 0L
    fun updateView() {
        if (DaiBooMK.isNeedClean()) {
            if (System.currentTimeMillis() - lastScanTime > 2.mm) {
                binding.contentHome.run {
                    viewJunk.isVisible = true
                    ivScan.isVisible = false
                    clBall.translationY = 0f
                    ivBall.alpha = 1f
                    ivBallOrange.alpha = 0f
                    tvJunkSize.setTextColor(getColor(R.color.scan))
                    tvJunkSize.text = "0"
                    tvJunkSizeUnit.text = "B"
                }
                scanJunk()
            } else {
                animBallJump.start()
            }
        } else {
            binding.contentHome.run {
                ivScan.isVisible = true
                viewJunk.isVisible = false
                ivBall.alpha = 1f
                ivBallOrange.alpha = 0f
                animBallJump.cancel()
            }
        }


    }


    var jobScanJunk: Job? = null
    var curTotalSize = 0L
    private fun scanJunk() {
        jobScanJunk = lifecycleScope.launch(Dispatchers.IO) {
            curTotalSize = 0L

            val clnoptCleaner = DaiBooCleaner(Environment.getExternalStorageDirectory())
            clnoptCleaner.run {
                initFilters(true)
                setOnScanCallback(object : DaiBooCleaner.OnScanCallback {
                    override fun onScanFiles(file: File?) {
                    }

                    override fun onJunkFiles(file: File?) {
                        curTotalSize += (file?.length() ?: 0L)
                    }

                    override fun onScanStopped() {
                        launch(Dispatchers.Main) {
                            lastScanTime = System.currentTimeMillis()
                            val all = curTotalSize + CleanData.getAppCacheFileSize() + CleanData.getRAMUseD()
                            showJunkSize2(0, all)
                            animBall.start()
                        }
                    }
                })
                scanFiles()
            }
        }
    }

    fun showJunkSize2(start: Long, size: Long) {

        var curent = start
        val per = (size - start) / 100L
        lifecycleScope.launch {
            while (curent < size) {
                delay(9)
                curent += per
                val fs = Formatter.formatFileSize(this@HomeActivity, curent).uppercase()
//                LogDM.d("scan end ${fs.trim()}")
                fs.replace(" ", "").let {
                    binding.contentHome.tvJunkSize.text = it.substring(0, it.length - 2)
                    binding.contentHome.tvJunkSizeUnit.text = it.substring(it.length - 2, it.length)
                }
            }
        }

    }


    private val animBall by lazy {
        animSet {
            alphaAnim {
                target = binding.contentHome.ivBall
                values = floatArrayOf(1f, 0f)
                repeatCount = 0
            }
            alphaAnim {
                target = binding.contentHome.ivBallOrange
                values = floatArrayOf(0f, 1f)
                repeatCount = 0

            }

            duration = 1000
            interpolator = object : LinearInterpolator() {
                override fun getInterpolation(input: Float): Float {
                    val color = ArgbEvaluator().evaluate(
                        input,
                        Color.parseColor("#3390F9"),
                        Color.parseColor("#F8762A")
                    ) as Int
                    binding.contentHome.tvJunkSize.setTextColor(color)
                    return super.getInterpolation(input)
                }
            }
            playMode = PlayMode.TOGETHER


            onEnd {
                animBallJump.start()
            }

        }

    }


    private val animBallJump by lazy {
        animSet {

            translationYAnim {
                target = binding.contentHome.clBall
                values = floatArrayOf(0f, -80f, 0f)
                repeatCount = -1
            }

            scaleYAnim {
                target = binding.contentHome.ivShadow
                values = floatArrayOf(1f, 0.9f, 1f)
                repeatCount = -1
            }
            scaleXAnim {
                target = binding.contentHome.ivShadow
                values = floatArrayOf(1f, 0.5f, 1f)
                repeatCount = -1
            }

            duration = 2000
            interpolator = LinearInterpolator()
            playMode = PlayMode.TOGETHER

        }

    }

}
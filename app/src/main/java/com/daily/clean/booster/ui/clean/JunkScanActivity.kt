package com.daily.clean.booster.ui.clean

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Environment
import android.text.format.Formatter
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.daily.clean.booster.BuildConfig
import com.daily.clean.booster.R
import com.daily.clean.booster.ads.AdsLoader
import com.daily.clean.booster.ads.conf.AdPos
import com.daily.clean.booster.appIns
import com.daily.clean.booster.base.BaseActivity
import com.daily.clean.booster.base.FirebaseEvent
import com.daily.clean.booster.base.*
import com.daily.clean.booster.core.CleanData
import com.daily.clean.booster.core.DaiBooCleaner
import com.daily.clean.booster.databinding.ActivityJunkScanBinding
import com.daily.clean.booster.ext.*
import com.daily.clean.booster.pop.Noty_KEY_SOURCE
import com.daily.clean.booster.pop.NotyWorkClean
import com.daily.clean.booster.ui.view.ScanItemLayout
import com.daily.clean.booster.utils.*
import com.lzp.dslanimator.PlayMode
import com.lzp.dslanimator.animSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class JunkScanActivity : BaseActivity<ActivityJunkScanBinding>() {

    override fun dailyBinding(): ActivityJunkScanBinding {
        return ActivityJunkScanBinding.inflate(layoutInflater)
    }

    override fun statusColor(): Int {
        return Color.parseColor("#3126F5")
    }

    override fun statusTxtColorDark(): Boolean {
        return false
    }

    override fun dailyData() {
        val tanId = intent.getStringExtra(Noty_KEY_SOURCE)
        if (intent?.action == DB_ACTION_FROM_POP_NOTY) {
            FirebaseEvent.logEvent("up_all_page")
        }
        FirebaseEvent.pageScanShow(NotyWorkClean)

        binding.titleBack.setOnClickListener { onBackPressed() }

        pathLiveData.observe(this) {
            binding.tvFilePath.text = it
        }
        //????????????
        sizeLiveData.observe(this) { size ->
            if (size> 500.MB){
                animBg.start()
            }
            val fs = Formatter.formatFileSize(this, size).uppercase()
            fs.replace(" ","").let {
                binding.tvJunkSize.text = it.substring(0, it.length - 2)
                binding.tvJunkSizeUnit.text = it.substring(it.length - 2, it.length)
            }
        }
        AdsLoader.preloadAd(this, AdPos.InsClean)
        AdsLoader.preloadAd(this, AdPos.NavResult)
    }

    override fun dailyLoad() {
        checkStoragePermission({
            initView()
        }, {
            if (it) {
                toast(R.string.access_storage_prompt.getString())
                finish()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        daiBooCleaner?.release()
    }


    private var pathLiveData = MutableLiveData<String>()
    private var sizeLiveData = MutableLiveData<Long>()
    private var isInScanning = true

    private var curTotalSize = 0L
    private var selectSizeStr = "0B"
    private var items = mutableListOf<ScanItemLayout>()

    private fun initView() {
        items.add(binding.itemAppCache)
        items.add(binding.itemApkfiles)
        items.add(binding.itemLogfiles)
        items.add(binding.itemTempfiles)
        items.add(binding.itemAdjunk)
        items.add(binding.itemAppresidual)
        items.add(binding.itemRamused)
        //????????? app
        queryApps()

        if (!DaiBooMK.isNeedClean()) {
            goClean()
            return
        }

        showLoading()
        startScan()
        items.forEach { sit ->
            sit.setOnClickListener {
                if (isInScanning) return@setOnClickListener
                CleanData.cache.forEach { junk ->
                    if (junk.name == sit.name) {
                        junk.isSelected = !junk.isSelected
                        sit.setLoadImg(if (junk.isSelected) R.mipmap.ic_choice_yes else R.mipmap.ic_choice_no)
                        updateSelectedView()
                    }
                }
            }
        }
    }

    private fun goClean() {
        if (isActivityPaused) {
            finish()
            return
        }
        if (DaiBooMK.isNeedClean()) {
            goJunkCleanPage(selectSizeStr, intent?.action)
        } else {
            binding.groupScan.visibility = View.GONE
            binding.groupComplete.visibility = View.VISIBLE
            animComplete.start()
            animComplete.onEnd {
                goCleanResult(NotyWorkClean, extra = "0B", from = intent.action)
                finish()
            }
        }
    }

    private fun updateSelectedView() {
        var selectedSize = 0L
        CleanData.cache.forEach { junk ->
            if (junk.isSelected) {
                selectedSize += junk.getSelectedSize()
            }
        }
        kotlin.runCatching {
            selectSizeStr = Formatter.formatFileSize(this, selectedSize).uppercase()
            binding.tvClean.text = getString(R.string.clean_junk_xx_up, selectSizeStr)
            binding.tvSelected.text = getString(R.string.selected_xx, selectSizeStr)

            if (selectSizeStr.replace(" ","").uppercase() == "0B"){
                binding.tvClean.alpha = 0.3f
                binding.tvClean.isClickable = false
            }else{
                binding.tvClean.alpha = 1f
                binding.tvClean.isClickable = true
            }
        }
    }

    private fun showLoading() {
        items.forEach { it.startLoad() }
    }

    private var scanJob: Job? = null
    private var daiBooCleaner: DaiBooCleaner? = null
    private fun startScan() {
        isInScanning = true
        scanJob = lifecycleScope.launch(Dispatchers.IO) {
            daiBooCleaner = DaiBooCleaner(Environment.getExternalStorageDirectory())
            daiBooCleaner?.run {

                initFilters(true)
                setOnScanCallback(object : DaiBooCleaner.OnScanCallback {
                    //???????????????
                    override fun onScanFiles(file: File?) {
                        file?.absolutePath?.let {
                            pathLiveData.postValue(it)
                        }
                    }

                    //????????????
                    override fun onJunkFiles(file: File?) {
                        try {
                            curTotalSize += (file?.length() ?: 0L)
                            sizeLiveData.postValue(curTotalSize)
                        } catch (e: Exception) {
                        }
                    }

                    override fun onScanFinished() {
                        val appCacheSize = CleanData.getAppCacheFileSize()
                        curTotalSize += appCacheSize
                        sizeLiveData.postValue(curTotalSize)
                        completeAnim()
                    }
                })
                scanFiles()
            }
        }
    }

    private fun completeAnim() {
        lifecycleScope.launch {
            items.forEachIndexed { i, it ->
                var radomTime = 300L
                //????????????
                CleanData.cache.forEach { junkItem ->
                    if (it.name == junkItem.name) {
                        if (junkItem.getSelectedSize() > 0) {
                            radomTime = (1000L..2000L).random()
                        }
                        delay(radomTime)
                        it.setSizeDes(junkItem.getSelectedSizeFormat(this@JunkScanActivity))
                        it.loadComplete()
                        if (it.name == R.string.ram_used.getString()) {
                            val ram = CleanData.getRAMUseD()
                            curTotalSize += ram
                            sizeLiveData.postValue(curTotalSize)
                        }
                    }
                }
                startProgressTimer(radomTime, target = (100 / items.size) * (i + 1))
            }

            delay(200L)
            startProgressTimer(100L, target = 100)
            updateSelectedView()
            animCleanBtn.start()
            animCleanBtn.onEnd {
                isInScanning = false
            }
            binding.tvClean.setOnClickListener {
                goClean()
            }
        }
    }


    private var progressTimer: ValueAnimator? = null
    private fun cancelProgressTimer() {
        progressTimer?.cancel()
        progressTimer = null
    }

    private fun startProgressTimer(d: Long = 1500L, target: Int) {
        cancelProgressTimer()
        progressTimer = ValueAnimator.ofInt(binding.progressScan.progress, target)
        progressTimer?.apply {
            duration = d
            interpolator = LinearInterpolator()
            addUpdateListener {
                val value = it.animatedValue as Int
                binding.progressScan.progress = value
            }
        }
        progressTimer?.start()
    }

    private val animCleanBtn by lazy {
        animSet {
            scaleXAnim {
                target = binding.tvClean
                values = floatArrayOf(0f, 1.14f, 1f)
                repeatCount = 0
            }
            scaleYAnim {
                target = binding.tvClean
                values = floatArrayOf(0f, 1.14f, 1f)
                repeatCount = 0
            }

            duration = 500
            interpolator = LinearInterpolator()
            playMode = PlayMode.TOGETHER

        }
    }
    private val animBg by lazy {
        animSet {
            alphaAnim {
                target = binding.ivBgTopBlue
                values = floatArrayOf(1f,  0f)
                repeatCount = 0
            }
            alphaAnim {
                target = binding.ivBgTopRed
                values = floatArrayOf(0f, 1f)
                repeatCount = 0
            }

            duration = 1500
            interpolator = LinearInterpolator()
            playMode = PlayMode.TOGETHER
            onEnd {
                changeStatusBarBg(Color.parseColor("#F57926"), false)
            }
        }
    }

    private val animComplete by lazy {
        animSet {
            scaleXAnim {
                target = binding.ivComplete
                values = floatArrayOf(0f, 1.14f, 1f)
                repeatCount = 0
            }
            scaleYAnim {
                target = binding.ivComplete
                values = floatArrayOf(0f, 1.14f, 1f)
                repeatCount = 0
            }

            duration = 1500
            interpolator = LinearInterpolator()
            playMode = PlayMode.TOGETHER
            onEnd {
            }
        }
    }


    private fun queryApps() {
        val intent =
            Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
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
}
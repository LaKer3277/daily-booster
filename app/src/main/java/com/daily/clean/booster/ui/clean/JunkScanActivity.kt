package com.daily.clean.booster.ui.clean

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Environment
import android.text.format.Formatter
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.daily.clean.booster.BuildConfig
import com.daily.clean.booster.App
import com.daily.clean.booster.R
import com.daily.clean.booster.ads.AdsLoader
import com.daily.clean.booster.ads.conf.AdPos
import com.daily.clean.booster.base.BaseActivity
import com.daily.clean.booster.base.FiBLogEvent
import com.daily.clean.booster.base.DBConfig
import com.daily.clean.booster.core.CleanData
import com.daily.clean.booster.core.DaiBooCleaner
import com.daily.clean.booster.databinding.ActivityJunkScanBinding
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

    override fun dailyData() {
        val tanId = intent.getStringExtra(DBConfig.DAIBOO_KEY_NOTY_ID)
        if (intent?.action == DBConfig.DAIBOO_ACTION_FROM_POP_NOTY_POP) {
            FiBLogEvent.up_all_page()
        }
        FiBLogEvent.page_scan_show(DBConfig.DAIBOO_WORK_ID_CLEAN)
        binding.titleBack.setOnClickListener { onBackPressed() }

        pathLiveData.observe(this) {
            binding.tvFilePath.text = it
        }
        //所有大小
        sizeLiveData.observe(this) { size ->
            if (size> 500.MB){
                animBg.start()
            }
            LogDB.dScan("all size = $size")
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
                goMain()
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
        //先查寻 app
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
                goCleanResult(DBConfig.DAIBOO_WORK_ID_CLEAN, extra = "0B", from = intent.action)
                finish()
            }
        }
    }

    private fun updateSelectedView() {
        var selectedSize = 0L
        CleanData.cache.forEach { junk ->
            if (junk.isSelected) {
                LogDB.dScan("item select = ${junk.getSelectedSize()}")
                selectedSize += junk.getSelectedSize()
            }
        }
        LogDB.dScan("all size select = $selectedSize")
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
                    //扫描的文件
                    override fun onScanFiles(file: File?) {
                        file?.absolutePath?.let {
                            pathLiveData.postValue(it)
                        }
                    }

                    //垃圾文件
                    override fun onJunkFiles(file: File?) {
                        try {
                            curTotalSize += (file?.length() ?: 0L)
                            sizeLiveData.postValue(curTotalSize)
                        } catch (e: Exception) {
                            LogDB.eScan("scan-${file?.absolutePath}")
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
                //显示大小
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
        LogDB.d("startProgressTimer $target")
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
                LogDB.d("boost complete...")
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
                LogDB.d("boost complete...")
            }
        }
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
}
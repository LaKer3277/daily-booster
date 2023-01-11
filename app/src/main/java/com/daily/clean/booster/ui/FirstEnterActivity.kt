package com.daily.clean.booster.ui

import android.view.View
import androidx.lifecycle.lifecycleScope
import com.daily.clean.booster.R
import com.daily.clean.booster.ad.DaiBooADUtil
import com.daily.clean.booster.base.BaseActivity
import com.daily.clean.booster.base.FiBRemoteUtil
import com.daily.clean.booster.base.DBConfig
import com.daily.clean.booster.base.FiBLogEvent
import com.daily.clean.booster.databinding.ActivityFirstBinding
import com.daily.clean.booster.tba.HttpTBA
import com.daily.clean.booster.utils.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FirstEnterActivity : BaseActivity<ActivityFirstBinding>() {

    override fun dailyBinding(): ActivityFirstBinding {
        return ActivityFirstBinding.inflate(layoutInflater)
    }

    override fun dailyData() {
        FiBLogEvent.start_first_()
    }

    override fun dailyLoad() {//下划线
        binding.layStart.tvPrivacyPolicy.showBottomLine()
        binding.layStart.tvTermofservice.showBottomLine()

        binding.layStart.tvPrivacyPolicy.setOnClickListener {
            goPrivacy()
        }

        binding.layStart.tvTermofservice.setOnClickListener {
            goTerm()
        }
        //点击
        binding.layStart.btnStart.setOnClickListener {
            showAD { goScan() }
        }
        HttpTBA.reportFirst()
        initLocalData()
    }

    private fun initLocalData() {
        FiBRemoteUtil.initFireBaseData {
            loadADS()
        }
    }

    private fun goScan() {
        binding.layStart.root.visibility = View.INVISIBLE
        binding.layScan.root.visibility = View.VISIBLE
        var pro = 0
        lifecycleScope.launch {
            while (pro < 100) {
                pro++
                binding.layScan.tvTextscan.text = R.string.scanning_boost_xx.getString("${pro}%")
                delay((30L..70L).random())
            }
            if (pro >= 100) {
                goInfo()
            }
        }
    }

    private fun goInfo() {
        binding.layScan.root.visibility = View.INVISIBLE
        binding.layInfo.root.visibility = View.VISIBLE
        DaiBooMK.saveFirstStart()

        binding.layInfo.tvSkip.setOnClickListener {
            goMain(DBConfig.DAIBOO_ACTION_FROM_FIRST)
            finish()
        }

        showUsedRAM()
    }


    private fun showUsedRAM() {
        lifecycleScope.launch {

            val usedPer = DaiBooRAMUtils.getUsedMemoryStringPerInt()
            val usedPerStr = DaiBooRAMUtils.getUsedMemoryStringPer()
            delay(500)
            binding.layInfo.ringPro.progress = usedPer.toFloat()
            //动画改变值
            binding.layInfo.tvArmUsdPer.animalText(dur = 1200, target = (usedPer))
            //空闲的
            binding.layInfo.tvFree.text = "${DaiBooRAMUtils.getFreeRAMtring()}"
            //使用了的
            binding.layInfo.tvUsed.text = "${DaiBooRAMUtils.getUsedMemoryString()}"

            binding.layInfo.tvRamUsageIsUp.setForegroundColorSpanText(this@FirstEnterActivity.getString(
                R.string.boost_now_text,
                usedPerStr
            ))
            binding.layInfo.tvBoostNow.setOnClickListener {
                goBoosting(DBConfig.DAIBOO_WORK_ID_BOOSTER, isFirst = true, actionStr = DBConfig.DAIBOO_ACTION_FROM_FIRST)
                finish()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        jobLoadOpen?.cancel()
    }

    var jobLoadOpen: Job? = null
    var loadTimes = 0
    private fun loadADS() {
        loadTimes = 0

        lifecycleScope.launch {
            DaiBooADUtil.load(DBConfig.DAIBOO_AD_OPEN, this@FirstEnterActivity)
            delay(2000)
            DaiBooADUtil.load(DBConfig.DAIBOO_AD_RESULT_NV, this@FirstEnterActivity)
            DaiBooADUtil.load(DBConfig.DAIBOO_AD_CLEAN_IV, this@FirstEnterActivity)
        }
    }

    var isShowedAD = false
    private fun showAD(next: () -> Unit) {
        lifecycleScope.launch {
            delay(100)
            DaiBooADUtil.showAD(DBConfig.DAIBOO_AD_OPEN, this@FirstEnterActivity) {
                DaiBooADUtil.load(DBConfig.DAIBOO_AD_OPEN, this@FirstEnterActivity)
                isShowedAD = it
                next.invoke()
            }
        }
    }


    override fun onBackPressed() {
    }

}
package com.daily.clean.booster.ui

import android.view.View
import androidx.lifecycle.lifecycleScope
import com.daily.clean.booster.R
import com.daily.clean.booster.ads.AdsLoader
import com.daily.clean.booster.ads.conf.AdPos
import com.daily.clean.booster.base.BaseActivity
import com.daily.clean.booster.base.*
import com.daily.clean.booster.base.FiBLogEvent
import com.daily.clean.booster.databinding.ActivityFirstBinding
import com.daily.clean.booster.ext.*
import com.daily.clean.booster.pop.NotyWorkBooster
import com.daily.clean.booster.utils.*
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
            goScan()
        }

        lifecycleScope.launch {
            delay(1000)
            AdsLoader.preloadAd(this@FirstEnterActivity, AdPos.NavResult)
            AdsLoader.preloadAd(this@FirstEnterActivity, AdPos.InsClean)
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
            goMain(DB_PAGE_FROM_FIRST)
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
                goBoosting(NotyWorkBooster, isFirst = true, actionStr = DB_PAGE_FROM_FIRST)
                finish()
            }
        }
    }

    override fun onBackPressed() {}

}
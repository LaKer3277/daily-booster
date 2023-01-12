package com.daily.clean.booster.ui

import android.content.Intent
import android.content.IntentFilter
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.blankj.utilcode.util.ActivityUtils
import com.daily.clean.booster.R
import com.daily.clean.booster.appIns
import com.daily.clean.booster.base.BaseActivity
import com.daily.clean.booster.base.*
import com.daily.clean.booster.base.FiBLogEvent
import com.daily.clean.booster.pop.NotifyManager
import com.daily.clean.booster.databinding.NotificationBigActBinding
import com.daily.clean.booster.utils.DaiBooRAMUtils
import com.daily.clean.booster.ext.getString
import com.daily.clean.booster.pop.*

class NotificationActivity : BaseActivity<NotificationBigActBinding>() {

    override fun dailyBinding(): NotificationBigActBinding {
        return NotificationBigActBinding.inflate(layoutInflater)
    }

    override fun onNewIntent(intentNew: Intent?) {
        super.onNewIntent(intent)
        intent = intentNew
        workID = intent?.getStringExtra(Noty_KEY_WORK) ?: ""
        tanID = intent?.getStringExtra(Noty_KEY_SOURCE) ?: ""
        initView()
    }

    override fun onBackPressed() {
    }

    private var workID = ""
    private var tanID = ""
    override fun dailyData() {
        workID = intent?.getStringExtra(Noty_KEY_WORK) ?: ""
        tanID = intent?.getStringExtra(Noty_KEY_SOURCE) ?: ""
        FiBLogEvent.up_ac_show(tanID)
    }

    override fun dailyLoad() {
        val params = window.attributes
        window.setGravity(Gravity.CENTER)
        params.width = (resources.displayMetrics.widthPixels * 0.98f).toInt()
        params.apply {
            height = WindowManager.LayoutParams.WRAP_CONTENT
            dimAmount = 0f
            alpha = 1f
            flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        }
        window.attributes = params
        initView()
    }

    private fun initView() {

        fun setImageViewResource(resid: Int, imgId: Int) {
            findViewById<ImageView>(resid).setImageResource(imgId)
        }

        fun setTextViewText(resid: Int, textStr: String) {
            findViewById<TextView>(resid).apply {
                text = textStr
            }
        }

        val item = NotifyManager.getPopItem(tanID)
        item?.let {
            when (it.acti_pos) {
                0 -> (binding.rlPage.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.TOP
                1 -> (binding.rlPage.layoutParams as FrameLayout.LayoutParams).gravity =
                    Gravity.CENTER
                2 -> (binding.rlPage.layoutParams as FrameLayout.LayoutParams).gravity =
                    Gravity.BOTTOM
            }
        }

        when (workID) {
            NotyWorkBooster -> {
                setImageViewResource(R.id.ivAlertIcon, R.mipmap.ic_pop_act_boost)
                setTextViewText(
                    R.id.tvAlertDescription,
                    R.string.des_boot_tan.getString(DaiBooRAMUtils.getUsedMemoryStringPer())
                )
                setTextViewText(R.id.btnWake, R.string.boost.getString())
            }
            NotyWorkCpu -> {
                setImageViewResource(R.id.ivAlertIcon, R.mipmap.ic_pop_act_cpu)
                setTextViewText(
                    R.id.tvAlertDescription,
                    R.string.des_cpu_tan.getString("${getCpuTemperature()}°C")
                )
                setTextViewText(R.id.btnWake, R.string.cool_down.getString())
            }
            NotyWorkBattery -> {
                setImageViewResource(R.id.ivAlertIcon, R.mipmap.ic_pop_act_battery)

                when (tanID) {
                    NotySourceCharge -> {
                        setTextViewText(
                            R.id.tvAlertDescription,
                            R.string.des_battery_pop_charge.getString()
                        )
                        setTextViewText(R.id.btnWake, R.string.optimize_up.getString())
                    }

                    NotySourceBattery -> {
                        setTextViewText(
                            R.id.tvAlertDescription,
                            R.string.des_battery_pop_size.getString()
                        )
                        setTextViewText(R.id.btnWake, R.string.extend_now.getString())
                    }
                    else -> {
                        setTextViewText(
                            R.id.tvAlertDescription,
                            R.string.des_battery_pop_time.getString()
                        )
                        setTextViewText(R.id.btnWake, R.string.manage.getString())
                    }
                }
            }
            NotyWorkClean -> {
                setImageViewResource(R.id.ivAlertIcon, R.mipmap.ic_pop_act_clean)
                when (tanID) {
                    NotySourceUninstall -> {
                        setTextViewText(
                            R.id.tvAlertDescription,
                            R.string.des_uninstall.getString("${(22..79).random()}MB")
                        )
                        setTextViewText(R.id.btnWake, R.string.clean_now.getString())

                    }

                    else -> {
                        setTextViewText(
                            R.id.tvAlertDescription,
                            R.string.des_clean_tan.getString(DaiBooRAMUtils.getUsedMemoryString(appIns))
                        )
                        setTextViewText(R.id.btnWake, R.string.clean.getString())
                    }
                }
            }
        }

        binding.btnWake.setOnClickListener {
            goBoost()
        }

        binding.popContent.setOnClickListener {
            goBoost()
        }

        binding.ivClose.setOnClickListener {
            finish()
        }
    }

    private fun goBoost() {
        FiBLogEvent.up_ac_click(tanID)
        ActivityUtils.finishAllActivities()
        startActivity(Intent(appIns, SplashActivity::class.java).apply {
            putExtra(Noty_KEY_WORK, workID)
            putExtra(Noty_KEY_SOURCE, tanID)
            action = (DB_ACTION_FROM_POP_NOTY)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
        finish()
    }


    private fun getCpuTemperature(): Int {
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        var temp = intent?.getIntExtra("temperature", 0) ?: 0 //电池温度
        if (temp == 0) {
            temp = (400..540).random()
        }
        return (temp * 0.1).toInt()
    }


}
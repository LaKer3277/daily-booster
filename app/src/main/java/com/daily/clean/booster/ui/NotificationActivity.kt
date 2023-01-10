package com.daily.clean.booster.ui

import android.content.Intent
import android.content.IntentFilter
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.blankj.utilcode.util.ActivityUtils
import com.daily.clean.booster.App
import com.daily.clean.booster.R
import com.daily.clean.booster.base.BaseActivity
import com.daily.clean.booster.base.DBConfig
import com.daily.clean.booster.base.FiBLogEvent
import com.daily.clean.booster.core.pop.PopCheckHelper
import com.daily.clean.booster.databinding.LayoutNotificationBigActBinding
import com.daily.clean.booster.utils.DaiBooRAMUtils
import com.daily.clean.booster.utils.getString

class NotificationActivity : BaseActivity<LayoutNotificationBigActBinding>() {

    override fun dailyBinding(): LayoutNotificationBigActBinding {
        return LayoutNotificationBigActBinding.inflate(layoutInflater)
    }

    override fun onNewIntent(intentNew: Intent?) {
        super.onNewIntent(intent)
        intent = intentNew
        dailyData()
        dailyView()
    }

    override fun onBackPressed() {
    }

    var workID = ""
    var tanID = ""
    override fun dailyData() {
        workID = intent?.getStringExtra(DBConfig.DAIBOO_KEY_WORK_ID) ?: ""
        tanID = intent?.getStringExtra(DBConfig.DAIBOO_KEY_NOTY_ID) ?: ""
        FiBLogEvent.up_ac_show(tanID)
    }

    override fun dailyView() {
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        initView()
    }

    override fun dailyLoad() {
    }

    fun setImageViewResource(resid: Int, imgId: Int) {
        findViewById<ImageView>(resid).setImageResource(imgId)
    }

    fun setTextViewText(resid: Int, textStr: String) {
        findViewById<TextView>(resid).apply {
            text = textStr
        }
    }

    fun initView() {

        val item = PopCheckHelper.getPopItem(tanID)
        item?.let {
            when (it.acti_pos) {
                0 -> (binding.rlPage.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.TOP
                1 -> (binding.rlPage.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.CENTER
                2 -> (binding.rlPage.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.BOTTOM
            }
        }

        when (workID) {
            DBConfig.DAIBOO_WORK_ID_BOOSTER -> {
                setImageViewResource(R.id.ivAlertIcon, R.mipmap.ic_pop_act_boost)
                setTextViewText(
                    R.id.tvAlertDescription,
                    R.string.des_boot_tan.getString(DaiBooRAMUtils.getUsedMemoryStringPer())
                )
                setTextViewText(R.id.btnWake, R.string.boost.getString())
            }
            DBConfig.DAIBOO_WORK_ID_CPU -> {
                setImageViewResource(R.id.ivAlertIcon, R.mipmap.ic_pop_act_cpu)
                setTextViewText(
                    R.id.tvAlertDescription,
                    R.string.des_cpu_tan.getString("${getCpuTemperature()}°C")
                )
                setTextViewText(R.id.btnWake, R.string.cool_down.getString())
            }
            DBConfig.DAIBOO_WORK_ID_BATTERY -> {
                setImageViewResource(R.id.ivAlertIcon, R.mipmap.ic_pop_act_battery)

                when (tanID) {
                    DBConfig.DAIBOO_NOTY_CHARGE -> {
                        setTextViewText(
                            R.id.tvAlertDescription,
                            R.string.des_battery_pop_charge.getString()
                        )
                        setTextViewText(R.id.btnWake, R.string.optimize_up.getString())
                    }

                    DBConfig.DAIBOO_NOTY_BATTERY -> {
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
            DBConfig.DAIBOO_WORK_ID_CLEAN -> {
                setImageViewResource(R.id.ivAlertIcon, R.mipmap.ic_pop_act_clean)
                when (tanID) {
                    DBConfig.DAIBOO_NOTY_UNINSTALL -> {
                        setTextViewText(
                            R.id.tvAlertDescription,
                            R.string.des_uninstall.getString("${(22..79).random()}MB")
                        )
                        setTextViewText(R.id.btnWake, R.string.clean_now.getString())

                    }

                    else -> {
                        setTextViewText(
                            R.id.tvAlertDescription,
                            R.string.des_clean_tan.getString(DaiBooRAMUtils.getUsedMemoryString(App.ins))
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

    fun goBoost() {
        FiBLogEvent.up_ac_click(tanID)
        ActivityUtils.finishAllActivities()
        startActivity(Intent(App.ins, SplashActivity::class.java).apply {
            putExtra(DBConfig.DAIBOO_KEY_WORK_ID, workID)
            putExtra(DBConfig.DAIBOO_KEY_NOTY_ID, tanID)
            action = (DBConfig.DAIBOO_ACTION_FROM_POP_NOTY_POP)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
        finish()
    }


    fun getCpuTemperature(): Int {
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        var temp = intent?.getIntExtra("temperature", 0) ?: 0 //电池温度
        if (temp == 0) {
            temp = (400..540).random()
        }
        return (temp * 0.1).toInt()
    }


}
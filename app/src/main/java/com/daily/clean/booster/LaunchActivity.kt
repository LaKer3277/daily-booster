package com.daily.clean.booster

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.blankj.utilcode.util.ActivityUtils
import com.daily.clean.booster.base.DBConfig
import com.daily.clean.booster.ui.FirstEnterActivity
import com.daily.clean.booster.ui.NotificationActivity
import com.daily.clean.booster.ui.SplashActivity
import com.daily.clean.booster.utils.DaiBooMK

class LaunchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (DaiBooMK.isFirstStart()) {
            ActivityUtils.startActivity(FirstEnterActivity::class.java)
        }else{
            startActivity(Intent(this, SplashActivity::class.java))
//            startActivity(Intent(this, NotificationActivity::class.java).apply {
//                putExtra(DBConfig.DAIBOO_KEY_WORK_ID,DBConfig.DAIBOO_WORK_ID_BOOSTER)
//                putExtra(DBConfig.DAIBOO_KEY_NOTY_ID,DBConfig.DAIBOO_NOTY_TIME)
//            })
        }

        finish()
    }
}
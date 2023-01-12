package com.daily.clean.booster

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.blankj.utilcode.util.ActivityUtils
import com.daily.clean.booster.ui.FirstEnterActivity
import com.daily.clean.booster.ui.SplashActivity
import com.daily.clean.booster.utils.DaiBooMK

class LaunchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (DaiBooMK.isFirstStart()) {
            ActivityUtils.startActivity(FirstEnterActivity::class.java)
        } else {
            startActivity(Intent(this, SplashActivity::class.java))
        }

        finish()
    }
}
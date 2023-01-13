package com.daily.clean.booster.base

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.viewbinding.ViewBinding
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.daily.clean.booster.App
import com.daily.clean.booster.R
import com.daily.clean.booster.ext.isRPlus
import com.daily.clean.booster.pop.Noty_KEY_SOURCE
import com.daily.clean.booster.pop.Noty_KEY_WORK
import com.daily.clean.booster.tba.HttpTBA
import com.daily.clean.booster.ui.HomeActivity
import com.daily.clean.booster.ui.clean.BoostActivity
import com.daily.clean.booster.ui.clean.CleanResultActivity
import com.daily.clean.booster.ui.clean.JunkCleanActivity
import com.daily.clean.booster.ui.clean.JunkScanActivity
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseActivity<T: ViewBinding> : AppCompatActivity() {

    abstract fun dailyBinding(): T
    open lateinit var binding: T

    abstract fun dailyData()
    abstract fun dailyLoad()
    open fun statusColor(): Int {
        return 0
    }
    open fun statusTxtColorDark(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        //防止输入法顶起底部布局
        //window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        super.onCreate(savedInstanceState)
        //屏幕适配
        dailyDensity()
        val theme = statusColor()
        if (theme != 0) {
            changeStatusBarBg(theme, statusTxtColorDark())
        }
        binding = dailyBinding()
        setContentView(binding.root)

        dailyData()
        dailyLoad()
        doSessionUpload()
    }

    companion object {
        private val uploadedSession = AtomicBoolean(false)
    }
    private fun doSessionUpload() {
        if (!uploadedSession.getAndSet(true)) {
            HttpTBA.doReportSession()
        }
    }

    var isActivityPaused: Boolean = false
    override fun onResume() {
        super.onResume()
        isActivityPaused = false
    }

    override fun onPause() {
        super.onPause()
        isActivityPaused = true
    }

    override fun onStop() {
        super.onStop()
        isActivityPaused = true
    }


    private fun dailyDensity() {
        resources.displayMetrics.apply {
            val finalHeight = heightPixels / 745f
            density = finalHeight
            scaledDensity = finalHeight
            densityDpi = (160 * finalHeight).toInt()
        }
    }


    protected fun changeStatusBarBg(statusBarColor: Int, isDarkText: Boolean) {
        val window = window
        val decorView = window.decorView
        //5.0开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = statusBarColor
        //android 6.0以上才能设置状态栏字体、图标颜色. 如果设置了透明底色又不能深色字体,状态栏就GG了
        if (isDarkText) {
            //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }


    var toast: Toast? = null
    fun showToast(msg: String, isLong: Boolean = false) {
        if (toast == null) {
            toast = Toast.makeText(
                this@BaseActivity,
                msg,
                if (isLong.not()) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
            )
        }
        toast?.let {
            it.setText(msg)
            it.show()
        }
    }

    fun openMain(workId: String = "", sourceId: String = "", actionStr: String = "") {
        startActivity(Intent(this, HomeActivity::class.java).apply {
            putExtra(Noty_KEY_WORK, workId)
            putExtra(Noty_KEY_SOURCE, sourceId)
            action.let { action = actionStr }
        })
    }


    fun goBoosting(
        work_id: String,
        isFirst: Boolean = false,
        actionStr: String? = null
    ) {
        startActivity(Intent(this, BoostActivity::class.java).apply {
            putExtra(Noty_KEY_WORK, work_id)
            putExtra(DB_KEY_IS_FIRST, isFirst)
            actionStr?.let { action = actionStr }
        })
    }

    fun goJunkScanPage(act: String? = null) {
        startActivity(Intent(this, JunkScanActivity::class.java).apply {
            act?.let { action = act }
        })
    }

    fun goJunkCleanPage(extra: String?, act: String? = null) {
        startActivity(Intent(this, JunkCleanActivity::class.java).apply {
            extra?.let { putExtra(DB_KEY_CLEAN_SIZE, it) }
            act?.let { action = act }
        })
    }

    fun goCleanResult(work_id: String, extra: String? = null, from: String? = null, isFirst: Boolean = false) {
        startActivity(Intent(this, CleanResultActivity::class.java).apply {
            putExtra(Noty_KEY_WORK, work_id)
            putExtra(DB_KEY_IS_FIRST, isFirst)
            extra?.let { putExtra(DB_KEY_CLEAN_SIZE, it) }
            from?.let { action = from }
        })
    }

    fun checkStoragePermission(
        onGranted: () -> Unit,
        onDenied: (Boolean) -> Unit
    ) {
        val isNeedFilleAll = isRPlus()

        if (isNeedFilleAll) {
            checkIsHaveAllStoragePermission(onGranted, onDenied)
        } else {
            if (XXPermissions.isGranted(this, Permission.Group.STORAGE)) {
                onGranted()
            } else {
                FirebaseEvent.logEvent("per_show")
                XXPermissions.with(this)
                    .permission(Permission.Group.STORAGE)
                    .request(object : OnPermissionCallback {
                        override fun onGranted(permissions: List<String>, all: Boolean) {
                            if (all) {
                                FirebaseEvent.logEvent("per_agree")
                                onGranted()
                            }
                        }

                        override fun onDenied(permissions: List<String>, never: Boolean) {
                            FirebaseEvent.logEvent("per_reject")
                            onDenied(true)
                        }
                    })
            }
        }

    }


    //请求 危险权限，管理外部存储权限
    private fun checkIsHaveAllStoragePermission(onGranted: () -> Unit, onDenied: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {

            MaterialDialog(this).show {
                FirebaseEvent.logEvent("per_show")
                customView(R.layout.dialog_permission_request)
                cancelable(false)
                cancelOnTouchOutside(false)
                cornerRadius(12f)
                maxWidth(literal = (this@BaseActivity.resources.displayMetrics.density * 275f).toInt())
                this.view.contentLayout.findViewById<AppCompatButton>(R.id.btnGrant)
                    .setOnClickListener {
                        dismiss()
                        App.isNotDoHotStart = true
                        XXPermissions.with(this@BaseActivity)
                            .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                            .request(object : OnPermissionCallback {
                                override fun onGranted(permissions: List<String>, all: Boolean) {
                                    if (all) {
                                        FirebaseEvent.logEvent("per_agree")
                                        onGranted()
                                    }
                                }

                                override fun onDenied(permissions: List<String>, never: Boolean) {
                                    FirebaseEvent.logEvent("per_reject")
                                    onDenied(never)
                                }
                            })
                    }
                this.view.contentLayout.findViewById<AppCompatTextView>(R.id.btnCancel)
                    .setOnClickListener {
                        dismiss()
                        FirebaseEvent.logEvent("per_reject")
                        onDenied(true)
                    }
            }
        } else {
            onGranted()
        }
    }


}



package com.daily.clean.booster.base

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.daily.clean.booster.DBApp
import com.daily.clean.booster.R
import com.daily.clean.booster.utils.isRPlus
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions

abstract class BaseActivity : AppCompatActivity() {

    abstract fun daibooLayoutId(): View

    /**
     * 初始化数据
     */
    abstract fun daibooData()

    /**
     * 初始化 View
     */
    abstract fun daibooView()

    /**
     * 开始请求
     */
    abstract fun daibooLoad()

    open fun daibooListener() {}
    open fun daiboo_initService() {}
    open fun daiboo_initReceiver() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        //防止输入法顶起底部布局
//        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        super.onCreate(savedInstanceState)
        //屏幕适配
        daibooDensity()
        //设置状态栏透明
        daibooStatusBar()
        daibooData()
        setContentView(daibooLayoutId())
        daibooView()
        daibooListener()
        daibooLoad()
    }

    var isPause: Boolean = false
    override fun onResume() {
        super.onResume()
        isPause = false
    }

    override fun onPause() {
        super.onPause()
        isPause = true
    }

    override fun onStop() {
        super.onStop()
        isPause = true
    }


    private fun daibooDensity() {
        val adm: DisplayMetrics = resources.displayMetrics
        //density = px/dp
        val td = adm.heightPixels / 760f
        // px = dp * (dpi / 160)
        val dpi = (160 * td).toInt()
        //密度
        adm.density = td
        adm.scaledDensity = td

        adm.densityDpi = dpi
    }


    fun daibooStatusBar() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            val option = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            val vis = window.decorView.systemUiVisibility
            window.decorView.systemUiVisibility = option or vis
            window.statusBarColor = Color.TRANSPARENT
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
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


    /**
     * @param isNotCheckFileManagerPermisson  是否不继续检查 文件管理权限
     */
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
                FiBLogEvent.per_show()
                XXPermissions.with(this)
                    .permission(Permission.Group.STORAGE)
                    .request(object : OnPermissionCallback {
                        override fun onGranted(permissions: List<String>, all: Boolean) {
                            if (all) {
                                FiBLogEvent.per_agree()
                                onGranted()
                            }
                        }

                        override fun onDenied(permissions: List<String>, never: Boolean) {
                            FiBLogEvent.per_reject()
                            onDenied(true)
                        }
                    })
            }
        }

    }


    //请求 危险权限，管理外部存储权限
    fun checkIsHaveAllStoragePermission(onGranted: () -> Unit, onDenied: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {

            MaterialDialog(this).show {
                FiBLogEvent.per_show()
                customView(R.layout.dialog_permission_request)
                cancelable(false)
                cancelOnTouchOutside(false)
                cornerRadius(12f)
                maxWidth(literal = (this@BaseActivity.resources.displayMetrics.density * 275f).toInt())
                this.view.contentLayout.findViewById<AppCompatButton>(R.id.btnGrant)
                    .setOnClickListener {
                        dismiss()
                        DBApp.isNotDoHotStart = true
                        XXPermissions.with(this@BaseActivity)
                            .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                            .request(object : OnPermissionCallback {
                                override fun onGranted(permissions: List<String>, all: Boolean) {
                                    if (all) {
                                        FiBLogEvent.per_agree()
                                        onGranted()
                                    }
                                }

                                override fun onDenied(permissions: List<String>, never: Boolean) {
                                    FiBLogEvent.per_reject()
                                    onDenied(never)
                                }
                            })
                    }
                this.view.contentLayout.findViewById<AppCompatTextView>(R.id.btnCancel)
                    .setOnClickListener {
                        dismiss()
                        FiBLogEvent.per_reject()
                        onDenied(true)
                    }
            }
        } else {
            onGranted()
        }
    }


}



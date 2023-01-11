package com.daily.clean.booster.ext

import android.animation.ValueAnimator
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.applovin.adview.AppLovinFullscreenActivity
import com.daily.clean.booster.BuildConfig
import com.daily.clean.booster.App
import com.daily.clean.booster.R
import com.daily.clean.booster.base.DBConfig
import com.daily.clean.booster.base.DBConfig.DAIBOO_KEY_IS_FIRST
import com.daily.clean.booster.pop.NotyWorkBattery
import com.daily.clean.booster.pop.NotyWorkBooster
import com.daily.clean.booster.pop.NotyWorkClean
import com.daily.clean.booster.pop.NotyWorkCpu
import com.daily.clean.booster.service.KeepingService
import com.daily.clean.booster.ui.*
import com.daily.clean.booster.ui.clean.BoostActivity
import com.daily.clean.booster.ui.clean.CleanResultActivity
import com.daily.clean.booster.ui.clean.JunkCleanActivity
import com.daily.clean.booster.ui.clean.JunkScanActivity
import com.daily.clean.booster.utils.work.ServiceWork
import com.google.android.gms.ads.AdActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay


@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.M)
fun isMarshmallowPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N)
fun isNougatPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N_MR1)
fun isNougatMR1Plus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
fun isOreoPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O_MR1)
fun isOreoMr1Plus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.P)
fun isPiePlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
fun isQPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
fun isRPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
fun isSPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
fun isTiramisuPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU


val Number.dip
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

val Number.mm
    get() = this.toInt() * 60 * 1000
val Number.ss
    get() = this.toInt() * 1000

val Number.MB
    get() = this.toLong() * 1024 * 1024

val Number.GB
    get() = this.toLong() * 1024 * 1024 * 1024


fun Int.getString(): String {
    return App.ins.getString(this)
}

fun Int.getString(vararg formatArgs: Any?): String {
    return App.ins.getString(this, *formatArgs)
}

suspend fun CoroutineScope.doCycle(back: (Int) -> Unit) {
    var count1 = 0
    while (count1 < 100) {
        delay((60..80).random().toLong())
        count1++
        back(count1)
    }
}

fun TextView.showBottomLine() {
    this.paintFlags = Paint.UNDERLINE_TEXT_FLAG
    this.paint.isAntiAlias = true;//抗锯齿
}

fun TextView.setForegroundColorSpanText(textStr: String, color: Int = Color.parseColor("#2E35F5")) {
    val spannableStringBuilder = SpannableStringBuilder(textStr)

    textStr.forEachIndexed { index, c ->
        if (c.isDigit() || c == '%') {
            spannableStringBuilder.setSpan(ForegroundColorSpan(color), index, (index + 1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
    this.text = spannableStringBuilder

}


fun Activity.isADActivity(): Boolean {
    return this is AdActivity || this is AppLovinFullscreenActivity
}


fun Context.startCleanService() {

    runCatching {
        if (is31Android12()) {
            val request = OneTimeWorkRequestBuilder<ServiceWork>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            WorkManager.getInstance(this)
                .enqueue(request)
        } else {
            ContextCompat.startForegroundService(this, Intent(this, KeepingService::class.java))
        }

    }
}


fun AppCompatActivity.goBoosting(
    work_id: String,
    isFirst: Boolean = false,
    actionStr: String? = null
) {
    startActivity(Intent(this, BoostActivity::class.java).apply {
        putExtra(DBConfig.DAIBOO_KEY_WORK_ID, work_id)
        putExtra(DAIBOO_KEY_IS_FIRST, isFirst)
        actionStr?.let { action = actionStr }
    })
}


fun AppCompatActivity.goJunkCleanScanning(act: String? = null) {
    startActivity(Intent(this, JunkScanActivity::class.java).apply {
        act?.let { action = act }
    })
}

fun AppCompatActivity.goJunkCleanPage(extra: String?, act: String? = null) {
    startActivity(Intent(this, JunkCleanActivity::class.java).apply {
        extra?.let { putExtra(DBConfig.DAIBOO_KEY_CLEAN_SIZE, it) }
        act?.let { action = act }
    })
}


fun Context.updateApp() {
    try {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
        intent.setPackage("com.android.vending") //这里对应的是谷歌商店，跳转别的商店改成对应的即可
        if (intent.resolveActivity(this.packageManager) != null) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            this.startActivity(intent)
        } else { //没有应用市场，通过浏览器跳转到Google Play
            val intent2 = Intent(Intent.ACTION_VIEW)
            intent2.data =
                Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
            if (intent2.resolveActivity(this.packageManager) != null) {
                intent2.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                this.startActivity(intent2)
            } else {
                //没有Google Play 也没有浏览器
            }
        }
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
    }


}


fun Context.goPrivacy() {
    val intent = Intent(this, WebActivity::class.java).apply {
        putExtra(DBConfig.DAIBOO_KEY_WEB_URL, DBConfig.DAIBOO_URL_PRIVACY)
    }
    this.startActivity(intent)
}

fun Context.goTerm() {
    val intent = Intent(this, WebActivity::class.java).apply {
        putExtra(DBConfig.DAIBOO_KEY_WEB_URL, DBConfig.DAIBOO_URL_TERM)
    }
    this.startActivity(intent)
}

fun AppCompatActivity.goMain(from: String? = null) {
    startActivity(Intent(this, HomeActivity::class.java).apply {
        from?.let { putExtra(DBConfig.DAIBOO_KEY_FROM, it) }

    })
}

fun AppCompatActivity.goCleanResult(work_id: String, extra: String? = null, from: String? = null, isFirst: Boolean = false) {

    startActivity(Intent(this, CleanResultActivity::class.java).apply {
        putExtra(DBConfig.DAIBOO_KEY_WORK_ID, work_id)
        putExtra(DAIBOO_KEY_IS_FIRST, isFirst)
        extra?.let { putExtra(DBConfig.DAIBOO_KEY_CLEAN_SIZE, it) }
        from?.let { action = from }

    })
}


fun Context.goContactUs() {
    try {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:${DBConfig.DAIBOO_EMAIL}")
        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback")
        intent.putExtra(Intent.EXTRA_TEXT, "mail content")
        this.startActivity(intent)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        Toast.makeText(
            this,
            "Contact us by email:${DBConfig.DAIBOO_EMAIL}",
            Toast.LENGTH_LONG
        ).show()
    }
}


fun Context.goShareApp() {
    try {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(
            Intent.EXTRA_TEXT,
            "https://play.google.com/store/apps/details?id=" + DBConfig.DAIBOO_APPLICATION_ID
        )
        this.startActivity(Intent.createChooser(intent, "share"))
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}


fun ProgressBar.startProgress(
    dur: Long = 5000L,
    text: TextView? = null,
    end: () -> Unit = {}
): ValueAnimator {
    val progressAnimator = ValueAnimator.ofInt(this.progress, 100).apply {
        duration = dur
        interpolator = LinearInterpolator()
        addUpdateListener {
            val value = it.animatedValue as Int
            this@startProgress.progress = value
            text?.text = "$value %"
            if (value == 100) {
                end()
            }
        }
    }
    progressAnimator?.start()
    return progressAnimator
}


fun TextView.animalText(dur: Long = 500L, start: Int = 0, target: Int = 100, change: (Int) -> Unit = {}): ValueAnimator {
    return ValueAnimator.ofInt(start, target).run {
        duration = dur
        interpolator = LinearInterpolator()
        addUpdateListener {
            val value = it.animatedValue as Int
            this@animalText.text = "$value %"
            change(value)
        }
        start()
        this
    }

}

fun Context.toast(text: CharSequence) {
    val toast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
    toast.run {
        setGravity(Gravity.CENTER, 0, 0)
        show()
    }
}

fun String.getTitleText(): String {
    return when (this) {
        NotyWorkBooster -> R.string.phone_boost.getString()
        NotyWorkCpu -> R.string.cpu_cooler.getString()
        NotyWorkBattery -> R.string.battery_saver.getString()
        NotyWorkClean -> R.string.junk_clean.getString()
        else -> ""
    }
}

fun is23Android6() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
fun is24Android7() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
fun is26Android8() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
fun is28Android9() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
fun is29Android10() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
fun is30Android11() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
fun is31Android12() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
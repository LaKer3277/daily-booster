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
import com.applovin.adview.AppLovinFullscreenActivity
import com.daily.clean.booster.BuildConfig
import com.daily.clean.booster.R
import com.daily.clean.booster.appIns
import com.daily.clean.booster.base.*
import com.daily.clean.booster.pop.*
import com.daily.clean.booster.ui.*
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
    return appIns.getString(this)
}

fun Int.getString(vararg formatArgs: Any?): String {
    return appIns.getString(this, *formatArgs)
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
        putExtra(DB_KEY_WEB_URL, DB_URL_PRIVACY)
    }
    this.startActivity(intent)
}

fun Context.goTerm() {
    val intent = Intent(this, WebActivity::class.java).apply {
        putExtra(DB_KEY_WEB_URL, DB_URL_TERM)
    }
    this.startActivity(intent)
}

fun AppCompatActivity.goMain(from: String? = null) {
    startActivity(Intent(this, HomeActivity::class.java).apply {
        from?.let { putExtra(DB_KEY_FROM, it) }

    })
}


fun Context.goContactUs() {
    try {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:${DB_EMAIL}")
        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback")
        intent.putExtra(Intent.EXTRA_TEXT, "mail content")
        this.startActivity(intent)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        Toast.makeText(
            this,
            "Contact us by email:${DB_EMAIL}",
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
            "https://play.google.com/store/apps/details?id=$DB_APPLICATION_ID"
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
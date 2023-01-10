package com.daily.clean.booster.ui

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.daily.clean.booster.base.BaseActivity
import com.daily.clean.booster.base.DBConfig
import com.daily.clean.booster.databinding.ActivityWebBinding

class WebActivity : BaseActivity<ActivityWebBinding>() {

    override fun dailyBinding(): ActivityWebBinding {
        return ActivityWebBinding.inflate(layoutInflater)
    }

    var url: String = ""
    lateinit var webview: WebView
    override fun dailyData() {
        url = intent.getStringExtra(DBConfig.DAIBOO_KEY_WEB_URL) ?: ""
    }

    override fun dailyView() {
        webview = binding.webview
    }

    override fun dailyLoad() {
        webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
            }
        }
        if (url.isNotEmpty()) {
            webview.loadUrl(url)
        } else {
            finish()
        }
    }

}
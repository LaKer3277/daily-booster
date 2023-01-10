package com.daily.clean.booster.ui

import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.daily.clean.booster.base.BaseActivity
import com.daily.clean.booster.base.DBConfig
import com.daily.clean.booster.databinding.ActivityWebBinding

class WebActivity : BaseActivity() {

    private lateinit var binding: ActivityWebBinding
    override fun daibooLayoutId(): View {
        binding = ActivityWebBinding.inflate(layoutInflater)
        return binding.root
    }

    var url: String = ""
    lateinit var webview: WebView
    override fun daibooData() {
        url = intent.getStringExtra(DBConfig.DAIBOO_KEY_WEB_URL) ?: ""
    }

    override fun daibooView() {
        webview = binding.webview
    }

    override fun daibooLoad() {
        initweb()
    }

    fun initweb() {
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
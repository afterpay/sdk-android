package com.afterpay.android.internal

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.afterpay.android.R

internal class AfterpayInfoActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_checkout)

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        webView = findViewById<WebView>(R.id.afterpay_webView)
            .setAfterpayUserAgentString()

        loadUrl()
    }

    private fun loadUrl() {
        val url = intent.getInfoUrlExtra() ?: return dismiss()
        webView.loadUrl(url)
    }

    private fun dismiss() {
        setResult(Activity.RESULT_OK)
        finish()
    }
}

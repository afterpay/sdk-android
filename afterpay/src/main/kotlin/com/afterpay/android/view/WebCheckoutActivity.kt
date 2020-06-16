package com.afterpay.android.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.afterpay.android.CheckoutStatus
import com.afterpay.android.util.getCheckoutUrlExtra
import com.afterpay.android.util.putCheckoutStatusExtra
import com.afterpay.android.util.tryOrNull

internal class WebCheckoutActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val checkoutUrl = intent.getCheckoutUrlExtra() ?: error("Checkout URL is missing")

        val webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            webViewClient = AfterpayWebViewClient(openExternalLink = ::open, completed = ::finish)
            loadUrl(checkoutUrl)
        }

        setContentView(webView)

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private fun open(url: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, url)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun finish(status: CheckoutStatus) {
        setResult(status.resultCode, Intent().putCheckoutStatusExtra(status))
        finish()
    }
}

private class AfterpayWebViewClient(
    private val openExternalLink: (Uri) -> Unit,
    private val completed: (CheckoutStatus) -> Unit
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url ?: return false
        val status = url.getQueryParameter("status")
        if (status != null) {
            completed(tryOrNull { enumValueOf<CheckoutStatus>(status) } ?: CheckoutStatus.ERROR)
        } else {
            openExternalLink(url)
        }
        return true
    }
}

private val CheckoutStatus.resultCode: Int
    get() = if (this == CheckoutStatus.CANCELLED) Activity.RESULT_CANCELED else Activity.RESULT_OK

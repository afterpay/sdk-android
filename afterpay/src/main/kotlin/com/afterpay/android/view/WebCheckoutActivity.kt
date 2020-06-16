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
import com.afterpay.android.getCheckoutUrlExtra
import com.afterpay.android.putCheckoutStatusExtra
import com.afterpay.android.util.tryOrNull

internal class WebCheckoutActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val checkoutUrl = intent.getCheckoutUrlExtra() ?: error("Checkout URL is missing")

        val webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            webViewClient = AfterpayWebViewClient { status ->
                if (status == CheckoutStatus.CANCELLED) {
                    setResult(Activity.RESULT_CANCELED)
                } else {
                    setResult(Activity.RESULT_OK, Intent().putCheckoutStatusExtra(status))
                }
                finish()
            }
            loadUrl(checkoutUrl)
        }

        setContentView(webView)

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }
}

private class AfterpayWebViewClient(
    private val completion: (CheckoutStatus) -> Unit
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if (request?.url?.host == "www.afterpay-merchant.com") {
            completion(request.url.checkoutStatus ?: CheckoutStatus.ERROR)
            return true
        }
        return super.shouldOverrideUrlLoading(view, request)
    }
}

private val Uri.checkoutStatus: CheckoutStatus?
    get() = getQueryParameter("status")?.let {
        tryOrNull { enumValueOf<CheckoutStatus>(it) }
    }

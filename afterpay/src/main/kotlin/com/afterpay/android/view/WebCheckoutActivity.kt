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
import com.afterpay.android.util.getCheckoutUrlExtra
import com.afterpay.android.util.putOrderTokenExtra

internal class WebCheckoutActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val checkoutUrl = requireNotNull(intent.getCheckoutUrlExtra()) { "Checkout URL is missing" }

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
        when (status) {
            is CheckoutStatus.Success -> {
                setResult(Activity.RESULT_OK, Intent().putOrderTokenExtra(status.orderToken))
            }
            CheckoutStatus.Cancelled -> {
                setResult(Activity.RESULT_CANCELED)
            }
        }
        finish()
    }
}

private class AfterpayWebViewClient(
    private val openExternalLink: (Uri) -> Unit,
    private val completed: (CheckoutStatus) -> Unit
) : WebViewClient() {
    private val linksToOpenExternally = listOf("privacy-policy", "terms-of-service")

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url ?: return false
        val status = CheckoutStatus.fromUrl(url)

        return when {
            status != null -> {
                completed(status)
                true
            }

            linksToOpenExternally.contains(url.lastPathSegment) -> {
                openExternalLink(url)
                true
            }

            else -> false
        }
    }
}

private sealed class CheckoutStatus {
    data class Success(val orderToken: String) : CheckoutStatus()
    object Cancelled : CheckoutStatus()

    companion object {
        fun fromUrl(url: Uri): CheckoutStatus? = when (url.getQueryParameter("status")) {
            "SUCCESS" -> url.getQueryParameter("orderToken")?.let(::Success)
            "CANCELLED" -> Cancelled
            else -> null
        }
    }
}

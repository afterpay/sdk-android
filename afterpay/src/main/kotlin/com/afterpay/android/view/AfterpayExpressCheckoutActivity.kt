package com.afterpay.android.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.afterpay.android.CancellationStatus
import com.afterpay.android.R
import com.afterpay.android.internal.getCheckoutUrlExtra
import com.afterpay.android.internal.putCancellationStatusExtra
import com.afterpay.android.internal.putOrderTokenExtra

internal class AfterpayExpressCheckoutActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_checkout)

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        webView = findViewById<WebView>(R.id.afterpay_webView).apply {
            settings.javaScriptEnabled = true
            settings.setSupportMultipleWindows(true)
            webViewClient = AfterpayExpressWebViewClient(
                openExternalLink = ::open,
                receivedError = ::handleError,
                completed = ::finish
            )
            webChromeClient = AfterpayExpressWebChromeClient()
        }

        loadCheckoutUrl()
    }

    override fun onDestroy() {
        // Prevent WebView from leaking memory when the Activity is destroyed.
        // The leak appears when enabling JavaScript and is fixed by disabling it.
        webView.apply {
            stopLoading()
            settings.javaScriptEnabled = false
        }

        super.onDestroy()
    }

    override fun onBackPressed() {
        finish(CancellationStatus.USER_INITIATED)
    }

    private fun loadCheckoutUrl() {
        val checkoutUrl = intent.getCheckoutUrlExtra()
            ?: return finish(CancellationStatus.NO_CHECKOUT_URL)

        webView.loadUrl(checkoutUrl)

        // if (AfterpayCheckoutActivity.validCheckoutUrls.contains(Uri.parse(checkoutUrl).host)) {
        //     webView.loadUrl(checkoutUrl, mapOf("X-Afterpay-SDK" to AfterpayCheckoutActivity.versionHeader))
        // } else {
        //     finish(CancellationStatus.INVALID_CHECKOUT_URL)
        // }
    }

    private fun open(url: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, url)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun handleError() {
        // Clear default system error from the web view.
        webView.loadUrl("about:blank")

        AlertDialog.Builder(this)
            .setTitle(R.string.afterpay_load_error_title)
            .setMessage(R.string.afterpay_load_error_message)
            .setPositiveButton(R.string.afterpay_load_error_retry) { dialog, _ ->
                loadCheckoutUrl()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.afterpay_load_error_cancel) { dialog, _ ->
                dialog.cancel()
            }
            .setOnCancelListener {
                finish(CancellationStatus.USER_INITIATED)
            }
            .show()
    }

    private fun finish(status: ExpressCheckoutStatus) {
        when (status) {
            is ExpressCheckoutStatus.Success -> {
                setResult(Activity.RESULT_OK, Intent().putOrderTokenExtra(status.orderToken))
                finish()
            }
            ExpressCheckoutStatus.Cancelled -> {
                finish(CancellationStatus.USER_INITIATED)
            }
        }
    }

    private fun finish(status: CancellationStatus) {
        setResult(Activity.RESULT_CANCELED, Intent().putCancellationStatusExtra(status))
        finish()
    }
}

private class AfterpayExpressWebViewClient(
    private val openExternalLink: (Uri) -> Unit,
    private val receivedError: () -> Unit,
    private val completed: (ExpressCheckoutStatus) -> Unit
) : WebViewClient() {
    private val linksToOpenExternally = listOf("privacy-policy", "terms-of-service")

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url ?: return false
        val status = ExpressCheckoutStatus.fromUrl(url)

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

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        if (request?.isForMainFrame == true) {
            receivedError()
        }
    }
}

private class AfterpayExpressWebChromeClient : WebChromeClient() {
    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
    }
}

private sealed class ExpressCheckoutStatus {
    data class Success(val orderToken: String) : ExpressCheckoutStatus()
    object Cancelled : ExpressCheckoutStatus()

    companion object {
        fun fromUrl(url: Uri): ExpressCheckoutStatus? = when (url.getQueryParameter("status")) {
            "SUCCESS" -> url.getQueryParameter("orderToken")?.let(::Success)
            "CANCELLED" -> Cancelled
            else -> null
        }
    }
}

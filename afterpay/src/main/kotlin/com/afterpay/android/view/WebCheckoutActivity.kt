package com.afterpay.android.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.afterpay.android.BuildConfig
import com.afterpay.android.CancellationStatus
import com.afterpay.android.R
import com.afterpay.android.util.getCheckoutUrlExtra
import com.afterpay.android.util.putCancellationStatusExtra
import com.afterpay.android.util.putOrderTokenExtra

internal class WebCheckoutActivity : AppCompatActivity() {
    private companion object {
        val validCheckoutUrls = listOf("portal.afterpay.com", "portal.sandbox.afterpay.com")
        const val versionHeader = "${BuildConfig.VERSION_NAME}-android"
    }

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_checkout)

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        webView = findViewById<WebView>(R.id.afterpay_webView).apply {
            settings.javaScriptEnabled = true
            webViewClient = AfterpayWebViewClient(
                openExternalLink = ::open,
                receivedError = ::handleError,
                completed = ::finish
            )
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

        if (validCheckoutUrls.contains(Uri.parse(checkoutUrl).host)) {
            webView.loadUrl(checkoutUrl, mapOf("X-Afterpay-SDK" to versionHeader))
        } else {
            finish(CancellationStatus.INVALID_CHECKOUT_URL)
        }
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
            .setTitle(R.string.load_error_title)
            .setMessage(R.string.load_error_message)
            .setPositiveButton(R.string.load_error_retry) { dialog, _ ->
                loadCheckoutUrl()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.load_error_cancel) { dialog, _ ->
                dialog.cancel()
            }
            .setOnCancelListener {
                finish(CancellationStatus.USER_INITIATED)
            }
            .show()
    }

    private fun finish(status: CheckoutStatus) {
        when (status) {
            is CheckoutStatus.Success -> {
                setResult(Activity.RESULT_OK, Intent().putOrderTokenExtra(status.orderToken))
                finish()
            }
            CheckoutStatus.Cancelled -> {
                finish(CancellationStatus.USER_INITIATED)
            }
        }
    }

    private fun finish(status: CancellationStatus) {
        setResult(Activity.RESULT_CANCELED, Intent().putCancellationStatusExtra(status))
        finish()
    }
}

private class AfterpayWebViewClient(
    private val openExternalLink: (Uri) -> Unit,
    private val receivedError: () -> Unit,
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

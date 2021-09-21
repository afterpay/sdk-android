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
import com.afterpay.android.Afterpay
import com.afterpay.android.CancellationStatus
import com.afterpay.android.R
import com.afterpay.android.internal.getCheckoutUrlExtra
import com.afterpay.android.internal.putCancellationStatusExtra
import com.afterpay.android.internal.putOrderTokenExtra
import com.afterpay.android.internal.setAfterpayUserAgentString

internal class AfterpayCheckoutActivity : AppCompatActivity() {

    private companion object {

        val validCheckoutUrls = listOf(
            "portal.afterpay.com",
            "portal.sandbox.afterpay.com",
            "portal.clearpay.co.uk",
            "portal.sandbox.clearpay.co.uk"
        )
    }

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_checkout)

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        webView = findViewById<WebView>(R.id.afterpay_webView).apply {
            setAfterpayUserAgentString()
            settings.javaScriptEnabled = true
            settings.setSupportMultipleWindows(true)
            webViewClient = AfterpayWebViewClient(
                receivedError = ::handleError,
                completed = ::finish
            )
            webChromeClient = AfterpayWebChromeClient(openExternalLink = ::open)
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
            webView.loadUrl(checkoutUrl)
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
            .setTitle(R.string.afterpay_load_error_title)
            .setMessage(String.format(
                resources.getString(R.string.afterpay_load_error_message),
                resources.getString(Afterpay.brand.serviceName)
            ))
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
    private val receivedError: () -> Unit,
    private val completed: (CheckoutStatus) -> Unit
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url ?: return false
        val status = CheckoutStatus.fromUrl(url)

        return when {
            status != null -> {
                completed(status)
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

private class AfterpayWebChromeClient(
    private val openExternalLink: (Uri) -> Unit
) : WebChromeClient() {
    companion object {
        const val URL_KEY = "url"
    }

    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        val hrefMessage = view?.handler?.obtainMessage()
        view?.requestFocusNodeHref(hrefMessage)

        val url = hrefMessage?.data?.getString(URL_KEY)
        url?.let { openExternalLink(Uri.parse(it)) }

        return false
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

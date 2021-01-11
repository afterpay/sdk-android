package com.afterpay.android.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebView.WebViewTransport
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.afterpay.android.CancellationStatus
import com.afterpay.android.R
import com.afterpay.android.internal.getCheckoutUrlExtra
import com.afterpay.android.internal.putCancellationStatusExtra
import com.afterpay.android.internal.putOrderTokenExtra
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

internal class AfterpayExpressCheckoutActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_checkout)

        WebView.setWebContentsDebuggingEnabled(true)

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val checkoutUrl = intent.getCheckoutUrlExtra()
            ?: return finish(CancellationStatus.NO_CHECKOUT_URL)

        val frameLayout = findViewById<FrameLayout>(R.id.afterpay_webView_frame_layout)

        webView = findViewById<WebView>(R.id.afterpay_webView).apply {
            settings.javaScriptEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.setSupportMultipleWindows(true)
            webViewClient = AfterpayExpressWebViewClient(
                checkoutUrl = checkoutUrl,
                openExternalLink = ::open,
                receivedError = ::handleError,
                completed = ::finish
            )
            webChromeClient = AfterpayExpressWebChromeClient(
                context = this@AfterpayExpressCheckoutActivity,
                viewGroup = frameLayout
            )
            addJavascriptInterface(
                AfterpayExpressJavascriptInterface(
                    this@AfterpayExpressCheckoutActivity,
                    this
                ),
                "Android"
            )
        }

        webView.loadUrl("https://afterpay.github.io/sdk-example-server/")
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

private data class AfterpayExpressMessageMeta(
    val requestId: String
)

private data class AfterpayExpressPayload(
    val name: String
)

private data class AfterpayExpressMessage(
    val type: String,
    val meta: AfterpayExpressMessageMeta,
    val payload: AfterpayExpressPayload
)

private class AfterpayExpressJavascriptInterface(
    val activity: Activity,
    val webView: WebView
) {
    @JavascriptInterface
    fun postMessage(json: String) {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(AfterpayExpressMessage::class.java)
        val message = adapter.fromJson(json) ?: return

        val javascript =
            """
            someSpecialName(
            {
              meta: {
                requestId: "${message.meta.requestId}"
              },
              payload: [
                {
                  id: "standard",
                  name: "Standard",
                  description: "3 - 5 days",
                  shippingAmount: {
                    amount: "0.00",
                    currency: "AUD"
                  },
                  orderAmount: {
                    amount: "50.00",
                    currency: "AUD"
                  }
                },
                {
                  id: "priority",
                  name: "Priority",
                  description: "Next business day",
                  shippingAmount: {
                    amount: "10.00",
                    currency: "AUD"
                  },
                  orderAmount: {
                    amount: "60.00",
                    currency: "AUD"
                  }
                }
              ]
            },
            "https://portal.sandbox.afterpay.com"
            )
            """

        activity.runOnUiThread {
            webView.evaluateJavascript(javascript) {}
        }
    }
}

private class AfterpayExpressWebViewClient(
    private val checkoutUrl: String,
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

    override fun onPageFinished(view: WebView?, url: String?) {
        view?.evaluateJavascript("openAfterpay('$checkoutUrl');") { result ->
            print(result)
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

private class AfterpayExpressWebChromeClient(
    private val context: Context,
    private val viewGroup: ViewGroup
) : WebChromeClient() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        val newWebView = WebView(context).apply {
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
        }

        viewGroup.addView(newWebView)

        val transport = resultMsg?.obj as? WebViewTransport ?: return false
        transport.webView = newWebView
        resultMsg.sendToTarget()

        return true
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

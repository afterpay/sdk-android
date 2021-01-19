package com.afterpay.android.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.Base64
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebView.INVISIBLE
import android.webkit.WebView.VISIBLE
import android.webkit.WebView.WebViewTransport
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.afterpay.android.CancellationStatus
import com.afterpay.android.R
import com.afterpay.android.internal.AfterpayCheckoutMessage
import com.afterpay.android.internal.Html
import com.afterpay.android.internal.ShippingAddressMessage
import com.afterpay.android.internal.ShippingOptionMessage
import com.afterpay.android.internal.ShippingOptionsMessage
import com.afterpay.android.internal.getCheckoutUrlExtra
import com.afterpay.android.internal.putCancellationStatusExtra
import com.afterpay.android.internal.putOrderTokenExtra
import com.afterpay.android.model.Money
import com.afterpay.android.model.ShippingOption
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

internal class AfterpayExpressCheckoutActivity : AppCompatActivity() {
    private lateinit var bootstrapWebView: WebView
    private lateinit var loadingWebView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_express_web_checkout)

        WebView.setWebContentsDebuggingEnabled(true)

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val checkoutUrl = intent.getCheckoutUrlExtra()
            ?: return finish(CancellationStatus.NO_CHECKOUT_URL)

        val checkoutUri = Uri.parse(checkoutUrl)
            .buildUpon()
            .appendQueryParameter("isWindowed", "true")
            .build()

        val frameLayout = findViewById<FrameLayout>(R.id.afterpay_webView_frame_layout)

        loadingWebView = findViewById<WebView>(R.id.afterpay_loadingWebView).apply {
            val htmlData = Base64.encodeToString(Html.loading.toByteArray(), Base64.NO_PADDING)
            loadData(htmlData, "text/html", "base64")
        }

        val activity = this

        bootstrapWebView = findViewById<WebView>(R.id.afterpay_webView).apply {
            settings.javaScriptEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.setSupportMultipleWindows(true)

            webViewClient = BootstrapWebViewClient(checkoutUri, ::handleError)
            webChromeClient = BootstrapWebChromeClient(
                context = activity,
                viewGroup = frameLayout,
                onPageFinished = { frameLayout.removeView(loadingWebView) }
            )

            val javascriptInterface = BootstrapJavascriptInterface(activity, this, checkoutUri)
            addJavascriptInterface(javascriptInterface, "Android")

            loadUrl("https://afterpay.github.io/sdk-example-server/")
        }
    }

    override fun onDestroy() {
        // Prevent WebView from leaking memory when the Activity is destroyed.
        // The leak appears when enabling JavaScript and is fixed by disabling it.
        bootstrapWebView.apply {
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

        bootstrapWebView.loadUrl(checkoutUrl)

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
        bootstrapWebView.loadUrl("about:blank")

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

private class BootstrapWebViewClient(
    private val checkoutUri: Uri,
    private val receivedError: () -> Unit
) : WebViewClient() {
    override fun onPageFinished(view: WebView?, url: String?) {
        view?.evaluateJavascript("openAfterpay('$checkoutUri');") { result ->
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

private class BootstrapWebChromeClient(
    private val context: Context,
    private val viewGroup: ViewGroup,
    private val onPageFinished: () -> Unit
) : WebChromeClient() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        val webView = WebView(context)
        webView.visibility = INVISIBLE
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                webView.visibility = VISIBLE
                onPageFinished()
            }
        }

        viewGroup.addView(webView)

        val transport = resultMsg?.obj as? WebViewTransport ?: return false
        transport.webView = webView
        resultMsg.sendToTarget()

        return true
    }
}

private class BootstrapJavascriptInterface(
    val activity: Activity,
    val webView: WebView,
    val checkoutUri: Uri
) {
    @JavascriptInterface
    fun postMessage(json: String) {
        val polymorphicJsonAdapterFactory = PolymorphicJsonAdapterFactory
            .of(AfterpayCheckoutMessage::class.java, "type")
            .withSubtype(ShippingAddressMessage::class.java, "onShippingAddressChange")
            .withSubtype(ShippingOptionMessage::class.java, "onShippingOptionChange")
            .withSubtype(ShippingOptionsMessage::class.java, "onShippingOptionsChange")

        val moshi = Moshi.Builder()
            .add(polymorphicJsonAdapterFactory)
            .add(KotlinJsonAdapterFactory())
            .build()

        val adapter = moshi.adapter(AfterpayCheckoutMessage::class.java)
        val message = adapter.fromJson(json) ?: return

        when (message) {
            is ShippingAddressMessage -> {
                val shippingOptions = listOf(
                    ShippingOption(
                        "standard",
                        "Standard",
                        "",
                        Money("0.00", "AUD"),
                        Money("50.00", "AUD"),
                        null
                    ),
                    ShippingOption(
                        "priority",
                        "Priority",
                        "Next business day",
                        Money("10.00", "AUD"),
                        Money("60.00", "AUD"),
                        null
                    )
                )

                val shippingOptionsMessage = ShippingOptionsMessage(message.meta, shippingOptions)
                val shippingOptionsJson = adapter.toJson(shippingOptionsMessage)
                val targetUrl = checkoutUri.buildUpon().clearQuery().build().toString()
                val javascript = "postCheckoutMessage('${shippingOptionsJson}', '${targetUrl}');"

                activity.runOnUiThread {
                    webView.evaluateJavascript(javascript) {}
                }
            }
            else -> {
            }
        }
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

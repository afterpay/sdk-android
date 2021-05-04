package com.afterpay.android.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.Base64
import android.util.Log
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
import com.afterpay.android.Afterpay
import com.afterpay.android.CancellationStatus
import com.afterpay.android.CancellationStatus.NO_CHECKOUT_HANDLER
import com.afterpay.android.R
import com.afterpay.android.internal.AfterpayCheckoutCompletion
import com.afterpay.android.internal.AfterpayCheckoutMessage
import com.afterpay.android.internal.AfterpayCheckoutV2
import com.afterpay.android.internal.CheckoutLogMessage
import com.afterpay.android.internal.Html
import com.afterpay.android.internal.ShippingAddressMessage
import com.afterpay.android.internal.ShippingOptionMessage
import com.afterpay.android.internal.getCheckoutV2OptionsExtra
import com.afterpay.android.internal.putCancellationStatusExtra
import com.afterpay.android.internal.putOrderTokenExtra
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Locale

internal class AfterpayCheckoutV2Activity : AppCompatActivity() {

    private lateinit var bootstrapWebView: WebView
    private lateinit var loadingWebView: WebView
    private var checkoutWebView: WebView? = null

    private val bootstrapUrl = "https://static.afterpay.com/mobile-sdk/bootstrap/index.html"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_express_web_checkout)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        loadingWebView = findViewById<WebView>(R.id.afterpay_loadingWebView).apply {
            val htmlData = Base64.encodeToString(Html.loading.toByteArray(), Base64.NO_PADDING)
            loadData(htmlData, "text/html", "base64")
        }

        bootstrapWebView = findViewById(R.id.afterpay_webView)

        val activity = this
        val frameLayout = findViewById<FrameLayout>(R.id.afterpay_webView_frame_layout)

        bootstrapWebView.apply {
            settings.javaScriptEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.setSupportMultipleWindows(true)

            webViewClient = BootstrapWebViewClient(::loadCheckoutToken, ::handleBootstrapError)
            webChromeClient = BootstrapWebChromeClient(
                context = activity,
                viewGroup = frameLayout,
                onOpenWebView = { checkoutWebView = it },
                onPageFinished = { frameLayout.removeView(loadingWebView) },
                receivedError = ::handleCheckoutError,
                openExternalLink = ::open
            )

            val javascriptInterface = BootstrapJavascriptInterface(
                activity = activity,
                webView = this,
                complete = ::finish,
                cancel = ::finish
            )

            addJavascriptInterface(javascriptInterface, "Android")
            loadUrl(bootstrapUrl)
        }
    }

    override fun onDestroy() {
        // Prevent WebView from leaking memory when the Activity is destroyed.
        // The leak appears when enabling JavaScript and is fixed by disabling it.
        bootstrapWebView.apply {
            stopLoading()
            settings.javaScriptEnabled = false
        }

        checkoutWebView?.apply {
            stopLoading()
            settings.javaScriptEnabled = false
        }

        super.onDestroy()
    }

    override fun onBackPressed() {
        finish(CancellationStatus.USER_INITIATED)
    }

    private fun loadCheckoutToken() {
        val handler =
            Afterpay.checkoutV2Handler ?: return finish(NO_CHECKOUT_HANDLER)
        val configuration =
            Afterpay.configuration ?: return finish(CancellationStatus.NO_CONFIGURATION)
        val options = requireNotNull(intent.getCheckoutV2OptionsExtra())

        handler.didCommenceCheckout { result ->
            val token = result.getOrNull() ?: return@didCommenceCheckout handleCheckoutError()
            val checkout = AfterpayCheckoutV2(token, configuration, options)
            val checkoutJson = Json.encodeToString(checkout)

            runOnUiThread {
                bootstrapWebView.evaluateJavascript("openCheckout('$checkoutJson');", null)
            }
        }
    }

    private fun open(url: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, url)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun errorAlert(retryAction: () -> Unit) =
        AlertDialog.Builder(this)
            .setTitle(R.string.afterpay_load_error_title)
            .setMessage(R.string.afterpay_load_error_message)
            .setPositiveButton(R.string.afterpay_load_error_retry) { dialog, _ ->
                retryAction()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.afterpay_load_error_cancel) { dialog, _ ->
                dialog.cancel()
            }
            .setOnCancelListener {
                finish(CancellationStatus.USER_INITIATED)
            }

    private fun handleBootstrapError() {
        errorAlert { bootstrapWebView.loadUrl(bootstrapUrl) }.show()
    }

    private fun handleCheckoutError() {
        // Clear default system error from the web view.
        checkoutWebView?.loadUrl("about:blank")

        errorAlert { loadCheckoutToken() }.show()
    }

    private fun finish(completion: AfterpayCheckoutCompletion) {
        when (completion.status) {
            AfterpayCheckoutCompletion.Status.SUCCESS -> {
                setResult(Activity.RESULT_OK, Intent().putOrderTokenExtra(completion.orderToken))
                finish()
            }
            AfterpayCheckoutCompletion.Status.CANCELLED -> {
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
    private val onPageFinished: () -> Unit,
    private val receivedError: () -> Unit
) : WebViewClient() {
    override fun onPageFinished(view: WebView?, url: String?) {
        onPageFinished()
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
    private val onOpenWebView: (WebView) -> Unit,
    private val onPageFinished: () -> Unit,
    private val receivedError: () -> Unit,
    private val openExternalLink: (Uri) -> Unit
) : WebChromeClient() {
    companion object {
        const val URL_KEY = "url"
    }

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
        webView.settings.setSupportMultipleWindows(true)
        webView.settings.domStorageEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                webView.visibility = VISIBLE
                onPageFinished()
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

        webView.webChromeClient = object : WebChromeClient() {
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

        viewGroup.addView(webView)

        val transport = resultMsg?.obj as? WebViewTransport ?: return false
        transport.webView = webView
        resultMsg.sendToTarget()

        onOpenWebView(webView)

        return true
    }
}

private class BootstrapJavascriptInterface(
    private val activity: Activity,
    private val webView: WebView,
    private val complete: (AfterpayCheckoutCompletion) -> Unit,
    private val cancel: (CancellationStatus) -> Unit
) {
    @JavascriptInterface
    fun postMessage(json: String) {
        val checkoutMessage = runCatching { Json.decodeFromString<AfterpayCheckoutMessage>(json) }
            .getOrNull()

        if (checkoutMessage == null) {
            runCatching { Json.decodeFromString<AfterpayCheckoutCompletion>(json) }
                .getOrNull()
                ?.let(complete)
        } else {
            val handler = Afterpay.checkoutV2Handler ?: return cancel(NO_CHECKOUT_HANDLER)

            when (checkoutMessage) {
                is CheckoutLogMessage -> Log.d(
                    "AfterpayCheckoutV2",
                    checkoutMessage.payload.run { "${severity.capitalize(Locale.ROOT)}: $message" }
                )

                is ShippingAddressMessage -> handler.shippingAddressDidChange(checkoutMessage.payload) {
                    val javascript = AfterpayCheckoutMessage
                        .fromShippingOptionsResult(it, checkoutMessage.meta)
                        .let { result -> "postMessageToCheckout('${Json.encodeToString(result)}');" }
                    activity.runOnUiThread { webView.evaluateJavascript(javascript, null) }
                }

                is ShippingOptionMessage -> handler.shippingOptionDidChange(checkoutMessage.payload)

                else -> Unit
            }
        }
    }
}

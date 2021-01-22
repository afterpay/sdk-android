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
import com.afterpay.android.Afterpay
import com.afterpay.android.CancellationStatus
import com.afterpay.android.R
import com.afterpay.android.internal.AfterpayCheckoutCompletion
import com.afterpay.android.internal.AfterpayCheckoutMessage
import com.afterpay.android.internal.Html
import com.afterpay.android.internal.ShippingAddressMessage
import com.afterpay.android.internal.ShippingOptionMessage
import com.afterpay.android.internal.ShippingOptionsMessage
import com.afterpay.android.internal.getCheckoutUrlExtra
import com.afterpay.android.internal.putCancellationStatusExtra
import com.afterpay.android.internal.putOrderTokenExtra
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

internal class AfterpayInteractiveCheckoutActivity : AppCompatActivity() {

    private lateinit var bootstrapWebView: WebView
    private lateinit var loadingWebView: WebView
    private lateinit var checkoutWebView: WebView
    private lateinit var checkoutUri: Uri

    private val bootstrapUrl = "https://afterpay.github.io/sdk-example-server/"

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

            webViewClient = BootstrapWebViewClient(::loadCheckoutUri, ::handleBootstrapError)
            webChromeClient = BootstrapWebChromeClient(
                context = activity,
                viewGroup = frameLayout,
                onPageFinished = { frameLayout.removeView(loadingWebView) }
            )

            val javascriptInterface = BootstrapJavascriptInterface(
                activity,
                this,
                { this@AfterpayInteractiveCheckoutActivity.checkoutUri },
                ::finish
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

        super.onDestroy()
    }

    override fun onBackPressed() {
        finish(CancellationStatus.USER_INITIATED)
    }

    private fun loadCheckoutUri() {
        val checkoutUrl = intent.getCheckoutUrlExtra()

        val openAfterpay: (String) -> Unit = { url ->
            this.checkoutUri = Uri.parse(url).buildUpon().appendQueryParameter("isWindowed", "true").build()
            bootstrapWebView.evaluateJavascript("openAfterpay('${this.checkoutUri}');", null)
        }

        if (checkoutUrl != null) {
            openAfterpay(checkoutUrl)
        } else {
            val handler = Afterpay.interactiveCheckoutHandler ?:
                return finish(CancellationStatus.NO_CHECKOUT_HANDLER)

            handler.didCommenceCheckout { result ->
                val uri = result.getOrNull() ?: return@didCommenceCheckout handleCheckoutError()
                this.runOnUiThread { openAfterpay(uri) }
            }
        }


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
        checkoutWebView.loadUrl("about:blank")

        errorAlert { loadCheckoutUri() }.show()
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
    val checkoutUri: () -> Uri,
    val finish: (AfterpayCheckoutCompletion) -> Unit
) {
    @JavascriptInterface
    fun postMessage(json: String) {
        val checkoutMessageAdapterFactory = PolymorphicJsonAdapterFactory
            .of(AfterpayCheckoutMessage::class.java, "type")
            .withSubtype(ShippingAddressMessage::class.java, "onShippingAddressChange")
            .withSubtype(ShippingOptionMessage::class.java, "onShippingOptionChange")
            .withSubtype(ShippingOptionsMessage::class.java, "onShippingOptionsChange")

        val moshi = Moshi.Builder()
            .add(checkoutMessageAdapterFactory)
            .add(KotlinJsonAdapterFactory())
            .build()

        val messageAdapter = moshi.adapter(AfterpayCheckoutMessage::class.java)
        val message = try {
            messageAdapter.fromJson(json)
        } catch (e: Throwable) {
            null
        }

        when (message) {
            is ShippingAddressMessage -> {
                val handler = Afterpay.interactiveCheckoutHandler ?: return

                handler.shippingAddressDidChange(message.payload) {
                    val shippingOptionsMessage = ShippingOptionsMessage(message.meta, it)
                    val shippingOptionsJson = messageAdapter.toJson(shippingOptionsMessage)
                    val targetUrl = checkoutUri().buildUpon().clearQuery().build().toString()
                    val javascript = "postCheckoutMessage" +
                        "('${shippingOptionsJson}', '${targetUrl}');"

                    activity.runOnUiThread {
                        webView.evaluateJavascript(javascript) {}
                    }
                }
            }

            is ShippingOptionMessage -> {
                val handler = Afterpay.interactiveCheckoutHandler ?: return

                handler.shippingOptionDidChange(message.payload)
            }

            else -> {}
        }

        val completion = try {
            moshi.adapter(AfterpayCheckoutCompletion::class.java).fromJson(json)
        } catch (e: Throwable) {
            null
        }

        completion?.let { finish(it) }
    }
}

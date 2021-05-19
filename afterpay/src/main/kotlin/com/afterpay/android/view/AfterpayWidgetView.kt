package com.afterpay.android.view

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Message
import android.util.AttributeSet
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import com.afterpay.android.Afterpay
import com.afterpay.android.R
import com.afterpay.android.internal.Configuration
import com.afterpay.android.internal.setAfterpayUserAgentString
import com.afterpay.android.model.Money
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.math.BigDecimal

class AfterpayWidgetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    private val configuration: Configuration
        get() = checkNotNull(Afterpay.configuration) { "Afterpay configuration is not set" }

    private val json = Json { ignoreUnknownKeys = true }

    private lateinit var onUpdate: (Money, String?) -> Unit
    private lateinit var onError: (String) -> Unit

    /**
     * Initialises the Afterpay widget for the given [token] returned from a successful checkout.
     *
     * External links in the widget should be handled in [onExternalRequest]. [onUpdate] will be
     * called for any change to the total (including the initial value) while [onError] is called
     * if a problem has occurred and indicates that the order should not proceed.
     *
     * The Afterpay logo and heading are visible by default but can be hidden by setting [showLogo]
     * and [showHeading] to false.
     *
     * Results in an [IllegalStateException] if the configuration has not been set.
     */
    @JvmOverloads
    fun init(
        token: String,
        onExternalRequest: (externalUrl: Uri) -> Unit,
        onUpdate: (dueToday: Money, checksum: String?) -> Unit,
        onError: (error: String) -> Unit,
        showLogo: Boolean = true,
        showHeading: Boolean = true
    ) {
        check(token.isNotBlank()) { "Supplied token is empty" }
        this.onUpdate = onUpdate
        this.onError = onError
        configureWebView(onExternalRequest, onError) {
            loadWidget(""""$token"""", totalCost = null, showLogo, showHeading)
        }
    }

    /**
     * Initialises the Afterpay widget for the given [currency amount][totalCost].
     *
     * External links in the widget should be handled in [onExternalRequest]. [onUpdate] will be
     * called for any change to the total (including the initial value) while [onError] is called
     * if a problem has occurred and indicates that the order should not proceed.
     *
     * The Afterpay logo and heading are visible by default but can be hidden by setting [showLogo]
     * and [showHeading] to false.
     *
     * Results in an [IllegalStateException] if the configuration has not been set.
     */
    @JvmOverloads
    fun init(
        totalCost: BigDecimal,
        onExternalRequest: (externalUrl: Uri) -> Unit,
        onUpdate: (dueToday: Money, checksum: String?) -> Unit,
        onError: (error: String) -> Unit,
        showLogo: Boolean = true,
        showHeading: Boolean = true
    ) {
        this.onUpdate = onUpdate
        this.onError = onError
        configureWebView(onExternalRequest, onError) {
            loadWidget(token = null, totalCost.toAmount(), showLogo, showHeading)
        }
    }

    override fun setWebViewClient(client: WebViewClient) = Unit

    override fun setWebChromeClient(client: WebChromeClient?) = Unit

    private fun configureWebView(
        onExternalRequest: (Uri) -> Unit,
        onError: (String) -> Unit,
        onPageFinished: () -> Unit
    ) {
        setAfterpayUserAgentString()
        @SuppressLint("SetJavaScriptEnabled")
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.setSupportMultipleWindows(true)
        addJavascriptInterface(this, "Android")

        super.setWebChromeClient(
            object : WebChromeClient() {

                override fun onCreateWindow(
                    webView: WebView?,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: Message?
                ): Boolean {
                    val message = webView?.handler?.obtainMessage()
                    webView?.requestFocusNodeHref(message)
                    message?.data?.getString("url")?.let { onExternalRequest(Uri.parse(it)) }
                    return false
                }
            }
        )

        super.setWebViewClient(
            object : WebViewClient() {

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    onPageFinished()
                }

                @RequiresApi(Build.VERSION_CODES.M)
                override fun onReceivedError(
                    webView: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    checkNotNull(webView) { "A WebView was expected but not received" }
                    if (request?.isForMainFrame == true) {
                        onError(error?.description.toString().orDefaultError())
                    }
                }

                override fun onReceivedError(
                    webView: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        checkNotNull(webView) { "A WebView was expected but not received" }
                        onError(description.orDefaultError())
                    }
                }
            }
        )

        val widgetScriptUrl = context.resources.getString(R.string.afterpay_url_widget)
        val bootstrapScriptUrl = context.resources.getString(R.string.afterpay_url_widget_bootstrap)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val html = context.assets.open("widget/index.html")
                    .bufferedReader()
                    .use { it.readText() }
                    .format(widgetScriptUrl, bootstrapScriptUrl)

                withContext(Dispatchers.Main.immediate) {
                    loadDataWithBaseURL(widgetScriptUrl, html, "text/html", "base64", null)
                }
            } catch (e: IOException) {
                onError(e.message ?: "Failed to open widget bootstrap")
            }
        }
    }

    private fun loadWidget(
        token: String?,
        totalCost: String?,
        showLogo: Boolean,
        showHeading: Boolean
    ) {
        val style = "{ \"logo\": $showLogo, \"heading\": $showHeading }"
        evaluateJavascript(
            "createAfterpayWidget($token, $totalCost, \"${configuration.locale}\", $style);",
            null
        )
    }

    /**
     * Updates the Afterpay widget with the given [currency amount][totalCost].
     *
     * Results in an [IllegalStateException] if the configuration has not been set.
     */
    fun update(totalCost: BigDecimal) {
        evaluateJavascript(
            "updateAmount(${totalCost.toAmount()}, \"${configuration.locale}\");",
            null
        )
    }

    private fun BigDecimal.toAmount(): String =
        json.encodeToString(Money.serializer(), Money(this, configuration.currency))

    @JavascriptInterface
    fun postMessage(messageJson: String) {
        if (messageJson.contains("resize")) return

        runCatching {
            val event = json.decodeFromString<Event>(messageJson)

            if (event.isValid && event.amountDueToday == null) {
                error("Valid widget event does not contain amount due")
            }

            return@runCatching event
        }
            .onSuccess {
                if (it.isValid) {
                    onUpdate(it.amountDueToday!!, it.paymentScheduleChecksum)
                } else {
                    onError(it.error?.message.orDefaultError())
                }
            }
            .onFailure { onError(it.message.orDefaultError()) }
    }

    private fun String?.orDefaultError() =
        takeUnless { it.isNullOrBlank() } ?: "An unknown error occurred"

    @Serializable
    private data class Event(
        val isValid: Boolean,
        val amountDueToday: Money? = null,
        val paymentScheduleChecksum: String? = null,
        val error: Error? = null
    ) {

        @Serializable
        data class Error(
            val errorCode: String? = null,
            val errorId: String? = null,
            val message: String? = null,
            val httpStatusCode: Int? = null
        )
    }
}

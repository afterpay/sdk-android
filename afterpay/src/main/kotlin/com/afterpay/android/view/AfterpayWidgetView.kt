package com.afterpay.android.view

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import com.afterpay.android.Afterpay
import com.afterpay.android.internal.Configuration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.util.Currency

class AfterpayWidgetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    private val configuration: Configuration
        get() = checkNotNull(Afterpay.configuration) { "Afterpay configuration is not set" }

    private val json = Json { ignoreUnknownKeys = true }

    private lateinit var onUpdate: (DueToday, String?) -> Unit
    private lateinit var onError: (String?) -> Unit

    @JvmOverloads
    fun init(
        token: String,
        onExternalRequest: (externalUrl: Uri) -> Unit,
        onUpdate: (dueToday: DueToday, checksum: String?) -> Unit,
        onError: (error: String?) -> Unit,
        showLogo: Boolean = false,
        showHeading: Boolean = false
    ) {
        check(token.isNotBlank()) { "Supplied token is empty" }
        this.onUpdate = onUpdate
        this.onError = onError
        configureWebView(onExternalRequest, onError) {
            loadWidget(""""$token"""", totalCost = null, showLogo, showHeading)
        }
    }

    @JvmOverloads
    fun init(
        totalCost: BigDecimal,
        onExternalRequest: (externalUrl: Uri) -> Unit,
        onUpdate: (dueToday: DueToday, checksum: String?) -> Unit,
        onError: (error: String?) -> Unit,
        showLogo: Boolean = false,
        showHeading: Boolean = false
    ) {
        this.onUpdate = onUpdate
        this.onError = onError
        configureWebView(onExternalRequest, onError) {
            loadWidget(token = null, totalCost.toAmount(), showLogo, showHeading)
        }
    }

    private fun configureWebView(
        onExternalRequest: (Uri) -> Unit,
        onError: (String?) -> Unit,
        onPageFinished: () -> Unit
    ) {
        @SuppressLint("SetJavaScriptEnabled")
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.setSupportMultipleWindows(true)
        addJavascriptInterface(this, "Android")

        webChromeClient = object : WebChromeClient() {

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

        webViewClient = object : WebViewClient() {

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
                    onError(error?.description.toString())
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
                    onError(description)
                }
            }
        }

        loadUrl("https://afterpay.github.io/sdk-example-server/widget-bootstrap.html")
    }

    private fun loadWidget(
        token: String?,
        totalCost: String?,
        showLogo: Boolean,
        showHeading: Boolean
    ) {
        val style = """{ "logo": $showLogo, "heading": $showHeading }"""
        val script =
            """createAfterpayWidget($token, $totalCost, "${configuration.locale}", $style);"""
        evaluateJavascript(script, null)
    }

    fun update(totalCost: BigDecimal) {
        val script = """updateAmount(${totalCost.toAmount()}, "${configuration.locale}");"""
        evaluateJavascript(script, null)
    }

    private fun BigDecimal.toAmount(): String =
        json.encodeToString(DueToday.serializer(), DueToday(this, configuration.currency))

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
                    onError(it.error?.message ?: "An unknown error occurred")
                }
            }
            .onFailure { onError(it.message ?: "An unknown error occurred") }
    }

    @Serializable
    data class DueToday(
        @Serializable(with = BigDecimalSerializer::class) val amount: BigDecimal,
        @Serializable(with = CurrencySerializer::class) val currency: Currency
    ) {

        object BigDecimalSerializer : KSerializer<BigDecimal> {

            override val descriptor = PrimitiveSerialDescriptor(
                serialName = "BigDecimal",
                kind = PrimitiveKind.STRING
            )

            override fun deserialize(decoder: Decoder) = decoder.decodeString().toBigDecimal()

            override fun serialize(encoder: Encoder, value: BigDecimal) =
                encoder.encodeString(value.toPlainString())
        }

        object CurrencySerializer : KSerializer<Currency> {

            override val descriptor = PrimitiveSerialDescriptor(
                serialName = "Currency",
                kind = PrimitiveKind.STRING
            )

            override fun deserialize(decoder: Decoder): Currency =
                Currency.getInstance(decoder.decodeString())

            override fun serialize(encoder: Encoder, value: Currency) =
                encoder.encodeString(value.currencyCode)
        }
    }

    @Serializable
    private data class Event(
        val isValid: Boolean,
        val amountDueToday: DueToday? = null,
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

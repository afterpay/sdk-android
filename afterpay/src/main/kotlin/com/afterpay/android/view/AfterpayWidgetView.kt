package com.afterpay.android.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.afterpay.android.Afterpay
import com.afterpay.android.internal.Configuration
import com.afterpay.android.model.Money
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.math.BigDecimal

class AfterpayWidgetView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr),
    CoroutineScope by CoroutineScope(Dispatchers.Main.immediate) {

    private val configuration: Configuration
        get() = checkNotNull(Afterpay.configuration) { "Afterpay configuration is not set" }

    private val pageLoaded = MutableStateFlow(false)
    private val token = MutableStateFlow<String?>(null)
    private val totalCost = MutableStateFlow<BigDecimal?>(null)

    private val json = Json { ignoreUnknownKeys = true }

    init {
        combineTransform(pageLoaded.filter { it }, token, totalCost) { _, token, totalCost ->
            token ?: totalCost ?: return@combineTransform
            emit(token?.let { """"$it"""" } to totalCost?.toAmount())
        }
            .take(1)
            .onEach { (token, totalCost) -> init(token, totalCost) }
            .launchIn(this)

        @SuppressLint("SetJavaScriptEnabled")
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        addJavascriptInterface(this, "Android")

        webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                pageLoaded.tryEmit(true)
            }
        }

        loadUrl("https://afterpay.github.io/sdk-example-server/widget-bootstrap.html")
    }

    fun init(token: String) {
        check(token.isNotBlank()) { "Supplied token is empty" }
        this.token.tryEmit(token)
    }

    fun init(totalCost: BigDecimal) {
        this.totalCost.tryEmit(totalCost)
    }

    private fun init(token: String?, totalCost: String?) {
        val script =
            """createAfterpayWidget($token, $totalCost, "${configuration.locale}", { "logo": true, "heading": true });"""
        evaluateJavascript(script, null)
    }

    fun update(totalCost: BigDecimal) {
        val script = """updateAmount(${totalCost.toAmount()}, "${configuration.locale}");"""
        evaluateJavascript(script, null)
    }

    private fun BigDecimal.toAmount(): String =
        """{ "amount": "$this", "currency": "${configuration.currency}" }"""

    @JavascriptInterface
    fun postMessage(messageJson: String) {
        kotlin.runCatching { json.decodeFromString<Event>(messageJson) }
            .onSuccess { TODO() }
            .onFailure {
                it.printStackTrace()
                // TODO()
            }
    }

    @Serializable
    internal data class Event(
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

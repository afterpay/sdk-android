package com.afterpay.android.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.Base64
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.afterpay.android.CancellationStatusV3
import com.afterpay.android.R
import com.afterpay.android.internal.ApiV3
import com.afterpay.android.internal.CheckoutV3
import com.afterpay.android.internal.Html
import com.afterpay.android.internal.getCheckoutV3OptionsExtra
import com.afterpay.android.internal.putCancellationStatusExtraErrorV3
import com.afterpay.android.internal.putCancellationStatusExtraV3
import com.afterpay.android.internal.putCheckoutV3OptionsExtra
import com.afterpay.android.internal.putResultDataV3
import com.afterpay.android.internal.setAfterpayUserAgentString
import com.afterpay.android.model.CheckoutV3Tokens
import com.afterpay.android.model.CheckoutV3Data
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.net.URL

internal class AfterpayCheckoutV3Activity : AppCompatActivity() {

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
            webViewClient = AfterpayWebViewClientV3(
                receivedError = ::handleError,
                received = ::received
            )
            webChromeClient = AfterpayWebChromeClientV3(openExternalLink = ::open)
            val htmlData = Base64.encodeToString(Html.loading.toByteArray(), Base64.NO_PADDING)
            loadData(htmlData, "text/html", "base64")
        }

        lifecycleScope.launchWhenStarted {
            performCheckoutRequest()
        }
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
        received(CancellationStatusV3.USER_INITIATED)
    }

    private suspend fun performCheckoutRequest() {
        val options = intent.getCheckoutV3OptionsExtra()
            ?: return received(CancellationStatusV3.CONFIGURATION_ERROR)
        val checkoutUrl = options.checkoutUrl
            ?: return received(CancellationStatusV3.CONFIGURATION_ERROR)
        val checkoutPayload = options.checkoutPayload
            ?: return received(CancellationStatusV3.CONFIGURATION_ERROR)

        withContext(Dispatchers.IO) {
            val result = ApiV3.request<CheckoutV3.Response, String>(checkoutUrl, ApiV3.HttpVerb.POST, checkoutPayload)
            try {
                val response = result.getOrThrow()
                val builder = Uri.parse(response.redirectCheckoutUrl)
                    .buildUpon()
                    .appendQueryParameter("buyNow", (options.buyNow ?: false).toString())
                    .build()
                options.redirectUrl = URL(builder.toString())
                options.singleUseCardToken = response.singleUseCardToken
                options.token = response.token
                intent.putCheckoutV3OptionsExtra(options)
                withContext(Dispatchers.Main) {
                    loadRedirectUrl()
                }
            } catch (exception: Exception) {
                received(CancellationStatusV3.REQUEST_ERROR, exception)
            }
        }
    }

    private fun loadRedirectUrl() {
        val options = intent.getCheckoutV3OptionsExtra()
            ?: return received(CancellationStatusV3.CONFIGURATION_ERROR)
        val redirectUrl = options.redirectUrl
            ?: return received(CancellationStatusV3.CONFIGURATION_ERROR)

        webView.loadUrl(redirectUrl.toString())
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
                val options = intent.getCheckoutV3OptionsExtra()
                val retryUrl = options?.redirectUrl ?: options?.checkoutUrl
                retryUrl?.let {
                    webView.loadUrl(it.toString())
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.afterpay_load_error_cancel) { dialog, _ ->
                dialog.cancel()
            }
            .setOnCancelListener {
                received(CancellationStatusV3.USER_INITIATED)
            }
            .show()
    }

    private fun received(status: CheckoutStatusV3) {
        when (status) {
            is CheckoutStatusV3.Success -> {
                intent.getCheckoutV3OptionsExtra()?.let {
                    it.ppaConfirmToken = status.ppaConfirmToken
                    intent.putCheckoutV3OptionsExtra(it)
                }
                lifecycleScope.launch {
                    performConfirmationRequest()
                }
            }
            CheckoutStatusV3.Cancelled -> {
                received(CancellationStatusV3.USER_INITIATED)
            }
        }
    }

    private suspend fun performConfirmationRequest() {
        val options = intent.getCheckoutV3OptionsExtra()
        val token = options?.token ?: return
        val ppaConfirmToken = options.ppaConfirmToken ?: return
        val singleUseCardToken = options.singleUseCardToken ?: return
        val conformationUrl = options.confirmUrl ?: return
        val request = CheckoutV3.Confirmation.Request(
            token = token,
            ppaConfirmToken = ppaConfirmToken,
            singleUseCardToken =  singleUseCardToken
        )
        withContext(Dispatchers.IO) {
            val result: Result<CheckoutV3.Confirmation.Response> = ApiV3.request(conformationUrl, ApiV3.HttpVerb.POST, request)
            try {
                val response = result.getOrThrow()
                val data = CheckoutV3Data(
                    cardDetails = response.paymentDetails.virtualCard,
                    cardValidUntil = response.cardValidUntil,
                    tokens = CheckoutV3Tokens(
                        token = token,
                        singleUseCardToken = singleUseCardToken,
                        ppaConfirmToken = ppaConfirmToken
                    )
                )
                withContext(Dispatchers.Main) {
                    setResult(Activity.RESULT_OK, Intent().putResultDataV3(data))
                    finish()
                }

            } catch (exception: Exception) {
                received(CancellationStatusV3.REQUEST_ERROR, exception)
            }
        }

    }

    private fun received(status: CancellationStatusV3, exception: Exception? = null) {
        val intent = Intent()
        intent.putCancellationStatusExtraV3(status)
        exception?.let {
            intent.putCancellationStatusExtraErrorV3(it)
        }
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }
}

private class AfterpayWebViewClientV3(
    private val receivedError: () -> Unit,
    private val received: (CheckoutStatusV3) -> Unit
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url ?: return false
        val status = CheckoutStatusV3.fromUrl(url)

        return when {
            status != null -> {
                received(status)
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

private class AfterpayWebChromeClientV3(
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

private sealed class CheckoutStatusV3 {
    data class Success(val orderToken: String, val ppaConfirmToken: String) : CheckoutStatusV3()
    object Cancelled : CheckoutStatusV3()

    companion object {
        fun fromUrl(url: Uri): CheckoutStatusV3? = when (url.getQueryParameter("status")) {
            "SUCCESS" -> {
                val success = url.getQueryParameter("orderToken")?.let { token ->
                    url.getQueryParameter("ppaConfirmToken")?.let { confirmToken ->
                        Success(token, confirmToken)
                    }
                }
                success
            }
            "CANCELLED" -> Cancelled
            else -> null
        }
    }
}

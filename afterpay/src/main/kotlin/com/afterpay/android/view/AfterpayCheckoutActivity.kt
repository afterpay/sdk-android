/*
 * Copyright (C) 2024 Afterpay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afterpay.android.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.afterpay.android.Afterpay
import com.afterpay.android.CancellationStatus
import com.afterpay.android.R
import com.afterpay.android.internal.getCheckoutShouldLoadRedirectUrls
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
            "portal.sandbox.clearpay.co.uk",
            "checkout.clearpay.com",
            "checkout.sandbox.clearpay.com",
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
            settings.setDomStorageEnabled(true)
            webViewClient = AfterpayWebViewClient(
                receivedError = ::handleError,
                completed = ::finish,
                shouldLoadRedirectUrls = intent.getCheckoutShouldLoadRedirectUrls(),
            )
            webChromeClient = AfterpayWebChromeClient(openExternalLink = ::open)
        }

        val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish(CancellationStatus.USER_INITIATED)
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

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

    private fun loadCheckoutUrl() {
        val checkoutUrl = intent.getCheckoutUrlExtra()
            ?: return finish(CancellationStatus.NO_CHECKOUT_URL)

        if (validCheckoutUrls.contains(Uri.parse(checkoutUrl).host)) {
            webView.loadUrl(checkoutUrl)
        } else if (checkoutUrl == "LANGUAGE_NOT_SUPPORTED") {
            finish(CancellationStatus.LANGUAGE_NOT_SUPPORTED)
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
            .setTitle(Afterpay.strings.loadErrorTitle)
            .setMessage(
                String.format(
                    Afterpay.strings.loadErrorMessage,
                    resources.getString(Afterpay.brand.title),
                ),
            )
            .setPositiveButton(Afterpay.strings.loadErrorRetry) { dialog, _ ->
                loadCheckoutUrl()
                dialog.dismiss()
            }
            .setNegativeButton(Afterpay.strings.loadErrorCancel) { dialog, _ ->
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
    private val completed: (CheckoutStatus) -> Unit,
    private val shouldLoadRedirectUrls: Boolean,
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url ?: return false
        val status = CheckoutStatus.fromUrl(url)

        return when {
            status != null -> {
                if (shouldLoadRedirectUrls) {
                    return false
                }

                completed(status)
                true
            }

            else -> false
        }
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

        if (url.equals("about:blank")) {
            return
        }

        val uri = Uri.parse(url)
        val status = CheckoutStatus.fromUrl(uri)

        when {
            status != null -> {
                completed(status)
            }

            else -> {}
        }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?,
    ) {
        if (request?.isForMainFrame == true) {
            receivedError()
        }
    }
}

private class AfterpayWebChromeClient(
    private val openExternalLink: (Uri) -> Unit,
) : WebChromeClient() {
    companion object {
        const val URL_KEY = "url"
    }

    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?,
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
        fun fromUrl(url: Uri): CheckoutStatus? {
            return when (url.getQueryParameter("status")) {
                "SUCCESS" -> {
                    val token = url.getQueryParameter("orderToken") ?: url.getQueryParameter("token")
                    token?.let(::Success)
                }
                "CANCELLED" -> Cancelled
                else -> null
            }
        }
    }
}

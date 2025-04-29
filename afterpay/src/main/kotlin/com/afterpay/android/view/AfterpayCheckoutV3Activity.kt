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
import com.afterpay.android.Afterpay
import com.afterpay.android.CancellationStatusV3
import com.afterpay.android.R
import com.afterpay.android.internal.CheckoutV3ViewModel
import com.afterpay.android.internal.Html
import com.afterpay.android.internal.getCheckoutV3OptionsExtra
import com.afterpay.android.internal.putCancellationStatusExtraErrorV3
import com.afterpay.android.internal.putCancellationStatusExtraV3
import com.afterpay.android.internal.putResultDataV3
import com.afterpay.android.internal.setAfterpayUserAgentString
import kotlinx.coroutines.launch
import java.lang.Exception

internal class AfterpayCheckoutV3Activity : AppCompatActivity() {

  private lateinit var webView: WebView
  private lateinit var viewModel: CheckoutV3ViewModel

  @SuppressLint("SetJavaScriptEnabled")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_web_checkout)

    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

    viewModel = CheckoutV3ViewModel(requireNotNull(intent.getCheckoutV3OptionsExtra()))
    webView = findViewById<WebView>(R.id.afterpay_webView).apply {
      setAfterpayUserAgentString()
      settings.allowFileAccess = false
      settings.javaScriptEnabled = true
      settings.setSupportMultipleWindows(true)
      webViewClient = AfterpayWebViewClientV3(
        receivedError = ::handleError,
        received = ::received,
      )
      webChromeClient = AfterpayWebChromeClientV3(openExternalLink = ::open)
      val htmlData = Base64.encodeToString(Html.LOADING.toByteArray(), Base64.NO_PADDING)
      loadData(htmlData, "text/html", "base64")
    }

    lifecycleScope.launchWhenStarted {
      viewModel.performCheckoutRequest()
        .onSuccess { checkoutRedirectUrl ->
          webView.loadUrl(checkoutRedirectUrl.toString())
        }
        .onFailure {
          received(CancellationStatusV3.REQUEST_ERROR, it as? Exception)
        }
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
      .setMessage(Afterpay.strings.loadErrorMessage)
      .setPositiveButton(Afterpay.strings.loadErrorRetry) { dialog, _ ->
        val options = intent.getCheckoutV3OptionsExtra()
        val retryUrl = options?.redirectUrl ?: options?.checkoutUrl
        retryUrl?.let {
          webView.loadUrl(it.toString())
        }
        dialog.dismiss()
      }
      .setNegativeButton(Afterpay.strings.loadErrorCancel) { dialog, _ ->
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
        lifecycleScope.launch {
          viewModel.performConfirmationRequest(status.ppaConfirmToken)
            .onSuccess {
              setResult(Activity.RESULT_OK, Intent().putResultDataV3(it))
              finish()
            }
            .onFailure {
              received(CancellationStatusV3.REQUEST_ERROR, it as? Exception)
            }
        }
      }
      CheckoutStatusV3.Cancelled -> {
        received(CancellationStatusV3.USER_INITIATED)
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
  private val received: (CheckoutStatusV3) -> Unit,
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
    error: WebResourceError?,
  ) {
    if (request?.isForMainFrame == true) {
      receivedError()
    }
  }
}

private class AfterpayWebChromeClientV3(
  private val openExternalLink: (Uri) -> Unit,
  private val URL_KEY: String = "url",
) : WebChromeClient() {

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

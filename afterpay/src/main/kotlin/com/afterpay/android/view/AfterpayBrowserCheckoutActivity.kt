/*
 * Copyright (C) 2026 Afterpay
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

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import com.afterpay.android.CancellationStatus
import com.afterpay.android.internal.getCheckoutUrlExtra
import com.afterpay.android.internal.putCancellationStatusExtra
import com.afterpay.android.internal.putOrderTokenExtra

/**
 * Opens checkout in a Custom Tab and turns the checkout's redirect, or the customer closing the
 * tab, into the same activity result as [AfterpayCheckoutActivity].
 *
 * This activity has no UI. It stays below the Custom Tab while checkout runs.
 * [AfterpayBrowserRedirectActivity] brings it back to the top with the redirect URL, which clears
 * the Custom Tab.
 */
internal class AfterpayBrowserCheckoutActivity : Activity() {

  private companion object {
    const val BROWSER_OPENED = "browser_opened"
  }

  private var browserOpened = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    browserOpened = savedInstanceState?.getBoolean(BROWSER_OPENED) ?: false

    // A redirect that arrives without a checkout in progress, such as a stale or forged link,
    // starts a fresh instance. It has nobody to report to, so it closes without a result.
    if (savedInstanceState == null && intent.data != null) {
      finish()
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putBoolean(BROWSER_OPENED, browserOpened)
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    intent.data?.let(::handleRedirect)
  }

  override fun onResume() {
    super.onResume()
    if (isFinishing) return

    if (!browserOpened) {
      openBrowser()
    } else {
      // The customer is back here without a redirect, so they closed the Custom Tab.
      finish(CancellationStatus.USER_INITIATED)
    }
  }

  private fun openBrowser() {
    val checkoutUrl = intent.getCheckoutUrlExtra()
      ?: return finish(CancellationStatus.NO_CHECKOUT_URL)
    if (checkoutUrl == "LANGUAGE_NOT_SUPPORTED") {
      return finish(CancellationStatus.LANGUAGE_NOT_SUPPORTED)
    }

    try {
      CustomTabsIntent.Builder().build().launchUrl(this, Uri.parse(checkoutUrl))
      browserOpened = true
    } catch (e: ActivityNotFoundException) {
      // No browser is installed that can open the checkout.
      finish(CancellationStatus.INVALID_CHECKOUT_URL)
    }
  }

  private fun handleRedirect(redirectUrl: Uri) {
    when (val status = CheckoutStatus.fromUrl(redirectUrl)) {
      is CheckoutStatus.Success -> {
        // Only accept the order token of the checkout this activity opened.
        val expectedToken = intent.getCheckoutUrlExtra()?.let { Uri.parse(it).getQueryParameter("token") }
        if (expectedToken != null && expectedToken != status.orderToken) {
          return finish(CancellationStatus.INVALID_CHECKOUT_URL)
        }
        setResult(RESULT_OK, Intent().putOrderTokenExtra(status.orderToken))
        finish()
      }
      CheckoutStatus.Cancelled, null -> finish(CancellationStatus.USER_INITIATED)
    }
  }

  private fun finish(status: CancellationStatus) {
    setResult(RESULT_CANCELED, Intent().putCancellationStatusExtra(status))
    finish()
  }
}

/**
 * Receives the checkout's redirect to the app's `<applicationId>.afterpay://checkout` URL and
 * hands it to the [AfterpayBrowserCheckoutActivity] that opened the Custom Tab.
 */
internal class AfterpayBrowserRedirectActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    startActivity(
      Intent(this, AfterpayBrowserCheckoutActivity::class.java)
        .setData(intent.data)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
    )
    finish()
  }
}

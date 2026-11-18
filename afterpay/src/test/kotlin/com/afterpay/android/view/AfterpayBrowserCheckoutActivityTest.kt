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
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.afterpay.android.Afterpay
import com.afterpay.android.CancellationStatus
import com.afterpay.android.internal.putCheckoutUrlExtra
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
class AfterpayBrowserCheckoutActivityTest {

  private val checkoutUrl = "https://portal.sandbox.afterpay.com/us/checkout/?token=002.abc"

  @Test
  fun `opens the checkout in a Custom Tab`() {
    val activity = launch().get()

    val browserIntent = shadowOf(activity).nextStartedActivity
    assertEquals(Intent.ACTION_VIEW, browserIntent.action)
    assertEquals(Uri.parse(checkoutUrl), browserIntent.data)
    assertTrue(browserIntent.hasExtra(CustomTabsIntent.EXTRA_SESSION))
  }

  @Test
  fun `returns the order token from a success redirect`() {
    val controller = launch()

    controller.newIntent(redirect("status=SUCCESS&orderToken=002.abc"))

    val activity = controller.get()
    assertTrue(activity.isFinishing)
    assertEquals(Activity.RESULT_OK, shadowOf(activity).resultCode)
    assertEquals("002.abc", Afterpay.parseCheckoutSuccessResponse(shadowOf(activity).resultIntent))
  }

  @Test
  fun `rejects a success redirect for a different checkout`() {
    val controller = launch()

    controller.newIntent(redirect("status=SUCCESS&orderToken=002.other"))

    assertCancelled(controller.get(), CancellationStatus.INVALID_CHECKOUT_URL)
  }

  @Test
  fun `reports a cancel redirect as cancelled by the customer`() {
    val controller = launch()

    controller.newIntent(redirect("status=CANCELLED&orderToken=002.abc"))

    assertCancelled(controller.get(), CancellationStatus.USER_INITIATED)
  }

  @Test
  fun `reports closing the Custom Tab as cancelled by the customer`() {
    val controller = launch()

    // The Custom Tab covers this activity, then the customer closes it without a redirect.
    controller.pause().stop().restart().start().resume()

    assertCancelled(controller.get(), CancellationStatus.USER_INITIATED)
  }

  private fun launch(): ActivityController<AfterpayBrowserCheckoutActivity> {
    val intent = Intent(RuntimeEnvironment.getApplication(), AfterpayBrowserCheckoutActivity::class.java)
      .putCheckoutUrlExtra(checkoutUrl)
    return Robolectric.buildActivity(AfterpayBrowserCheckoutActivity::class.java, intent).setup()
  }

  private fun redirect(query: String) =
    Intent().setData(Uri.parse("com.afterpay.android.test.afterpay://checkout?&$query"))

  private fun assertCancelled(activity: Activity, status: CancellationStatus) {
    assertTrue(activity.isFinishing)
    assertEquals(Activity.RESULT_CANCELED, shadowOf(activity).resultCode)
    assertEquals(status, Afterpay.parseCheckoutCancellationResponse(shadowOf(activity).resultIntent))
  }
}

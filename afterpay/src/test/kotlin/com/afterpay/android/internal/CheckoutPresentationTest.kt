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
package com.afterpay.android.internal

import android.content.Intent
import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.afterpay.android.Afterpay
import com.afterpay.android.AfterpayCheckoutPresentation
import com.afterpay.android.R
import com.afterpay.android.view.AfterpayCheckoutActivity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class CheckoutPresentationTest {

  @After
  fun resetPresentation() {
    Afterpay.setCheckoutPresentation(AfterpayCheckoutPresentation.MODAL)
  }

  @Test
  fun `checkout floats in a dialog by default`() {
    val activity = launchCheckout()

    assertTrue(activity.window.isFloating)
  }

  @Test
  fun `full screen checkout is not a floating window`() {
    Afterpay.setCheckoutPresentation(AfterpayCheckoutPresentation.FULL_SCREEN)

    val activity = launchCheckout()

    assertFalse(activity.window.isFloating)
  }

  @Test
  fun `full screen checkout keeps content clear of the system bars and keyboard`() {
    Afterpay.setCheckoutPresentation(AfterpayCheckoutPresentation.FULL_SCREEN)
    val content = launchCheckout().findViewById<View>(R.id.afterpay_webView_frame_layout)

    val insets = WindowInsetsCompat.Builder()
      .setInsets(WindowInsetsCompat.Type.statusBars(), Insets.of(0, 63, 0, 0))
      .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(0, 0, 0, 48))
      .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(24, 0, 0, 0))
      .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, 800))
      .build()
    ViewCompat.dispatchApplyWindowInsets(content, insets)

    // The keyboard covers the navigation bar, so the bottom padding follows the taller keyboard.
    assertEquals(listOf(24, 63, 0, 800), content.padding())
  }

  private fun launchCheckout(): AfterpayCheckoutActivity {
    val intent = Intent(RuntimeEnvironment.getApplication(), AfterpayCheckoutActivity::class.java)
      .putCheckoutUrlExtra("https://portal.sandbox.afterpay.com/us/checkout/?token=test")
    return Robolectric.buildActivity(AfterpayCheckoutActivity::class.java, intent).setup().get()
  }

  private fun View.padding() = listOf(paddingLeft, paddingTop, paddingRight, paddingBottom)
}

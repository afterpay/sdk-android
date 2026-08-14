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
package com.afterpay.android.internal

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import com.afterpay.android.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import kotlin.math.roundToInt

@RunWith(RobolectricTestRunner::class)
class AfterpayInfoActivityTest {

  @Test
  fun `close control is exposed to accessibility services`() = withInfoActivity { activity ->
    val closeButton = activity.findViewById<ImageButton>(R.id.afterpay_close_info)

    assertEquals("Close information modal", closeButton.contentDescription)
    assertTrue(closeButton.isShown)
    assertTrue(closeButton.isClickable)
    assertEquals(activity.dp(48), closeButton.layoutParams.width)
    assertEquals(activity.dp(48), closeButton.layoutParams.height)
    assertEquals(activity.dp(12), closeButton.paddingStart)
  }

  @Test
  fun `close control sits in a header above the web view`() = withInfoActivity { activity ->
    val header = activity.findViewById<View>(R.id.afterpay_info_header)
    val webView = activity.findViewById<View>(R.id.afterpay_webView)
    val closeButton = activity.findViewById<ImageButton>(R.id.afterpay_close_info)
    val root = header.parent as LinearLayout

    assertSame(header, closeButton.parent)
    assertSame(root, webView.parent)
    assertEquals(LinearLayout.VERTICAL, root.orientation)
    assertTrue(root.indexOfChild(header) < root.indexOfChild(webView))

    // The web view takes the space the header leaves over rather than sharing it, so web content
    // can never render beneath the close control.
    val webViewParams = webView.layoutParams as LinearLayout.LayoutParams
    assertEquals(0, webViewParams.height)
    assertEquals(1f, webViewParams.weight, 0f)

    // A flat header, not a floating overlay.
    assertEquals(0f, header.elevation, 0f)
    assertEquals(0f, closeButton.elevation, 0f)
  }

  @Test
  fun `close control dismisses the modal`() = withInfoActivity { activity ->
    activity.findViewById<ImageButton>(R.id.afterpay_close_info).performClick()

    assertTrue(activity.isFinishing)
    assertEquals(Activity.RESULT_OK, shadowOf(activity).resultCode)
  }

  private fun withInfoActivity(body: (AfterpayInfoActivity) -> Unit) {
    val intent = Intent(RuntimeEnvironment.getApplication(), AfterpayInfoActivity::class.java)
      .putInfoUrlExtra("about:blank")
    Robolectric.buildActivity(AfterpayInfoActivity::class.java, intent).use { controller ->
      body(controller.setup().get())
    }
  }

  private fun Activity.dp(value: Int): Int =
    (value * resources.displayMetrics.density).roundToInt()
}

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

import android.content.Intent
import android.view.View
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class AfterpayInfoActivityTest {

  @Test
  fun `info modal provides an accessible close control`() {
    val intent = Intent(RuntimeEnvironment.getApplication(), AfterpayInfoActivity::class.java)
      .putInfoUrlExtra("about:blank")
    val activity = Robolectric.buildActivity(AfterpayInfoActivity::class.java, intent)
      .setup()
      .get()
    val closeViews = arrayListOf<View>()

    activity.window.decorView.findViewsWithText(
      closeViews,
      "Close information modal",
      View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION,
    )

    assertEquals(1, closeViews.size)
    assertTrue(closeViews.single().isShown)

    closeViews.single().performClick()

    assertTrue(activity.isFinishing)
  }
}

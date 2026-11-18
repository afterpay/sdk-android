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

import android.app.Activity
import android.view.View
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.afterpay.android.Afterpay
import com.afterpay.android.AfterpayCheckoutPresentation
import com.afterpay.android.R

private val isFullScreen: Boolean
  get() = Afterpay.checkoutPresentation == AfterpayCheckoutPresentation.FULL_SCREEN

/**
 * Replaces the manifest's floating dialog theme when checkout is full screen. Call before
 * `super.onCreate`.
 */
internal fun Activity.applyCheckoutPresentationTheme() {
  if (isFullScreen) setTheme(R.style.AfterpayFullScreen)
}

/**
 * Draws a full-screen checkout edge to edge and pads [content] so the web view stays clear of the
 * system bars, display cutout, and keyboard. Call after `setContentView`.
 */
internal fun Activity.applyCheckoutPresentationInsets(content: View) {
  if (!isFullScreen) return

  WindowCompat.setDecorFitsSystemWindows(window, false)
  @Suppress("DEPRECATION")
  window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
  WindowCompat.getInsetsController(window, window.decorView).apply {
    // Checkout pages have a white background, so the system bars use dark icons.
    isAppearanceLightStatusBars = true
    isAppearanceLightNavigationBars = true
  }

  ViewCompat.setOnApplyWindowInsetsListener(content) { view, windowInsets ->
    val insets = windowInsets.getInsets(
      WindowInsetsCompat.Type.systemBars() or
        WindowInsetsCompat.Type.displayCutout() or
        WindowInsetsCompat.Type.ime(),
    )
    view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
    WindowInsetsCompat.CONSUMED
  }
}

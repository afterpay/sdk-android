/*
 * Copyright (C) 2025 Afterpay
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
package com.example

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.afterpay.android.Afterpay
import com.afterpay.android.view.AfterpayIntroText
import com.afterpay.android.view.AfterpayLogoType
import com.afterpay.android.view.AfterpayPriceBreakdown
import com.afterpay.android.view.AfterpayWidgetStyle
import com.example.api.GetConfigurationResponse
import com.example.api.merchantApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.Locale

class AfterpayUiGalleryActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.afterpay_ui_widgets)

    val darkContainer = findViewById<LinearLayout>(R.id.price_breakdown_container_dark)
    val lightContainer = findViewById<LinearLayout>(R.id.price_breakdown_container_light)

    // Populate both containers with price breakdown widgets
    populatePriceBreakdowns(darkContainer, isDarkBackground = true)
    populatePriceBreakdowns(lightContainer, isDarkBackground = false)

    // This is needed so that the UI gets inflated.
    // Right now an AP server needs to for the UI to be inflated.
    lifecycleScope.launch {
      getConfiguration()
    }
  }

  private fun populatePriceBreakdowns(container: LinearLayout, isDarkBackground: Boolean) {
    val captionColor = if (isDarkBackground) 0xFF999999.toInt() else 0xFF666666.toInt()
    val labelColor = if (isDarkBackground) 0xFFCCCCCC.toInt() else 0xFF333333.toInt()

    // Helper to add a price breakdown with caption
    fun addPriceBreakdown(
      caption: String,
      configure: AfterpayPriceBreakdown.() -> Unit,
    ) {
      try {
        val breakdownView = AfterpayPriceBreakdown(this).apply {
          totalAmount = BigDecimal("100.00")
          configure()
        }
        val breakdownParams = LinearLayout.LayoutParams(
          LinearLayout.LayoutParams.WRAP_CONTENT,
          LinearLayout.LayoutParams.WRAP_CONTENT,
        )
        container.addView(breakdownView, breakdownParams)

        val captionView = TextView(this).apply {
          text = caption
          setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
          setTextColor(captionColor)
        }
        val captionParams = LinearLayout.LayoutParams(
          LinearLayout.LayoutParams.WRAP_CONTENT,
          LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
          topMargin = (4 * resources.displayMetrics.density).toInt()
          bottomMargin = (20 * resources.displayMetrics.density).toInt()
        }
        container.addView(captionView, captionParams)
      } catch (_: IllegalStateException) {
        // Some configurations are not valid in certain locales
      }
    }

    // Helper to add a section divider with label
    fun addSectionLabel(label: String) {
      val labelView = TextView(this).apply {
        text = label
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        setTextColor(labelColor)
        setTypeface(typeface, Typeface.BOLD)
      }
      val labelParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
      ).apply {
        topMargin = (16 * resources.displayMetrics.density).toInt()
        bottomMargin = (12 * resources.displayMetrics.density).toInt()
      }
      container.addView(labelView, labelParams)
    }

    // ===== Logo type variations =====
    addSectionLabel("Logo type variations")

    addPriceBreakdown("logoType: badge") {
      logoType = AfterpayLogoType.BADGE
    }

    addPriceBreakdown("logoType: lockup") {
      logoType = AfterpayLogoType.LOCKUP
    }

    addPriceBreakdown("logoType: compactBadge") {
      logoType = AfterpayLogoType.COMPACT_BADGE
    }

    // ===== Intro text variations =====
    addSectionLabel("Intro text variations")

    addPriceBreakdown("introText: or") {
      introText = AfterpayIntroText.OR
    }

    addPriceBreakdown("introText: payIn") {
      introText = AfterpayIntroText.PAY_IN
    }

    addPriceBreakdown("introText: make") {
      introText = AfterpayIntroText.MAKE
    }

    addPriceBreakdown("introText: pay") {
      introText = AfterpayIntroText.PAY
    }

    addPriceBreakdown("introText: in") {
      introText = AfterpayIntroText.IN
    }

    addPriceBreakdown("introText: empty") {
      introText = AfterpayIntroText.EMPTY
    }

    // ===== Display options =====
    addSectionLabel("Display options")

    addPriceBreakdown("showWithText: false") {
      showWithText = false
    }

    addPriceBreakdown("showInterestFreeText: false") {
      showInterestFreeText = false
    }

    addPriceBreakdown("minimal (no with/interest)") {
      showWithText = false
      showInterestFreeText = false
    }

    // ===== Amount edge case =====
    addSectionLabel("Amount edge cases")

    try {
      val outOfRangeView = AfterpayPriceBreakdown(this).apply {
        totalAmount = BigDecimal("10000000.00") // Out of range amount
      }
      val breakdownParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
      )
      container.addView(outOfRangeView, breakdownParams)

      val captionView = TextView(this).apply {
        text = "amount out of range"
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        setTextColor(captionColor)
      }
      val captionParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
      ).apply {
        topMargin = (4 * resources.displayMetrics.density).toInt()
        bottomMargin = (20 * resources.displayMetrics.density).toInt()
      }
      container.addView(captionView, captionParams)
    } catch (_: IllegalStateException) {}

    // ===== Logo color schemes =====
    addSectionLabel("Logo color schemes")

    addPriceBreakdown("style: default") {
      style = AfterpayWidgetStyle.Default
    }

    addPriceBreakdown("style: alt") {
      style = AfterpayWidgetStyle.Alt
    }

    addPriceBreakdown("style: monochromeDark") {
      style = AfterpayWidgetStyle.MonochromeDark
    }

    addPriceBreakdown("style: monochromeLight") {
      style = AfterpayWidgetStyle.MonochromeLight
    }
  }

  private fun getConfiguration() {
    CoroutineScope(Dispatchers.IO).launch {
      merchantApi().getConfiguration().apply {
        onFailure { _ ->
          val msg = "You must run an AP server to fetch configuration."
          showToastFromBackground(this@AfterpayUiGalleryActivity, msg)
        }

        onSuccess { response: GetConfigurationResponse ->
          withContext(Dispatchers.Main) {
            // Not all logoTypes are valid in each locale (i.e. non-lockup types are not valid in
            // US locale) so we catch exceptions here since an updateText call is triggered when
            // configuration updates
            try {
              Afterpay.setConfiguration(
                minimumAmount = response.minimumAmount?.amount,
                maximumAmount = response.maximumAmount.amount,
                currencyCode = response.maximumAmount.currency,
                locale =
                Locale(response.locale.language, response.locale.country),
                environment = AFTERPAY_ENVIRONMENT,
              )
            } catch (_: IllegalStateException) {}
          }
        }
      }
    }
  }
}

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

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.afterpay.android.Afterpay
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

    val logoContainer = findViewById<LinearLayout>(R.id.logo_container)

    // Instantiate an AfterpayPriceBreakdown and fill it with dummy info.
    AfterpayLogoType.values().forEach { logoType ->

      AfterpayWidgetStyle.values().forEach { style ->
        try {
          // Not all logoTypes are valid in each locale (i.e. non-lockup types are not valid in US locale)
          // so we catch exceptions here
          val breakdownView = AfterpayPriceBreakdown(this).apply {
            totalAmount = BigDecimal("100.00")
            this.logoType = logoType
            this.style = style
          }
          val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
          )
          logoContainer.addView(breakdownView, params)
        } catch (_: IllegalStateException) {}
      }
    }

    // This is needed so that the UI gets inflated.
    // Right now an AP server needs to for the UI to be inflated.
    lifecycleScope.launch {
      getConfiguration()
    }
  }

  private fun getConfiguration() {
    CoroutineScope(Dispatchers.IO).launch {
      merchantApi().getConfiguration().apply {
        onFailure { error ->
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

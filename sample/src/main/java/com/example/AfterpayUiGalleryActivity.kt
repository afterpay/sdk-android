package com.example

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.afterpay.android.Afterpay
import com.afterpay.android.view.AfterpayLogoType
import com.afterpay.android.view.AfterpayPriceBreakdown
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
      val breakdownView = AfterpayPriceBreakdown(this).apply {
        totalAmount = BigDecimal("100.00")
        this.logoType = logoType
      }
      val params = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
      )
      logoContainer.addView(breakdownView, params)
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
            Afterpay.setConfiguration(
              minimumAmount = response.minimumAmount?.amount,
              maximumAmount = response.maximumAmount.amount,
              currencyCode = response.maximumAmount.currency,
              locale =
              Locale(response.locale.language, response.locale.country),
              environment = AFTERPAY_ENVIRONMENT,
            )
          }
        }
      }
    }
  }


}

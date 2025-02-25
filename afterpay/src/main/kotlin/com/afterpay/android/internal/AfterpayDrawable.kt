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

import androidx.annotation.DrawableRes
import com.afterpay.android.Afterpay
import com.afterpay.android.R
import com.afterpay.android.internal.Locales.EN_AU
import com.afterpay.android.internal.Locales.EN_CA
import com.afterpay.android.internal.Locales.EN_GB
import com.afterpay.android.internal.Locales.EN_NZ
import com.afterpay.android.internal.Locales.EN_US
import com.afterpay.android.internal.Locales.FR_CA

private val localeLanguages = mapOf(
  EN_AU to AfterpayDrawable.EN_AFTERPAY,
  EN_GB to AfterpayDrawable.EN_CLEARPAY,
  EN_NZ to AfterpayDrawable.EN_AFTERPAY,
  EN_US to AfterpayDrawable.EN_AFTERPAY_CASH_APP,
  EN_CA to AfterpayDrawable.EN_AFTERPAY,
  FR_CA to AfterpayDrawable.FR_CA,
)

internal enum class AfterpayDrawable(
  @DrawableRes val buttonBuyNowForegroundMonochrome: Int,
  @DrawableRes val buttonBuyNowForegroundPolychrome: Int = buttonBuyNowForegroundMonochrome,
  @DrawableRes val buttonCheckoutForegroundMonochrome: Int,
  @DrawableRes val buttonCheckoutForegroundPolychrome: Int = buttonCheckoutForegroundMonochrome,
  @DrawableRes val buttonPayNowForegroundMonochrome: Int,
  @DrawableRes val buttonPayNowForegroundPolychrome: Int = buttonPayNowForegroundMonochrome,
  @DrawableRes val buttonPlaceOrderForegroundMonochrome: Int,
  @DrawableRes val buttonPlaceOrderForegroundPolychrome: Int = buttonPlaceOrderForegroundMonochrome,
) {
  EN_AFTERPAY(
    buttonBuyNowForegroundMonochrome = R.drawable.afterpay_button_buy_now_fg_en,
    buttonCheckoutForegroundMonochrome = R.drawable.afterpay_button_checkout_fg_en,
    buttonPayNowForegroundMonochrome = R.drawable.afterpay_button_pay_now_fg_en,
    buttonPlaceOrderForegroundMonochrome = R.drawable.afterpay_button_place_order_fg_en,
  ),
  EN_CLEARPAY(
    buttonBuyNowForegroundMonochrome = R.drawable.clearpay_button_buy_now_fg_en,
    buttonCheckoutForegroundMonochrome = R.drawable.clearpay_button_checkout_fg_en,
    buttonPayNowForegroundMonochrome = R.drawable.clearpay_button_pay_now_fg_en,
    buttonPlaceOrderForegroundMonochrome = R.drawable.clearpay_button_place_order_fg_en,
    buttonPlaceOrderForegroundPolychrome = R.drawable.clearpay_button_place_order_fg_en,
  ),
  FR_CA(
    buttonBuyNowForegroundMonochrome = R.drawable.afterpay_button_buy_now_fg_fr_ca,
    buttonCheckoutForegroundMonochrome = R.drawable.afterpay_button_checkout_fg_fr_ca,
    buttonPayNowForegroundMonochrome = R.drawable.afterpay_button_pay_now_fg_fr_ca,
    buttonPlaceOrderForegroundMonochrome = R.drawable.afterpay_button_place_order_fg_fr_ca,
  ),
  EN_AFTERPAY_CASH_APP(
    buttonBuyNowForegroundMonochrome = R.drawable.afterpay_cash_app_button_buy_with_fg_en_monochrome,
    buttonBuyNowForegroundPolychrome = R.drawable.afterpay_cash_app_button_buy_with_fg_en_polychrome,
    buttonCheckoutForegroundMonochrome = R.drawable.afterpay_cash_app_button_checkout_with_fg_en_monochrome,
    buttonCheckoutForegroundPolychrome = R.drawable.afterpay_cash_app_button_checkout_with_fg_en_polychrome,
    buttonPayNowForegroundMonochrome = R.drawable.afterpay_cash_app_button_pay_with_fg_en_monochrome,
    buttonPayNowForegroundPolychrome = R.drawable.afterpay_cash_app_button_pay_with_fg_en_polychrome,
    buttonPlaceOrderForegroundMonochrome = R.drawable.afterpay_cash_app_button_continue_with_fg_en_monochrome,
    buttonPlaceOrderForegroundPolychrome = R.drawable.afterpay_cash_app_button_continue_with_fg_en_polychrome,
  ),
  ;

  companion object {
    fun forLocale(): AfterpayDrawable {
      return localeLanguages[Afterpay.language] ?: EN_AFTERPAY
    }
  }
}

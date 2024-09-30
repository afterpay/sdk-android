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
  EN_US to AfterpayDrawable.EN_AFTERPAY,
  EN_CA to AfterpayDrawable.EN_AFTERPAY,
  FR_CA to AfterpayDrawable.FR_CA,
)

internal enum class AfterpayDrawable(
  @DrawableRes val buttonBuyNowForeground: Int,
  @DrawableRes val buttonCheckoutForeground: Int,
  @DrawableRes val buttonPayNowForeground: Int,
  @DrawableRes val buttonPlaceOrderForeground: Int,
) {
  EN_AFTERPAY(
    buttonBuyNowForeground = R.drawable.afterpay_button_buy_now_fg_en,
    buttonCheckoutForeground = R.drawable.afterpay_button_checkout_fg_en,
    buttonPayNowForeground = R.drawable.afterpay_button_pay_now_fg_en,
    buttonPlaceOrderForeground = R.drawable.afterpay_button_place_order_fg_en,
  ),
  EN_CLEARPAY(
    buttonBuyNowForeground = R.drawable.clearpay_button_buy_now_fg_en,
    buttonCheckoutForeground = R.drawable.clearpay_button_checkout_fg_en,
    buttonPayNowForeground = R.drawable.clearpay_button_pay_now_fg_en,
    buttonPlaceOrderForeground = R.drawable.clearpay_button_place_order_fg_en,
  ),
  FR_CA(
    buttonBuyNowForeground = R.drawable.afterpay_button_buy_now_fg_fr_ca,
    buttonCheckoutForeground = R.drawable.afterpay_button_checkout_fg_fr_ca,
    buttonPayNowForeground = R.drawable.afterpay_button_pay_now_fg_fr_ca,
    buttonPlaceOrderForeground = R.drawable.afterpay_button_place_order_fg_fr_ca,
  ),
  ;

  companion object {
    fun forLocale(): AfterpayDrawable {
      return localeLanguages[Afterpay.language] ?: EN_AFTERPAY
    }
  }
}

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
import androidx.annotation.StringRes
import com.afterpay.android.R
import com.afterpay.android.internal.Locales.EN_AU
import com.afterpay.android.internal.Locales.EN_CA
import com.afterpay.android.internal.Locales.EN_GB
import com.afterpay.android.internal.Locales.EN_NZ
import com.afterpay.android.internal.Locales.EN_US
import com.afterpay.android.internal.Locales.FR_CA
import com.afterpay.android.view.AfterpayColorScheme
import com.afterpay.android.view.AfterpayColorScheme.AfterpayCashAppAlt
import com.afterpay.android.view.AfterpayColorScheme.AfterpayCashAppDefault
import java.util.Locale

private val brandLocales = mapOf(
  setOf(EN_AU, EN_CA, FR_CA, EN_NZ) to Brand.AFTERPAY,
  setOf(EN_GB) to Brand.CLEARPAY,
  setOf(EN_US) to Brand.AFTERPAY_CASH_APP,
)

internal enum class Brand(
  @StringRes val title: Int,
  @StringRes val description: Int,
  @DrawableRes val badgeForeground: Int,
  @DrawableRes val badgeForegroundCropped: Int,
  @DrawableRes val lockupMonochrome: Int,
  @DrawableRes val lockupPolychromeLight: Int = lockupMonochrome,
  @DrawableRes val lockupPolychromeDark: Int = lockupMonochrome,
) {

  AFTERPAY(
    title = R.string.afterpay_service_name,
    description = R.string.afterpay_service_name_description,
    badgeForeground = R.drawable.afterpay_badge_fg,
    badgeForegroundCropped = R.drawable.afterpay_badge_fg_cropped,
    lockupMonochrome = R.drawable.afterpay_lockup,
  ),
  CLEARPAY(
    title = R.string.clearpay_service_name,
    description = R.string.clearpay_service_name_description,
    badgeForeground = R.drawable.clearpay_badge_fg,
    badgeForegroundCropped = R.drawable.clearpay_badge_fg_cropped,
    lockupMonochrome = R.drawable.clearpay_lockup,
  ),

  AFTERPAY_CASH_APP(
    title = R.string.afterpay_service_name,
    description = R.string.afterpay_service_name_description,
    badgeForeground = R.drawable.afterpay_cash_app_lockup_monochrome,
    badgeForegroundCropped = R.drawable.afterpay_cash_app_lockup_monochrome,
    lockupMonochrome = R.drawable.afterpay_cash_app_lockup_monochrome,
    lockupPolychromeLight = R.drawable.afterpay_cash_app_lockup_polychrome_light,
    lockupPolychromeDark = R.drawable.afterpay_cash_app_lockup_polychrome_dark,
  ),
  ;

  fun lockupDrawableResIdForColorScheme(colorScheme: AfterpayColorScheme) = when (colorScheme) {
    AfterpayCashAppDefault -> lockupPolychromeLight
    AfterpayCashAppAlt -> lockupPolychromeDark
    else -> lockupMonochrome
  }

  companion object {
    fun forLocale(locale: Locale): Brand =
      brandLocales.entries.find { locale in it.key }?.value ?: AFTERPAY
  }
}

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
package com.afterpay.android.view

import androidx.annotation.ColorRes
import com.afterpay.android.R
import com.afterpay.android.internal.Locales.EN_US
import com.afterpay.android.view.AfterpayColorScheme.AfterpayBlackOnMint
import com.afterpay.android.view.AfterpayColorScheme.AfterpayBlackOnWhite
import com.afterpay.android.view.AfterpayColorScheme.AfterpayCashAppAlt
import com.afterpay.android.view.AfterpayColorScheme.AfterpayCashAppMonochromeDark
import com.afterpay.android.view.AfterpayColorScheme.AfterpayCashAppMonochromeLight
import com.afterpay.android.view.AfterpayColorScheme.AfterpayCashAppPreferred
import com.afterpay.android.view.AfterpayColorScheme.AfterpayMintOnBlack
import com.afterpay.android.view.AfterpayColorScheme.AfterpayWhiteOnBlack
import java.util.Locale

internal enum class AfterpayColorScheme(
  @ColorRes val foregroundColorResId: Int,
  @ColorRes val backgroundColorResId: Int
) {
  AfterpayBlackOnMint(
    foregroundColorResId = R.color.afterpay_black,
    backgroundColorResId = R.color.afterpay_mint,
  ),
  AfterpayMintOnBlack(
    foregroundColorResId = R.color.afterpay_mint,
    backgroundColorResId = R.color.afterpay_black,
  ),
  AfterpayWhiteOnBlack(
    foregroundColorResId = R.color.afterpay_white,
    backgroundColorResId = R.color.afterpay_black,
  ),
  AfterpayBlackOnWhite(
    foregroundColorResId = R.color.afterpay_black,
    backgroundColorResId = R.color.afterpay_white,
  ),
  AfterpayCashAppPreferred(
    foregroundColorResId = R.color.afterpay_white,
    backgroundColorResId = R.color.afterpay_black,
  ),
  AfterpayCashAppAlt(
    foregroundColorResId = R.color.afterpay_black,
    backgroundColorResId = R.color.cash_app_brand,
  ),
  AfterpayCashAppMonochromeDark(
    foregroundColorResId = R.color.afterpay_white,
    backgroundColorResId = R.color.afterpay_black,
  ),
  AfterpayCashAppMonochromeLight(
    foregroundColorResId = R.color.afterpay_black,
    backgroundColorResId = R.color.afterpay_white
  ),
  ;

  fun isCashAppScheme() = cashAppSchemes.contains(this)

  internal companion object {

    @JvmField
    val DEFAULT = AfterpayCashAppPreferred

    val cashAppSchemes = listOf(AfterpayCashAppPreferred, AfterpayCashAppAlt, AfterpayCashAppMonochromeDark, AfterpayCashAppMonochromeLight)

  }
}

/**
 * Public facing set of styles implementers can choose from.
 * Mapping from these styles to [AfterpayColorScheme] changes depending on Locale
 */
enum class Style {
  Preferred,
  Alt,
  MonochromeDark,
  MonochromeLight;

  internal fun toColorScheme(locale: Locale): AfterpayColorScheme =
    when (locale) {
      EN_US -> when (this) {
        Preferred -> AfterpayCashAppPreferred
        Alt -> AfterpayCashAppAlt
        MonochromeDark -> AfterpayCashAppMonochromeDark
        MonochromeLight -> AfterpayCashAppMonochromeLight
      }
      else -> when (this) {
        Preferred -> AfterpayBlackOnMint
        Alt -> AfterpayMintOnBlack
        MonochromeDark -> AfterpayWhiteOnBlack
        MonochromeLight -> AfterpayBlackOnWhite
      }
    }

  internal companion object {
    @JvmField
    val DEFAULT = Preferred
  }
}

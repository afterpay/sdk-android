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
import com.afterpay.android.view.AfterpayColorScheme.AfterpayCashAppDefault
import com.afterpay.android.view.AfterpayColorScheme.AfterpayCashAppMonochromeDark
import com.afterpay.android.view.AfterpayColorScheme.AfterpayCashAppMonochromeLight
import com.afterpay.android.view.AfterpayColorScheme.AfterpayMintOnBlack
import com.afterpay.android.view.AfterpayColorScheme.AfterpayWhiteOnBlack
import java.util.Locale

/**
 * Set of all possible color schemes, encompassing the various branding that exists across all
 * [Locale]s. Internally, these values are mapped to based on the selected [AfterpayWidgetStyle] and the [Locale],
 * as in [AfterpayWidgetStyle.toColorScheme].
 *
 * Previously, [AfterpayColorScheme] was accessed directly where a different set of options was
 * available. If migrating from direct use, find the equivalent [AfterpayWidgetStyle] value for the deprecated
 * [AfterpayColorScheme] value as follows:
 * - AfterpayColorScheme.mintOnBlack --> [AfterpayWidgetStyle.Default]
 * - AfterpayColorScheme.blackOnMint --> [AfterpayWidgetStyle.Alt]
 * - AfterpayColorScheme.whiteOnBlack --> [AfterpayWidgetStyle.MonochromeDark]
 * - AfterpayColorScheme.blackOnWhite --> [AfterpayWidgetStyle.MonochromeLight]
 */
internal enum class AfterpayColorScheme(
  @ColorRes val foregroundColorResId: Int,
  @ColorRes val backgroundColorResId: Int,
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
  AfterpayCashAppDefault(
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
    backgroundColorResId = R.color.afterpay_white,
  ),
  ;

  fun isCashAppScheme() = cashAppSchemes.contains(this)

  internal companion object {

    @JvmField
    val DEFAULT = AfterpayCashAppDefault

    val cashAppSchemes = listOf(AfterpayCashAppDefault, AfterpayCashAppAlt, AfterpayCashAppMonochromeDark, AfterpayCashAppMonochromeLight)
  }
}

/**
 * Set of semantic styles to apply to the provided widgets.
 *
 * Internally, these values are mapped to [AfterpayColorScheme] values based on the current [Locale]
 */
enum class AfterpayWidgetStyle {
  Default,
  Alt,
  MonochromeDark,
  MonochromeLight,
  ;

  internal fun toColorScheme(locale: Locale): AfterpayColorScheme =
    when (locale) {
      EN_US -> when (this) {
        Default -> AfterpayCashAppDefault
        Alt -> AfterpayCashAppAlt
        MonochromeDark -> AfterpayCashAppMonochromeDark
        MonochromeLight -> AfterpayCashAppMonochromeLight
      }
      else -> when (this) {
        Default -> AfterpayBlackOnMint
        Alt -> AfterpayMintOnBlack
        MonochromeDark -> AfterpayWhiteOnBlack
        MonochromeLight -> AfterpayBlackOnWhite
      }
    }
}

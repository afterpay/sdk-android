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

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView.ScaleType.FIT_CENTER
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.res.use
import androidx.core.view.setPadding
import com.afterpay.android.Afterpay
import com.afterpay.android.R
import com.afterpay.android.internal.ConfigurationObservable
import com.afterpay.android.internal.coloredDrawable
import com.afterpay.android.internal.dp
import com.afterpay.android.internal.rippleDrawable
import com.afterpay.android.view.AfterpayColorScheme.AfterpayBlackOnMint
import com.afterpay.android.view.AfterpayColorScheme.AfterpayBlackOnWhite
import com.afterpay.android.view.AfterpayColorScheme.AfterpayCashAppAlt
import com.afterpay.android.view.AfterpayColorScheme.AfterpayCashAppDefault
import com.afterpay.android.view.AfterpayColorScheme.AfterpayCashAppMonochromeDark
import com.afterpay.android.view.AfterpayColorScheme.AfterpayCashAppMonochromeLight
import com.afterpay.android.view.AfterpayColorScheme.AfterpayMintOnBlack
import com.afterpay.android.view.AfterpayColorScheme.AfterpayWhiteOnBlack
import java.util.Observer

private const val PADDING: Int = 0

class AfterpayPaymentButton @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
) : AppCompatImageButton(context, attrs) {

  var buttonText: ButtonText = ButtonText.DEFAULT
    set(value) {
      field = value
      update()
    }

  var style: AfterpayWidgetStyle = AfterpayWidgetStyle.Default
    set(value) {
      field = value
      colorScheme = value.toColorScheme(Afterpay.locale)
      update()
    }

  private var colorScheme: AfterpayColorScheme = style.toColorScheme(Afterpay.locale)
    set(value) {
      field = value
      update()
    }

  private val configurationObserver = Observer { _, _ ->
    update()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    ConfigurationObservable.addObserver(configurationObserver)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    ConfigurationObservable.deleteObserver(configurationObserver)
  }

  init {
    contentDescription = String.format(
      Afterpay.strings.paymentButtonContentDescription,
      resources.getString(Afterpay.brand.description),
    )
    scaleType = FIT_CENTER
    adjustViewBounds = true
    setPadding(PADDING.dp)

    context.theme.obtainStyledAttributes(attrs, R.styleable.Afterpay, 0, 0).use { attributes ->
      buttonText = ButtonText.values()[
        attributes.getInteger(
          R.styleable.Afterpay_afterpayButtonText,
          ButtonText.DEFAULT.ordinal,
        ),
      ]

      val index = attributes.getInteger(
        R.styleable.Afterpay_afterpayStyle,
        AfterpayWidgetStyle.Default.ordinal,
      )
      val value = AfterpayWidgetStyle.values()[index]
      style = value
    }

    update()
  }

  private fun update() {
    if (!Afterpay.enabled) {
      visibility = View.GONE
    } else {
      visibility = View.VISIBLE
    }

    setImageDrawable(
      context.coloredDrawable(
        drawableResId = buttonText.drawableResIdForColorScheme(colorScheme),
        colorResId = colorScheme.foregroundColorResId
          .takeIf { colorScheme != AfterpayCashAppDefault },
      ),
    )

    val rippleColorResId = when (colorScheme) {
      AfterpayCashAppDefault,
      AfterpayCashAppMonochromeDark,
      -> R.color.afterpay_cash_app_ripple_dark

      AfterpayCashAppAlt -> R.color.afterpay_cash_app_ripple_green

      AfterpayCashAppMonochromeLight,
      AfterpayBlackOnMint,
      AfterpayBlackOnWhite,
      -> R.color.afterpay_ripple_light

      AfterpayMintOnBlack,
      AfterpayWhiteOnBlack,
      -> R.color.afterpay_ripple_dark
    }

    background = context.rippleDrawable(
      rippleColorResId = rippleColorResId,
      drawable = context.coloredDrawable(
        drawableResId = if (colorScheme.isCashAppScheme()) {
          R.drawable.afterpay_cash_app_button_bg_outlined
        } else {
          R.drawable.afterpay_button_bg
        },
        colorResId = colorScheme.backgroundColorResId
          .takeIf { colorScheme != AfterpayCashAppMonochromeLight },
      ),
    )

    invalidate()
    requestLayout()
  }

  enum class ButtonText(
    @DrawableRes val monochromeDrawableResId: Int,
    @DrawableRes val polychromeDrawableResId: Int,
  ) {

    PAY(
      monochromeDrawableResId = Afterpay.drawables.buttonPayNowForegroundMonochrome,
      polychromeDrawableResId = Afterpay.drawables.buttonPayNowForegroundPolychrome,
    ),
    BUY(
      monochromeDrawableResId = Afterpay.drawables.buttonBuyNowForegroundMonochrome,
      polychromeDrawableResId = Afterpay.drawables.buttonBuyNowForegroundPolychrome,
    ),
    CHECKOUT(
      monochromeDrawableResId = Afterpay.drawables.buttonCheckoutForegroundMonochrome,
      polychromeDrawableResId = Afterpay.drawables.buttonCheckoutForegroundPolychrome,
    ),
    CONTINUE(
      monochromeDrawableResId = Afterpay.drawables.buttonPlaceOrderForegroundMonochrome,
      polychromeDrawableResId = Afterpay.drawables.buttonPlaceOrderForegroundPolychrome,
    ),
    ;

    internal fun drawableResIdForColorScheme(colorScheme: AfterpayColorScheme) = when (colorScheme) {
      AfterpayCashAppDefault -> polychromeDrawableResId
      else -> monochromeDrawableResId
    }

    companion object {

      @JvmField
      val DEFAULT = PAY
    }
  }
}

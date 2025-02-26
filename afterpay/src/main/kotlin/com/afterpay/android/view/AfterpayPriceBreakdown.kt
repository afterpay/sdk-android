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
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.use
import com.afterpay.android.Afterpay
import com.afterpay.android.R
import com.afterpay.android.internal.AfterpayInfoSpan
import com.afterpay.android.internal.AfterpayInstalment
import com.afterpay.android.internal.ConfigurationObservable
import com.afterpay.android.internal.Locales
import com.afterpay.android.internal.coloredDrawable
import com.afterpay.android.internal.resolveColorAttr
import com.afterpay.android.view.AfterpayColorScheme.AfterpayCashAppAlt
import com.afterpay.android.view.AfterpayColorScheme.AfterpayCashAppDefault
import com.afterpay.android.view.AfterpayLogoType.LOCKUP
import java.math.BigDecimal
import java.util.Currency
import java.util.Observer

class AfterpayPriceBreakdown @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

  private data class Content(
    val text: String,
    val description: String,
  )

  var totalAmount: BigDecimal = BigDecimal.ZERO
    set(value) {
      field = value
      updateText()
    }

  var style: AfterpayWidgetStyle = AfterpayWidgetStyle.Default
    set(value) {
      field = value
      colorScheme = value.toColorScheme(Afterpay.locale)
      updateText()
    }

  private var colorScheme: AfterpayColorScheme = style.toColorScheme(Afterpay.locale)
    set(value) {
      field = value
      updateText()
    }

  var introText: AfterpayIntroText = AfterpayIntroText.DEFAULT
    set(value) {
      field = value
      updateText()
    }

  var showWithText: Boolean = true
    set(value) {
      field = value
      updateText()
    }

  var showInterestFreeText: Boolean = true
    set(value) {
      field = value
      updateText()
    }

  var logoType: AfterpayLogoType = if (Afterpay.locale == Locales.EN_US) LOCKUP else AfterpayLogoType.DEFAULT
    set(value) {
      field = value
      updateText()
    }

  var moreInfoOptions: AfterpayMoreInfoOptions = AfterpayMoreInfoOptions()
    set(value) {
      field = value
      updateText()
    }

  private val textView: TextView = TextView(context).apply {
    setTextColor(context.resolveColorAttr(android.R.attr.textColorPrimary))
    setLinkTextColor(context.resolveColorAttr(android.R.attr.textColorSecondary))
    setLineSpacing(0f, 1.2f)
    textSize = 14f
    movementMethod = LinkMovementMethod.getInstance()
    importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
    layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
  }

  // The terms and conditions are tied to the configured locale on the configuration
  private val infoUrl: String
    get() {
      return "https://static.afterpay.com/modal/${moreInfoOptions.modalFile()}"
    }

  init {
    layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
    importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
    isFocusable = true

    addView(textView)

    context.theme.obtainStyledAttributes(attrs, R.styleable.Afterpay, 0, 0).use { attributes ->
      style = AfterpayWidgetStyle.values()[
        attributes.getInteger(
          R.styleable.Afterpay_afterpayStyle,
          AfterpayWidgetStyle.Default.ordinal,
        ),
      ]
    }

    updateText()
  }

  private val configurationObserver = Observer { _, _ ->
    updateText()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    ConfigurationObservable.addObserver(configurationObserver)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    ConfigurationObservable.deleteObserver(configurationObserver)
  }

  private fun updateText() {
    visibility = if (!Afterpay.enabled || (colorScheme.isCashAppScheme() && logoType != LOCKUP)) View.GONE else View.VISIBLE

    val drawable: Drawable = generateLogo()
    val instalment = AfterpayInstalment.of(totalAmount, Afterpay.configuration, resources.configuration.locales[0])
    val content = generateContent(instalment)

    textView.apply {
      text = SpannableStringBuilder().apply {
        if (instalment is AfterpayInstalment.NotAvailable) {
          append(
            context.getString(Afterpay.brand.title),
            CenteredImageSpan(drawable),
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE,
          )
          append(" ")
          append(content.text)
        } else {
          append(content.text)
          append(" ")
          append(
            context.getString(Afterpay.brand.title),
            CenteredImageSpan(drawable),
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE,
          )
        }

        val linkStyle = moreInfoOptions.modalLinkStyle.config

        if (linkStyle.customContent != null) {
          append(" ")
          append(
            linkStyle.customContent,
            AfterpayInfoSpan(infoUrl, false),
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE,
          )
        } else if (linkStyle.text != null) {
          append(" ")
          append(
            linkStyle.text,
            AfterpayInfoSpan(infoUrl, linkStyle.underlined),
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE,
          )
        } else if (linkStyle.image != null && linkStyle.imageRenderingMode != null) {
          append(" ")

          val imageDrawable = if (linkStyle.imageRenderingMode == AfterpayImageRenderingMode.TEMPLATE) {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(
              android.R.attr.textColorSecondary,
              typedValue,
              true,
            )

            context.coloredDrawable(
              drawableResId = linkStyle.image,
              colorResId = typedValue.resourceId,
            )
          } else {
            ResourcesCompat.getDrawable(resources, linkStyle.image, null)
          }

          if (imageDrawable != null) {
            imageDrawable.apply {
              val aspectRatio = intrinsicWidth / intrinsicHeight.toFloat()
              val drawableHeight = textView.paint.fontMetrics.run { descent - ascent }
              val drawableWidth = drawableHeight * aspectRatio
              setBounds(0, 0, drawableWidth.toInt(), drawableHeight.toInt())
            }

            val accessibleLinkString = Afterpay.strings.priceBreakdownLinkMoreInfo
            append(
              accessibleLinkString,
              CenteredImageSpan(imageDrawable),
              Spannable.SPAN_INCLUSIVE_EXCLUSIVE,
            )

            setSpan(
              AfterpayInfoSpan(infoUrl),
              this.length - accessibleLinkString.length,
              this.length,
              Spannable.SPAN_INCLUSIVE_EXCLUSIVE,
            )
          }
        }
      }
      contentDescription = content.description
    }
  }

  private fun getWidthToHeightRatioFromDrawableId(id: Int): Double {
    val bmForeground = ResourcesCompat.getDrawable(resources, id, null)!!
    val width = bmForeground.intrinsicWidth
    val height = bmForeground.intrinsicHeight
    return width.toDouble() / height.toDouble()
  }

  private fun generateLogo(): Drawable {
    val drawable = when (logoType) {
      LOCKUP -> context.coloredDrawable(
        drawableResId = Afterpay.brand.lockupDrawableResIdForColorScheme(colorScheme),
        colorResId = colorScheme.foregroundColorResId.takeIf {
          colorScheme != AfterpayCashAppDefault && colorScheme != AfterpayCashAppAlt
        },
      )
      AfterpayLogoType.COMPACT_BADGE -> {
        val foreGround = Afterpay.brand.badgeForegroundCropped
        val ratio = getWidthToHeightRatioFromDrawableId(foreGround)

        val badge = LayerDrawable(
          arrayOf(
            context.coloredDrawable(
              drawableResId = R.drawable.afterpay_badge_narrow_bg,
              colorResId = colorScheme.backgroundColorResId,
            ),

            context.coloredDrawable(
              drawableResId = foreGround,
              colorResId = colorScheme.foregroundColorResId,
            ),
          ),
        )

        badge.setLayerSize(
          1,
          (40 * ratio * logoType.fontHeightMultiplier).toInt(),
          (40 * logoType.fontHeightMultiplier).toInt(),
        )
        badge.setLayerGravity(1, Gravity.CENTER)

        badge
      }
      AfterpayLogoType.BADGE -> LayerDrawable(
        arrayOf(
          context.coloredDrawable(
            drawableResId = R.drawable.afterpay_badge_bg,
            colorResId = colorScheme.backgroundColorResId,
          ),

          context.coloredDrawable(
            drawableResId = Afterpay.brand.badgeForeground,
            colorResId = colorScheme.foregroundColorResId,
          ),
        ),
      )
    }

    drawable.apply {
      val aspectRatio = intrinsicWidth / intrinsicHeight.toFloat()
      val heightFactor = logoType.fontHeightMultiplier
      val drawableHeight = textView.paint.fontMetrics.run { descent - ascent } * heightFactor
      val drawableWidth = drawableHeight * aspectRatio
      setBounds(0, 0, drawableWidth.toInt(), drawableHeight.toInt())
    }

    return drawable
  }

  private fun generateContent(afterpay: AfterpayInstalment): Content = when (afterpay) {
    is AfterpayInstalment.Available -> {
      val isUkLocale = Afterpay.configuration?.locale == Locales.EN_GB
      val isGbpCurrency = Afterpay.configuration?.currency == Currency.getInstance(Locales.EN_GB)

      val withText: String = when {
        showWithText -> Afterpay.strings.priceBreakdownWith
        else -> ""
      }

      val interestFreeText: String = when {
        isUkLocale || isGbpCurrency -> ""
        showInterestFreeText -> Afterpay.strings.priceBreakdownInterestFree
        else -> ""
      }

      val numberOfInstalments: Int = Afterpay.configuration?.let {
        AfterpayInstalment.numberOfInstalments(it.currency)
      } ?: 4

      Content(
        text = String.format(
          Afterpay.strings.priceBreakdownAvailable,
          AfterpayIntroText.fromId(introText.id),
          numberOfInstalments.toString(),
          interestFreeText,
          afterpay.instalmentAmount,
          withText,
        ).trim(),
        description = String.format(
          Afterpay.strings.priceBreakdownAvailableDescription,
          AfterpayIntroText.fromId(introText.id),
          numberOfInstalments.toString(),
          interestFreeText,
          afterpay.instalmentAmount,
          withText,
          resources.getString(Afterpay.brand.description),
        ).trim(),
      )
    }
    is AfterpayInstalment.NotAvailable ->
      if (afterpay.minimumAmount != null) {
        Content(
          text = String.format(
            Afterpay.strings.breakdownLimit,
            afterpay.minimumAmount,
            afterpay.maximumAmount,
          ),
          description = String.format(
            Afterpay.strings.breakdownLimitDescription,
            resources.getString(Afterpay.brand.description),
            afterpay.minimumAmount,
            afterpay.maximumAmount,
          ),
        )
      } else {
        Content(
          text = String.format(
            Afterpay.strings.breakdownLimit,
            "1",
            afterpay.maximumAmount,
          ),
          description = String.format(
            Afterpay.strings.breakdownLimitDescription,
            resources.getString(Afterpay.brand.description),
            "1",
            afterpay.maximumAmount,
          ),
        )
      }
    AfterpayInstalment.NoConfiguration ->
      Content(
        text = Afterpay.strings.noConfiguration,
        description = String.format(
          Afterpay.strings.noConfigurationDescription,
          resources.getString(Afterpay.brand.description),
        ),
      )
  }
}

/**
 * A vertically centered image span.
 */
private class CenteredImageSpan(drawable: Drawable) : ImageSpan(drawable) {
  override fun getSize(
    paint: Paint,
    text: CharSequence,
    start: Int,
    end: Int,
    fontMetricsInt: Paint.FontMetricsInt?,
  ): Int {
    val drawable = drawable
    val bounds = drawable.bounds
    fontMetricsInt?.let {
      val paintFontMetrics = paint.fontMetricsInt
      val fontHeight = paintFontMetrics.descent - paintFontMetrics.ascent
      val drawableHeight = drawable.bounds.height()

      val centerY = paintFontMetrics.ascent + fontHeight / 2
      it.ascent = centerY - drawableHeight / 2
      it.top = it.ascent
      it.bottom = centerY + drawableHeight / 2
      it.descent = it.bottom
    }
    return bounds.right
  }

  override fun draw(
    canvas: Canvas,
    text: CharSequence,
    start: Int,
    end: Int,
    x: Float,
    top: Int,
    y: Int,
    bottom: Int,
    paint: Paint,
  ) {
    val drawable = drawable
    canvas.save()
    val fmPaint = paint.fontMetricsInt
    val fontHeight = fmPaint.descent - fmPaint.ascent
    val centerY = y + fmPaint.descent - fontHeight / 2
    val translationY = centerY - drawable.bounds.height() / 2
    canvas.translate(x, translationY.toFloat())
    drawable.draw(canvas)
    canvas.restore()
  }
}

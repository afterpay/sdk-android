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
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.use
import com.afterpay.android.Afterpay
import com.afterpay.android.R
import com.afterpay.android.internal.Locales
import com.afterpay.android.internal.coloredDrawable
import com.afterpay.android.internal.dp

private const val MIN_WIDTH: Int = 64

class AfterpayBadge @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
) : AppCompatImageView(context, attrs) {

  var style: Style = Style.DEFAULT
    set(value) {
      field = value
      colorScheme = value.toColorScheme(Afterpay.locale)
      update()
    }

  private var colorScheme: AfterpayColorScheme = AfterpayColorScheme.DEFAULT
    set(value) {
      field = value
      update()
    }

  init {
    contentDescription = resources.getString(Afterpay.brand.title)
    importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
    isFocusable = true
    scaleType = FIT_CENTER
    adjustViewBounds = true
    minimumWidth = MIN_WIDTH.dp

    context.theme.obtainStyledAttributes(attrs, R.styleable.Afterpay, 0, 0).use { attributes ->
      style = Style.values()[
        attributes.getInteger(
          R.styleable.Afterpay_afterpayStyle,
          Style.DEFAULT.ordinal,
        ),
      ]
    }
  }

  private fun update() {
    // Badges are not supported in Cash App branding, logo lockup is used instead.
    visibility = if (!Afterpay.enabled || Afterpay.locale == Locales.EN_US) View.GONE else View.VISIBLE

    setImageDrawable(
      context.coloredDrawable(
        drawableResId = Afterpay.brand.badgeForeground,
        colorResId = colorScheme.foregroundColorResId,
      ),
    )

    background = context.coloredDrawable(
      R.drawable.afterpay_badge_bg,
      colorScheme.backgroundColorResId,
    )

    invalidate()
    requestLayout()
  }
}

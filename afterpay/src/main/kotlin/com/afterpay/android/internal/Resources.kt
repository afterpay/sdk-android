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

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import kotlin.math.roundToInt

internal val Float.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics,
    )

internal val Int.dp: Int
    get() = toFloat().dp.roundToInt()

@ColorInt
internal fun Context.resolveColorAttr(@AttrRes colorAttr: Int): Int {
    val attribute = TypedValue().also {
        theme.resolveAttribute(colorAttr, it, true)
    }
    val colorRes = if (attribute.resourceId != 0) attribute.resourceId else attribute.data
    return ContextCompat.getColor(this, colorRes)
}

@ColorInt
internal fun Context.color(@ColorRes colorResId: Int): Int {
    return ContextCompat.getColor(this, colorResId)
}

internal fun Context.coloredDrawable(
    @DrawableRes drawableResId: Int,
    @ColorRes colorResId: Int,
): Drawable = ContextCompat.getDrawable(this, drawableResId).let {
    checkNotNull(it) { "Drawable resource not found" }
    val wrappedDrawable = DrawableCompat.wrap(it)
    DrawableCompat.setTint(wrappedDrawable, color(colorResId))
    return wrappedDrawable
}

internal fun Context.rippleDrawable(
    @ColorRes rippleColorResId: Int,
    drawable: Drawable,
): Drawable = RippleDrawable(
    ColorStateList.valueOf(color(rippleColorResId)),
    drawable,
    null,
)

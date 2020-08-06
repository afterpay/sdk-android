package com.afterpay.android.internal

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat

@ColorInt
internal fun Context.resolveColorAttr(@AttrRes colorAttr: Int): Int {
    val attribute = TypedValue().also {
        theme.resolveAttribute(colorAttr, it, true)
    }
    val colorRes = if (attribute.resourceId != 0) attribute.resourceId else attribute.data
    return ContextCompat.getColor(this, colorRes)
}

package com.afterpay.android.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.afterpay.android.R

class AfterpayButton(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    enum class ColorScheme {
        LIGHT, DARK
    }

    var colorScheme: ColorScheme = ColorScheme.LIGHT
        set(value) {
            field = value
            applyColorScheme(value)
            invalidate()
            requestLayout()
        }

    private val imageView: ImageView = ImageView(context).apply {
        setImageResource(R.drawable.logo_afterpay)
        isClickable = false
        adjustViewBounds = true
        scaleType = ImageView.ScaleType.FIT_CENTER
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, 20.dp).apply {
            gravity = Gravity.CENTER
        }
    }

    init {
        addView(imageView)
        setPadding(16.dp, 10.dp, 16.dp, 10.dp)
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        isClickable = true

        context.theme.obtainStyledAttributes(attrs, R.styleable.AfterpayButton, 0, 0).apply {
            try {
                val value = getInteger(R.styleable.AfterpayButton_colorScheme, 0)
                colorScheme =
                    ColorScheme.values().firstOrNull { it.ordinal == value } ?: ColorScheme.LIGHT
            } finally {
                recycle()
            }
        }
    }

    private fun applyColorScheme(colorScheme: ColorScheme) {
        DrawableCompat.setTint(
            imageView.drawable,
            ContextCompat.getColor(context, colorScheme.logoColor)
        )
        background = resources.getDrawable(colorScheme.backgroundDrawable, null)
    }

    private val ColorScheme.logoColor: Int
        get() = when (this) {
            ColorScheme.LIGHT -> android.R.color.black
            ColorScheme.DARK -> android.R.color.white
        }

    private val ColorScheme.backgroundDrawable: Int
        get() = when (this) {
            ColorScheme.LIGHT -> R.drawable.button_background_light
            ColorScheme.DARK -> R.drawable.button_background_dark
        }

    private val Int.dp: Int
        get() = TypedValue
            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), resources.displayMetrics)
            .toInt()
}

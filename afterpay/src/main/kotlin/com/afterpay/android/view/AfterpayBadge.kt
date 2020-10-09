package com.afterpay.android.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.afterpay.android.R

class AfterpayBadge(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    constructor(context: Context) : this(context, attrs = null)

    var colorScheme: AfterpayColorScheme = AfterpayColorScheme.DEFAULT
        set(value) {
            field = value
            applyColorScheme()
            invalidate()
            requestLayout()
        }

    private val badgeView = ImageView(context).apply {
        setImageResource(colorScheme.badgeDrawable)
        adjustViewBounds = true
        scaleType = ImageView.ScaleType.FIT_CENTER
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
        }
    }

    init {
        contentDescription = resources.getString(R.string.afterpay_badge_content_description)
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        minimumWidth = 64.dp
        isFocusable = true

        addView(badgeView)

        context.theme.obtainStyledAttributes(attrs, R.styleable.Afterpay, 0, 0).apply {
            try {
                val value = getInteger(R.styleable.Afterpay_afterpayColorScheme, 0)
                colorScheme = AfterpayColorScheme.values()[value]
            } finally {
                recycle()
            }
        }

        applyColorScheme()
    }

    private fun applyColorScheme() {
        badgeView.setImageResource(colorScheme.badgeDrawable)
    }

    private val Int.dp: Int
        get() = TypedValue
            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), resources.displayMetrics)
            .toInt()
}

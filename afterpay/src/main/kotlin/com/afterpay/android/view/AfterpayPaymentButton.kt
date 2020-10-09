package com.afterpay.android.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import com.afterpay.android.R

class AfterpayPaymentButton(
    context: Context,
    attrs: AttributeSet?
) : AppCompatImageButton(context, attrs) {
    constructor(context: Context) : this(context, attrs = null)

    var colorScheme: AfterpayColorScheme = AfterpayColorScheme.DEFAULT
        set(value) {
            field = value
            applyButtonImage()
            invalidate()
            requestLayout()
        }

    init {
        contentDescription = resources.getString(R.string.payment_button_content_description)
        adjustViewBounds = true
        background = null

        context.theme.obtainStyledAttributes(attrs, R.styleable.Afterpay, 0, 0).apply {
            try {
                val value = getInteger(R.styleable.Afterpay_afterpayColorScheme, 0)
                colorScheme = AfterpayColorScheme.values()[value]
            } finally {
                recycle()
            }
        }

        applyButtonImage()
    }

    private fun applyButtonImage() {
        setImageResource(colorScheme.payNowButtonDrawable)
    }
}

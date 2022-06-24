package com.afterpay.android.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView.ScaleType.FIT_CENTER
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.use
import com.afterpay.android.Afterpay
import com.afterpay.android.R
import com.afterpay.android.internal.coloredDrawable
import com.afterpay.android.internal.dp

private const val MIN_WIDTH: Int = 64

class AfterpayLockup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    var colorScheme: AfterpayColorScheme = AfterpayColorScheme.DEFAULT
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
            colorScheme = AfterpayColorScheme.values()[
                attributes.getInteger(
                    R.styleable.Afterpay_afterpayColorScheme,
                    AfterpayColorScheme.DEFAULT.ordinal
                )
            ]
        }

        if (!Afterpay.enabled) {
            visibility = View.GONE
        }
    }

    private fun update() {
        setImageDrawable(
            context.coloredDrawable(
                drawableResId = Afterpay.brand.lockup,
                colorResId = colorScheme.foregroundColorResId
            )
        )

        invalidate()
        requestLayout()
    }
}

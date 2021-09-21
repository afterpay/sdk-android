package com.afterpay.android.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView.ScaleType.FIT_CENTER
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.res.use
import androidx.core.view.setPadding
import com.afterpay.android.Afterpay
import com.afterpay.android.R
import com.afterpay.android.internal.coloredDrawable
import com.afterpay.android.internal.dp
import com.afterpay.android.internal.rippleDrawable
import com.afterpay.android.view.AfterpayColorScheme.BLACK_ON_MINT
import com.afterpay.android.view.AfterpayColorScheme.BLACK_ON_WHITE
import com.afterpay.android.view.AfterpayColorScheme.MINT_ON_BLACK
import com.afterpay.android.view.AfterpayColorScheme.WHITE_ON_BLACK
import com.afterpay.android.view.AfterpayColorScheme.values

private const val PADDING: Int = 0

class AfterpayPaymentButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageButton(context, attrs) {

    var buttonText: ButtonText = ButtonText.DEFAULT
        set(value) {
            field = value
            update()
        }

    var colorScheme: AfterpayColorScheme = AfterpayColorScheme.DEFAULT
        set(value) {
            field = value
            update()
        }

    init {
        contentDescription = String.format(
            resources.getString(R.string.afterpay_payment_button_content_description),
            resources.getString(Afterpay.brand.serviceNameDescription)
        )
        scaleType = FIT_CENTER
        adjustViewBounds = true
        setPadding(PADDING.dp)

        context.theme.obtainStyledAttributes(attrs, R.styleable.Afterpay, 0, 0).use { attributes ->
            buttonText = ButtonText.values()[
                attributes.getInteger(
                    R.styleable.Afterpay_afterpayButtonText,
                    ButtonText.DEFAULT.ordinal
                )
            ]

            colorScheme = values()[
                attributes.getInteger(
                    R.styleable.Afterpay_afterpayColorScheme,
                    AfterpayColorScheme.DEFAULT.ordinal
                )
            ]
        }

        update()
    }

    private fun update() {
        setImageDrawable(
            context.coloredDrawable(
                drawableResId = buttonText.drawableResId,
                colorResId = colorScheme.foregroundColorResId
            )
        )

        val rippleColorResId = when (colorScheme) {
            BLACK_ON_MINT, BLACK_ON_WHITE -> R.color.afterpay_ripple_light
            MINT_ON_BLACK, WHITE_ON_BLACK -> R.color.afterpay_ripple_dark
        }

        background = context.rippleDrawable(
            rippleColorResId = rippleColorResId,
            drawable = context.coloredDrawable(
                drawableResId = R.drawable.afterpay_button_bg,
                colorResId = colorScheme.backgroundColorResId
            )
        )

        invalidate()
        requestLayout()
    }

    enum class ButtonText(@DrawableRes val drawableResId: Int) {

        PAY_NOW(drawableResId = Afterpay.brand.buttonPayNowFg),
        BUY_NOW(drawableResId = Afterpay.brand.buttonBuyNowFg),
        CHECKOUT(drawableResId = Afterpay.brand.buttonCheckoutFg),
        PLACE_ORDER(drawableResId = Afterpay.brand.buttonPlaceOrderFg);

        companion object {

            @JvmField
            val DEFAULT = PAY_NOW
        }
    }
}

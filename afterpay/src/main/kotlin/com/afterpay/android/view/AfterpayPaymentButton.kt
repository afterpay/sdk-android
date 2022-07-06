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
import com.afterpay.android.view.AfterpayColorScheme.BLACK_ON_MINT
import com.afterpay.android.view.AfterpayColorScheme.BLACK_ON_WHITE
import com.afterpay.android.view.AfterpayColorScheme.MINT_ON_BLACK
import com.afterpay.android.view.AfterpayColorScheme.WHITE_ON_BLACK
import com.afterpay.android.view.AfterpayColorScheme.values
import java.util.Observer

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
            resources.getString(Afterpay.brand.description)
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
        if (!Afterpay.enabled) {
            visibility = View.GONE
        } else {
            visibility = View.VISIBLE
        }

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

        PAY_NOW(drawableResId = Afterpay.drawables.buttonPayNowForeground),
        BUY_NOW(drawableResId = Afterpay.drawables.buttonBuyNowForeground),
        CHECKOUT(drawableResId = Afterpay.drawables.buttonCheckoutForeground),
        PLACE_ORDER(drawableResId = Afterpay.drawables.buttonPlaceOrderForeground);

        companion object {

            @JvmField
            val DEFAULT = PAY_NOW
        }
    }
}

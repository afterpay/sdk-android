package com.afterpay.android.internal

import androidx.annotation.DrawableRes
import com.afterpay.android.Afterpay
import com.afterpay.android.R
import com.afterpay.android.internal.Locales.EN_AU
import com.afterpay.android.internal.Locales.EN_CA
import com.afterpay.android.internal.Locales.EN_GB
import com.afterpay.android.internal.Locales.EN_NZ
import com.afterpay.android.internal.Locales.EN_US
import com.afterpay.android.internal.Locales.ES_ES
import com.afterpay.android.internal.Locales.FR_CA
import com.afterpay.android.internal.Locales.FR_FR
import com.afterpay.android.internal.Locales.IT_IT

private val localeLanguages = mapOf(
    EN_AU to AfterpayDrawable.EN_AFTERPAY,
    EN_GB to AfterpayDrawable.EN_CLEARPAY,
    EN_NZ to AfterpayDrawable.EN_AFTERPAY,
    EN_US to AfterpayDrawable.EN_AFTERPAY,
    EN_CA to AfterpayDrawable.EN_AFTERPAY,
    FR_CA to AfterpayDrawable.FR_CA,
    FR_FR to AfterpayDrawable.FR,
    IT_IT to AfterpayDrawable.IT,
    ES_ES to AfterpayDrawable.ES,
)

internal enum class AfterpayDrawable(
    @DrawableRes val buttonBuyNowForeground: Int,
    @DrawableRes val buttonCheckoutForeground: Int,
    @DrawableRes val buttonPayNowForeground: Int,
    @DrawableRes val buttonPlaceOrderForeground: Int,
) {
    EN_AFTERPAY(
        buttonBuyNowForeground = R.drawable.afterpay_button_buy_now_fg_en,
        buttonCheckoutForeground = R.drawable.afterpay_button_checkout_fg_en,
        buttonPayNowForeground = R.drawable.afterpay_button_pay_now_fg_en,
        buttonPlaceOrderForeground = R.drawable.afterpay_button_place_order_fg_en,
    ),
    EN_CLEARPAY(
        buttonBuyNowForeground = R.drawable.clearpay_button_buy_now_fg_en,
        buttonCheckoutForeground = R.drawable.clearpay_button_checkout_fg_en,
        buttonPayNowForeground = R.drawable.clearpay_button_pay_now_fg_en,
        buttonPlaceOrderForeground = R.drawable.clearpay_button_place_order_fg_en,
    ),
    FR_CA(
        buttonBuyNowForeground = R.drawable.afterpay_button_buy_now_fg_fr_ca,
        buttonCheckoutForeground = R.drawable.afterpay_button_checkout_fg_fr_ca,
        buttonPayNowForeground = R.drawable.afterpay_button_pay_now_fg_fr_ca,
        buttonPlaceOrderForeground = R.drawable.afterpay_button_place_order_fg_fr_ca,
    ),
    FR(
        buttonBuyNowForeground = R.drawable.clearpay_button_buy_now_fg_fr,
        buttonCheckoutForeground = R.drawable.clearpay_button_checkout_fg_fr,
        buttonPayNowForeground = R.drawable.clearpay_button_pay_now_fg_fr,
        buttonPlaceOrderForeground = R.drawable.clearpay_button_place_order_fg_fr,
    ),
    IT(
        buttonBuyNowForeground = R.drawable.clearpay_button_buy_now_fg_it,
        buttonCheckoutForeground = R.drawable.clearpay_button_checkout_fg_it,
        buttonPayNowForeground = R.drawable.clearpay_button_pay_now_fg_it,
        buttonPlaceOrderForeground = R.drawable.clearpay_button_place_order_fg_it,
    ),
    ES(
        buttonBuyNowForeground = R.drawable.clearpay_button_buy_now_fg_es,
        buttonCheckoutForeground = R.drawable.clearpay_button_checkout_fg_es,
        buttonPayNowForeground = R.drawable.clearpay_button_pay_now_fg_es,
        buttonPlaceOrderForeground = R.drawable.clearpay_button_place_order_fg_es,
    ),
    ;

    companion object {
        fun forLocale(): AfterpayDrawable {
            return localeLanguages[Afterpay.language] ?: EN_AFTERPAY
        }
    }
}

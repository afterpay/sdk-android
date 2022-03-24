package com.afterpay.android.internal

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import java.util.Locale

private val brandLocales = mapOf(
    setOf(EN_AU, EN_CA, FR_CA, EN_NZ, EN_US) to Brand.AFTERPAY,
    setOf(EN_GB, IT_IT, FR_FR, ES_ES) to Brand.CLEARPAY,
)

internal enum class Brand(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @DrawableRes val badgeForeground: Int,
    @DrawableRes val payNowButtonForeground: Int,
    @DrawableRes val buyNowButtonForeground: Int,
    @DrawableRes val checkoutButtonForeground: Int,
    @DrawableRes val placeOrderButtonForeground: Int
) {

    AFTERPAY(
        title = R.string.afterpay_service_name,
        description = R.string.afterpay_service_name_description,
        badgeForeground = R.drawable.afterpay_badge_fg,
        payNowButtonForeground = R.drawable.afterpay_button_pay_now_fg,
        buyNowButtonForeground = R.drawable.afterpay_button_buy_now_fg,
        checkoutButtonForeground = R.drawable.afterpay_button_checkout_fg,
        placeOrderButtonForeground = R.drawable.afterpay_button_place_order_fg
    ),

    CLEARPAY(
        title = R.string.clearpay_service_name,
        description = R.string.clearpay_service_name_description,
        badgeForeground = R.drawable.clearpay_badge_fg,
        payNowButtonForeground = R.drawable.clearpay_button_pay_now_fg,
        buyNowButtonForeground = R.drawable.clearpay_button_buy_now_fg,
        checkoutButtonForeground = R.drawable.clearpay_button_checkout_fg,
        placeOrderButtonForeground = R.drawable.clearpay_button_place_order_fg
    );

    companion object {

        fun forLocale(locale: Locale): Brand =
            brandLocales.entries.find { locale in it.key }?.value ?: AFTERPAY
    }
}

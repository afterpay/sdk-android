package com.afterpay.android.internal

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.afterpay.android.R
import java.util.Locale

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
            Locales.brandLocales.entries.find { locale in it.key }?.value ?: AFTERPAY
    }
}

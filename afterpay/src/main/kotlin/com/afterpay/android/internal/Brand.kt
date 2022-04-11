package com.afterpay.android.internal

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.afterpay.android.R
import com.afterpay.android.internal.Locales.AUSTRALIA
import com.afterpay.android.internal.Locales.CANADA
import com.afterpay.android.internal.Locales.NEW_ZEALAND
import com.afterpay.android.internal.Locales.UK
import com.afterpay.android.internal.Locales.US
import java.util.Locale

private val brandLocales = mapOf(
    setOf(AUSTRALIA, CANADA, NEW_ZEALAND, US) to Brand.AFTERPAY,
    setOf(UK) to Brand.CLEARPAY,
)

internal enum class Brand(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @DrawableRes val badgeForeground: Int,
    @DrawableRes val lockup: Int,
    @DrawableRes val payNowButtonForeground: Int,
    @DrawableRes val buyNowButtonForeground: Int,
    @DrawableRes val checkoutButtonForeground: Int,
    @DrawableRes val placeOrderButtonForeground: Int
) {

    AFTERPAY(
        title = R.string.afterpay_service_name,
        description = R.string.afterpay_service_name_description,
        badgeForeground = R.drawable.afterpay_badge_fg,
        lockup = R.drawable.afterpay_lockup,
        payNowButtonForeground = R.drawable.afterpay_button_pay_now_fg,
        buyNowButtonForeground = R.drawable.afterpay_button_buy_now_fg,
        checkoutButtonForeground = R.drawable.afterpay_button_checkout_fg,
        placeOrderButtonForeground = R.drawable.afterpay_button_place_order_fg
    ),

    CLEARPAY(
        title = R.string.clearpay_service_name,
        description = R.string.clearpay_service_name_description,
        badgeForeground = R.drawable.clearpay_badge_fg,
        lockup = R.drawable.clearpay_lockup,
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

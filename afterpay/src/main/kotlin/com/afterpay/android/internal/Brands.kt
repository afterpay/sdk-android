package com.afterpay.android.internal

import com.afterpay.android.R
import java.util.Locale

class Brands(var locale: Locale) {
    private fun getAsset(afterpay: Int, clearpay: Int): Int {
        if (locale in Locales.clearpayBrands) {
            return clearpay
        }

        return afterpay
    }

    internal val serviceName =
        getAsset(R.string.afterpay_service_name, R.string.clearpay_service_name)

    internal val serviceNameDescription =
        getAsset(R.string.afterpay_service_name_description, R.string.clearpay_service_name_description)

    internal val badgeFg =
        getAsset(R.drawable.afterpay_badge_fg, R.drawable.clearpay_badge_fg)

    internal val buttonPayNowFg =
        getAsset(R.drawable.afterpay_button_pay_now_fg, R.drawable.clearpay_button_pay_now_fg)

    internal val buttonBuyNowFg =
        getAsset(R.drawable.afterpay_button_buy_now_fg, R.drawable.clearpay_button_buy_now_fg)

    internal val buttonCheckoutFg =
        getAsset(R.drawable.afterpay_button_checkout_fg, R.drawable.clearpay_button_checkout_fg)

    internal val buttonPlaceOrderFg =
        getAsset(R.drawable.afterpay_button_place_order_fg, R.drawable.clearpay_button_place_order_fg)
}

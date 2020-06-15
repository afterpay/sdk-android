package com.afterpay.android

import android.content.Intent

private object AfterpayIntent {
    const val CHECKOUT_URL = "AFTERPAY_CHECKOUT_URL"
    const val STATUS = "AFTERPAY_CHECKOUT_SUCCESS"
}

internal fun Intent.putCheckoutUrlExtra(url: String): Intent =
    putExtra(AfterpayIntent.CHECKOUT_URL, url)

internal fun Intent.getCheckoutUrlExtra(): String? =
    getStringExtra(AfterpayIntent.CHECKOUT_URL)

internal fun Intent.putCheckoutStatusExtra(status: CheckoutStatus): Intent =
    putExtra(AfterpayIntent.STATUS, status.name)

internal fun Intent.getCheckoutStatusExtra(): CheckoutStatus? {
    val status = getStringExtra(AfterpayIntent.STATUS) ?: return null
    return try {
        enumValueOf<CheckoutStatus>(status)
    } catch (error: IllegalAccessException) {
        null
    }
}

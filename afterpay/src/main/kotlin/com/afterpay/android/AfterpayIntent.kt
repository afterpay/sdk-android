package com.afterpay.android

import android.content.Intent

private object AfterpayIntent {
    const val CHECKOUT_URL = "AFTERPAY_CHECKOUT_URL"
    const val STATUS = "AFTERPAY_CHECKOUT_SUCCESS"
}

internal fun Intent.putExtra(request: CheckoutRequest): Intent =
    putExtra(AfterpayIntent.CHECKOUT_URL, request.checkoutUrl)

internal fun Intent.getCheckoutRequestExtra(): CheckoutRequest? {
    val url = getStringExtra(AfterpayIntent.CHECKOUT_URL) ?: return null
    return CheckoutRequest(checkoutUrl = url)
}

internal fun Intent.putExtra(status: CheckoutStatus): Intent =
    putExtra(AfterpayIntent.STATUS, status.name)

internal fun Intent.getCheckoutStatusExtra(): CheckoutStatus? {
    val status = getStringExtra(AfterpayIntent.STATUS) ?: return null
    return try {
        enumValueOf<CheckoutStatus>(status)
    } catch (error: IllegalAccessException) {
        null
    }
}

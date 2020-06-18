package com.afterpay.android.util

import android.content.Intent

private object AfterpayIntent {
    const val CHECKOUT_URL = "AFTERPAY_CHECKOUT_URL"
    const val ORDER_TOKEN = "AFTERPAY_ORDER_TOKEN"
}

internal fun Intent.putCheckoutUrlExtra(url: String): Intent =
    putExtra(AfterpayIntent.CHECKOUT_URL, url)

internal fun Intent.getCheckoutUrlExtra(): String? =
    getStringExtra(AfterpayIntent.CHECKOUT_URL)

internal fun Intent.putOrderTokenExtra(token: String): Intent =
    putExtra(AfterpayIntent.ORDER_TOKEN, token)

internal fun Intent.getOrderTokenExtra(): String? =
    getStringExtra(AfterpayIntent.ORDER_TOKEN)

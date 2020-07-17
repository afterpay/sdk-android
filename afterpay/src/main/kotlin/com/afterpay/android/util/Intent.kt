package com.afterpay.android.util

import android.content.Intent
import com.afterpay.android.CancellationStatus
import java.lang.Exception

private object AfterpayIntent {
    const val CHECKOUT_URL = "AFTERPAY_CHECKOUT_URL"
    const val ORDER_TOKEN = "AFTERPAY_ORDER_TOKEN"
    const val CANCELLATION_STATUS = "AFTERPAY_CANCELLATION_STATUS"
}

internal fun Intent.putCheckoutUrlExtra(url: String): Intent =
    putExtra(AfterpayIntent.CHECKOUT_URL, url)

internal fun Intent.getCheckoutUrlExtra(): String? =
    getStringExtra(AfterpayIntent.CHECKOUT_URL)

internal fun Intent.putOrderTokenExtra(token: String): Intent =
    putExtra(AfterpayIntent.ORDER_TOKEN, token)

internal fun Intent.getOrderTokenExtra(): String? =
    getStringExtra(AfterpayIntent.ORDER_TOKEN)

internal fun Intent.putCancellationStatusExtra(status: CancellationStatus): Intent =
    putExtra(AfterpayIntent.CANCELLATION_STATUS, status.name)

internal fun Intent.getCancellationStatusExtra(): CancellationStatus? = try {
    getStringExtra(AfterpayIntent.CANCELLATION_STATUS)?.let { enumValueOf<CancellationStatus>(it) }
} catch (_: Exception) {
    null
}

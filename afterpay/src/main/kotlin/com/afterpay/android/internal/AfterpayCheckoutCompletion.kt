package com.afterpay.android.internal

internal data class AfterpayCheckoutCompletion(
    val status: Status,
    val orderToken: String
) {
    @Suppress("UNUSED_PARAMETER")
    internal enum class Status(statusString: String) {
        SUCCESS("SUCCESS"),
        CANCELLED("CANCELLED")
    }
}

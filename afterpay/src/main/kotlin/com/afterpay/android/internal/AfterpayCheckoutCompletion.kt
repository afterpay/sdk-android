package com.afterpay.android.internal

import kotlinx.serialization.Serializable

@Serializable
internal data class AfterpayCheckoutCompletion(
    val status: Status,
    val orderToken: String
) {

    @Suppress("UNUSED_PARAMETER")
    @Serializable
    internal enum class Status(statusString: String) {
        SUCCESS("SUCCESS"),
        CANCELLED("CANCELLED")
    }
}

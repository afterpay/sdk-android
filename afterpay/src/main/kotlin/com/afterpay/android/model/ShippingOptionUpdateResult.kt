package com.afterpay.android.model

sealed class ShippingOptionUpdateResult

data class ShippingOptionUpdateSuccessResult(
    val shippingOptionUpdate: ShippingOptionUpdate,
) : ShippingOptionUpdateResult()

data class ShippingOptionUpdateErrorResult(
    val error: ShippingOptionUpdateError,
) : ShippingOptionUpdateResult()

enum class ShippingOptionUpdateError {
    SERVICE_UNAVAILABLE,
    BAD_RESPONSE,
}

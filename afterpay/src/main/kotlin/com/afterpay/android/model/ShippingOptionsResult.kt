package com.afterpay.android.model

sealed class ShippingOptionsResult

data class ShippingOptionsSuccessResult(
    val shippingOptions: List<ShippingOption>,
) : ShippingOptionsResult()

data class ShippingOptionsErrorResult(
    val error: ShippingOptionsError,
) : ShippingOptionsResult()

enum class ShippingOptionsError {
    SHIPPING_ADDRESS_UNRECOGNIZED,
    SHIPPING_ADDRESS_UNSUPPORTED,
    SERVICE_UNAVAILABLE,
    BAD_RESPONSE,
}

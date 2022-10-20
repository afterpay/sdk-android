package com.afterpay.android

enum class CancellationStatus {
    USER_INITIATED,
    NO_CHECKOUT_URL,
    INVALID_CHECKOUT_URL,
    NO_CHECKOUT_HANDLER,
    NO_CONFIGURATION,
    LANGUAGE_NOT_SUPPORTED
}

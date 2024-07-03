// Copyright Block, Inc.
package com.example.api

/**
 * Required format to work with https://github.com/afterpay/sdk-example-server
 */
data class GetTokenRequest(
    val email: String,
    val amount: String,
    val mode: CheckoutMode,
    val isCashAppPay: Boolean = false,
)

enum class CheckoutMode(val string: String) {
    STANDARD("standard"),
    EXPRESS("express"),
}

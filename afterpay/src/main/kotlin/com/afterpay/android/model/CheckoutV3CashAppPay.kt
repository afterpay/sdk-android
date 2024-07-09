// Copyright Block, Inc.
package com.afterpay.android.model

data class CheckoutV3CashAppPay(
    val token: String,
    val singleUseCardToken: String,
    val amount: Double,
    val redirectUri: String,
    val merchantId: String,
    val brandId: String,
    val jwt: String,
)

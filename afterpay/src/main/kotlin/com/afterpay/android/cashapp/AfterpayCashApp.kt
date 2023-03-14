package com.afterpay.android.cashapp

data class AfterpayCashApp(
    val amount: Double,
    val redirectUri: String,
    val merchantId: String,
    val brandId: String,
    val jwt: String,
)

package com.example.afterpay.data

data class ConfigurationResponse(
    val minimumAmount: Money?,
    val maximumAmount: Money
) {
    data class Money(
        val amount: String,
        val currency: String
    )
}

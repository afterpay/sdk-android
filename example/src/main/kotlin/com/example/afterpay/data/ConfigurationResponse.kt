package com.example.afterpay.data

data class ConfigurationResponse(
    val minimumAmount: Money?,
    val maximumAmount: Money,
    val locale: Locale
) {
    data class Money(
        val amount: String,
        val currency: String
    )

    data class Locale(
        val identifier: String,
        val language: String,
        val country: String
    )
}

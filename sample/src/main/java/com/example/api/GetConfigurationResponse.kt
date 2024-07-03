// Copyright Block, Inc.
package com.example.api

/**
 * Required format to work with https://github.com/afterpay/sdk-example-server
 */
data class GetConfigurationResponse(
    val minimumAmount: Money?,
    val maximumAmount: Money,
    val locale: Locale,
) {
    data class Money(
        val amount: String,
        val currency: String,
    )

    data class Locale(
        val identifier: String,
        val language: String,
        val country: String,
    )
}

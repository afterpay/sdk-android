package com.afterpay.android

data class Money(
    /**
     * The amount is a string representation of a decimal number, rounded to 2 decimal places.
     */
    val amount: String,
    /**
     * The currency in ISO 4217 format. Supported values include "AUD", "NZD", "USD", and "CAD".
     */
    val currency: String
)

/**
 * Payment limits for a merchant account.
 */
data class Configuration(
    /**
     * Minimum order amount.
     */
    val minimumAmount: Money?,
    /**
     * Maximum order amount.
     */
    val maximumAmount: Money
)

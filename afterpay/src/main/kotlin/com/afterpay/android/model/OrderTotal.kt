package com.afterpay.android.model

import java.math.BigDecimal

/** The order total. Each property will be transformed to a `Money` object by
 * conforming the amount to ISO-4217 by:
 * - Rounding to 2 decimals using banker's rounding.
 * - Including the currency code as provided by [AfterpayRegion].
 */
data class OrderTotal(
    /** Amount to be charged to consumer, inclusive of [shipping] and [tax]. */
    val total: BigDecimal,
    /** The shipping amount, included for fraud detection purposes. */
    val shipping: BigDecimal,
    /** The tax amount, included for fraud detection purposes. */
    val tax: BigDecimal
)

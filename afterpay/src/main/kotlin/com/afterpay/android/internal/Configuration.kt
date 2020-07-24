package com.afterpay.android.internal

import java.math.BigDecimal
import java.util.Currency

internal data class Configuration(
    val minimumAmount: BigDecimal?,
    val maximumAmount: BigDecimal,
    val currency: Currency
)

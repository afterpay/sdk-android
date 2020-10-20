package com.afterpay.android.internal

import java.math.BigDecimal
import java.util.Currency
import java.util.Locale

internal data class Configuration(
    val minimumAmount: BigDecimal?,
    val maximumAmount: BigDecimal,
    val currency: Currency,
    val locale: Locale
)

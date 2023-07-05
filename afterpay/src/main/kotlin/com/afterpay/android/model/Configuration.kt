package com.afterpay.android.model

import com.afterpay.android.AfterpayEnvironment
import java.math.BigDecimal
import java.util.Currency
import java.util.Locale

data class Configuration(
    val minimumAmount: BigDecimal?,
    val maximumAmount: BigDecimal,
    val currency: Currency,
    val locale: Locale,
    val environment: AfterpayEnvironment,
    val consumerLocale: Locale? = null,
)

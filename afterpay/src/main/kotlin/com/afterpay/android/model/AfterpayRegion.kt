package com.afterpay.android.model

import com.afterpay.android.internal.Locales
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

enum class AfterpayRegion(val locale: Locale, val currencyCode: String) {
    US(Locales.US, "USD")
}

fun AfterpayRegion.formatted(currency: BigDecimal): String {
    // Round to two decimals, as per ISO-4217, using banker's rounding
    return currency.setScale(2, RoundingMode.HALF_EVEN).toString()
}

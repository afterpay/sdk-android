package com.afterpay.android.model

import com.afterpay.android.internal.BigDecimalSerializer
import com.afterpay.android.internal.CurrencySerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.Currency

@Serializable
data class Money(
    @Serializable(with = BigDecimalSerializer::class) val amount: BigDecimal,
    @Serializable(with = CurrencySerializer::class) val currency: Currency
)

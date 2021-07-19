package com.afterpay.android.model

import java.math.BigDecimal

data class OrderTotal(
    val total: BigDecimal,
    val shipping: BigDecimal,
    val tax: BigDecimal
)

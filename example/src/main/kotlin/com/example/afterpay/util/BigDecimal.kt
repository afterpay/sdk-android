package com.example.afterpay.util

import java.math.BigDecimal
import java.text.NumberFormat

fun BigDecimal.asCurrency(): String =
    NumberFormat.getCurrencyInstance().format(this)

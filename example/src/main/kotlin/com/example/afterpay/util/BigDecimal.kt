package com.example.afterpay.util

import java.math.BigDecimal
import java.text.NumberFormat

fun BigDecimal.asCurrency(): String =
    NumberFormat.getCurrencyInstance().format(this)

fun <T> Iterable<T>.sumByBigDecimal(selector: (T) -> BigDecimal): BigDecimal =
    fold(0.toBigDecimal()) { acc, t -> acc + selector(t) }

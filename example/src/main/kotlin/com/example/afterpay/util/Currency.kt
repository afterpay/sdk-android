package com.example.afterpay.util

import java.text.NumberFormat

fun Double.asCurrency(): String {
    val format = NumberFormat.getCurrencyInstance().apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return format.format(this)
}

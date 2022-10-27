package com.example.afterpay.data

enum class CheckoutMode(val string: String) {
    STANDARD("standard"),
    EXPRESS("express"),
}

data class CheckoutRequest(
    val email: String,
    val amount: String,
    val mode: CheckoutMode,
)

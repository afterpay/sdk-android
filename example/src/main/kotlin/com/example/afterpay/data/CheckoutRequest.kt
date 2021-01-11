package com.example.afterpay.data

data class CheckoutRequest(
    val email: String,
    val amount: String,
    val mode: String = "express"
)

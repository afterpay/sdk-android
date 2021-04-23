package com.afterpay.android.model

import kotlinx.serialization.Serializable

@Serializable
data class Money(
    val amount: String,
    val currency: String
)

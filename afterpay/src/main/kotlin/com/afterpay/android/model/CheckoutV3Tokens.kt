package com.afterpay.android.model

import kotlinx.serialization.Serializable

@Serializable
data class CheckoutV3Tokens(
    val token: String,
    val singleUseCardToken: String,
    val ppaConfirmToken: String,
)

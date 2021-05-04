package com.afterpay.android.model

import kotlinx.serialization.Serializable

@Serializable
data class ShippingOption(
    val id: String,
    val name: String,
    val description: String,
    var shippingAmount: Money,
    var orderAmount: Money,
    var taxAmount: Money?
)

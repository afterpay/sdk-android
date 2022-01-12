package com.afterpay.android.model

import kotlinx.serialization.Serializable

@Serializable
data class ShippingOptionUpdate(
    val id: String,
    var shippingAmount: Money,
    var orderAmount: Money,
    var taxAmount: Money?
)

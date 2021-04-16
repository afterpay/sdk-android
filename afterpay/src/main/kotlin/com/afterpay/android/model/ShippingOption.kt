package com.afterpay.android.model

data class ShippingOption(
    val id: String,
    val name: String,
    val description: String,
    var shippingAmount: Money,
    var orderAmount: Money,
    var taxAmount: Money?
) {
    data class Money(
        val amount: String,
        val currency: String
    )
}

package com.afterpay.android.model

import kotlinx.serialization.Serializable

@Serializable
data class ShippingAddress(
    val countryCode: String?,
    val postcode: String?,
    val phoneNumber: String?,
    val state: String?,
    val suburb: String?
)

package com.afterpay.android.model

import kotlinx.serialization.Serializable

@Serializable
data class ShippingAddress(
    val name: String?,
    val address1: String?,
    val address2: String?,
    val countryCode: String?,
    val postcode: String?,
    val phoneNumber: String?,
    val state: String?,
    val suburb: String?
)

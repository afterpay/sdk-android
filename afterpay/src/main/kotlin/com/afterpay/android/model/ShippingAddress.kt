package com.afterpay.android.model

import kotlinx.serialization.Serializable

@Serializable
data class ShippingAddress(
    val name: String?,
    val address1: String?,
    val address2: String? = null,
    val countryCode: String? = null,
    val postcode: String?,
    val phoneNumber: String? = null,
    val state: String? = null,
    val suburb: String? = null,
)

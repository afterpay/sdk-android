package com.afterpay.android.internal

import com.afterpay.android.BuildConfig

internal data class AfterpayCheckoutV2(
    val token: String,
    val locale: String,
    val environment: String,
    val version: String,
    val pickup: Boolean?,
    val buyNow: Boolean?,
    val shippingOptionRequired: Boolean?
) {
    constructor(
        token: String,
        configuration: Configuration
    ) : this(
        token = token,
        locale = configuration.locale.toString(),
        environment = "sandbox",
        version = "${BuildConfig.AfterpayLibraryVersion}-android",
        pickup = null,
        buyNow = null,
        shippingOptionRequired = null
    )
}

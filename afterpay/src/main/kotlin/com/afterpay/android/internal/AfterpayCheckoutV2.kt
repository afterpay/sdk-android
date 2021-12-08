package com.afterpay.android.internal

import com.afterpay.android.AfterpayCheckoutV2Options
import com.afterpay.android.BuildConfig
import kotlinx.serialization.Serializable

@Serializable
internal data class AfterpayCheckoutV2(
    val token: String,
    val locale: String,
    val environment: String,
    val version: String,
    val pickup: Boolean?,
    val buyNow: Boolean?,
    val shippingOptionRequired: Boolean?,
    val checkoutRedesignForced: Boolean?
) {
    constructor(
        token: String,
        configuration: Configuration,
        options: AfterpayCheckoutV2Options
    ) : this(
        token = token,
        locale = configuration.locale.toString(),
        environment = configuration.environment.toString(),
        version = "${BuildConfig.AfterpayLibraryVersion}-android",
        pickup = options.pickup,
        buyNow = options.buyNow,
        shippingOptionRequired = options.shippingOptionRequired,
        checkoutRedesignForced = options.enableSingleShippingOptionUpdate
    )
}

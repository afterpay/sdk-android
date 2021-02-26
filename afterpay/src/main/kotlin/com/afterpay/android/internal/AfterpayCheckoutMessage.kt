package com.afterpay.android.internal

import com.afterpay.android.model.ShippingAddress
import com.afterpay.android.model.ShippingOption

internal sealed class AfterpayCheckoutMessage(
    open val meta: Meta,
    open val payload: Any
) {
    internal data class Meta(val requestId: String)
}

internal data class CheckoutLog(
    val severity: String,
    val message: String
)

internal data class CheckoutLogMessage(
    override val meta: Meta,
    override val payload: CheckoutLog
) : AfterpayCheckoutMessage(meta, payload)

internal data class ShippingAddressMessage(
    override val meta: Meta,
    override val payload: ShippingAddress
) : AfterpayCheckoutMessage(meta, payload)

internal data class ShippingOptionMessage(
    override val meta: Meta,
    override val payload: ShippingOption
) : AfterpayCheckoutMessage(meta, payload)

internal data class ShippingOptionsMessage(
    override val meta: Meta,
    override val payload: List<ShippingOption>
) : AfterpayCheckoutMessage(meta, payload)

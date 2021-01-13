package com.afterpay.android.internal

import com.afterpay.android.model.ShippingAddress
import com.afterpay.android.model.ShippingOption

internal data class MessageMeta(val requestId: String)

internal sealed class AfterpayCheckoutMessage(
    open val meta: MessageMeta,
    open val payload: Any
)

internal data class ShippingAddressMessage(
    override val meta: MessageMeta,
    override val payload: ShippingAddress
): AfterpayCheckoutMessage(meta, payload)

internal data class ShippingOptionMessage(
    override val meta: MessageMeta,
    override val payload: ShippingOption
): AfterpayCheckoutMessage(meta, payload)

internal data class ShippingOptionsMessage(
    override val meta: MessageMeta,
    override val payload: List<ShippingOption>
): AfterpayCheckoutMessage(meta, payload)

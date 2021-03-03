package com.afterpay.android.internal

import com.afterpay.android.model.ShippingAddress
import com.afterpay.android.model.ShippingOption
import com.afterpay.android.model.ShippingOptionsError

internal data class AfterpayCheckoutMessageMeta(val requestId: String)

// Received Messages

internal sealed class AfterpayCheckoutReceivedMessage(
    open val meta: AfterpayCheckoutMessageMeta,
    open val payload: Any
)

internal data class CheckoutLog(
    val severity: String,
    val message: String
)

internal data class CheckoutLogMessage(
    override val meta: AfterpayCheckoutMessageMeta,
    override val payload: CheckoutLog
) : AfterpayCheckoutReceivedMessage(meta, payload)

internal data class ShippingAddressMessage(
    override val meta: AfterpayCheckoutMessageMeta,
    override val payload: ShippingAddress
) : AfterpayCheckoutReceivedMessage(meta, payload)

internal data class ShippingOptionMessage(
    override val meta: AfterpayCheckoutMessageMeta,
    override val payload: ShippingOption
) : AfterpayCheckoutReceivedMessage(meta, payload)

// Sent Messages

internal data class ShippingOptionsSuccessMessage(
    val meta: AfterpayCheckoutMessageMeta,
    val payload: List<ShippingOption>
)

internal data class ShippingOptionsErrorMessage(
    val meta: AfterpayCheckoutMessageMeta,
    val error: ShippingOptionsError
)

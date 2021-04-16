package com.afterpay.android.internal

import com.afterpay.android.model.ShippingAddress
import com.afterpay.android.model.ShippingOption
import com.afterpay.android.model.ShippingOptionsErrorResult
import com.afterpay.android.model.ShippingOptionsResult
import com.afterpay.android.model.ShippingOptionsSuccessResult

internal data class AfterpayCheckoutMessageMeta(val requestId: String)

internal sealed class AfterpayCheckoutMessage(
    open val meta: AfterpayCheckoutMessageMeta,
) {
    companion object {
        fun fromShippingOptionsResult(
            result: ShippingOptionsResult,
            meta: AfterpayCheckoutMessageMeta
        ): AfterpayCheckoutMessage = when (result) {
            is ShippingOptionsErrorResult -> CheckoutErrorMessage(meta, result.error.name)
            is ShippingOptionsSuccessResult -> ShippingOptionsMessage(meta, result.shippingOptions)
        }
    }
}

internal data class CheckoutLog(
    val severity: String,
    val message: String
)

internal data class CheckoutLogMessage(
    override val meta: AfterpayCheckoutMessageMeta,
    val payload: CheckoutLog
) : AfterpayCheckoutMessage(meta)

internal data class CheckoutErrorMessage(
    override val meta: AfterpayCheckoutMessageMeta,
    val error: String
) : AfterpayCheckoutMessage(meta)

internal data class ShippingAddressMessage(
    override val meta: AfterpayCheckoutMessageMeta,
    val payload: ShippingAddress
) : AfterpayCheckoutMessage(meta)

internal data class ShippingOptionMessage(
    override val meta: AfterpayCheckoutMessageMeta,
    val payload: ShippingOption
) : AfterpayCheckoutMessage(meta)

internal data class ShippingOptionsMessage(
    override val meta: AfterpayCheckoutMessageMeta,
    val payload: List<ShippingOption>
) : AfterpayCheckoutMessage(meta)

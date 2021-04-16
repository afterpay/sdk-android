package com.afterpay.android

import com.afterpay.android.model.ShippingAddress
import com.afterpay.android.model.ShippingOption
import com.afterpay.android.model.ShippingOptionsResult

interface AfterpayCheckoutV2Handler {
    fun didCommenceCheckout(onTokenLoaded: (Result<String>) -> Unit)

    fun shippingAddressDidChange(
        address: ShippingAddress,
        onProvideShippingOptions: (ShippingOptionsResult) -> Unit
    )

    fun shippingOptionDidChange(shippingOption: ShippingOption)
}

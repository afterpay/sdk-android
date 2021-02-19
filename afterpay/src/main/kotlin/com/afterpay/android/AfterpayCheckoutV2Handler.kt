package com.afterpay.android

import com.afterpay.android.model.ShippingAddress
import com.afterpay.android.model.ShippingOption

interface AfterpayCheckoutV2Handler {
    fun didCommenceCheckout(onTokenLoaded: (Result<String>) -> Unit)

    fun shippingAddressDidChange(
        address: ShippingAddress,
        onProvideShippingOptions: (List<ShippingOption>) -> Unit
    )

    fun shippingOptionDidChange(shippingOption: ShippingOption)
}

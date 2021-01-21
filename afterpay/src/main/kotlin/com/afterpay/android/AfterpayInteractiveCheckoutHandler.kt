package com.afterpay.android

import android.net.Uri
import com.afterpay.android.model.ShippingAddress
import com.afterpay.android.model.ShippingOption

interface AfterpayInteractiveCheckoutHandler {
    fun didCommenceCheckout(completion: (Result<Uri>) -> Unit)

    fun shippingAddressDidChange(
        address: ShippingAddress,
        completion: (List<ShippingOption>) -> Unit
    )

    fun shippingOptionDidChange(shippingOption: ShippingOption)
}

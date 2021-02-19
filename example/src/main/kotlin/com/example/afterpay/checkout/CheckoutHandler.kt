package com.example.afterpay.checkout

import com.afterpay.android.AfterpayCheckoutV2Handler
import com.afterpay.android.model.ShippingAddress
import com.afterpay.android.model.ShippingOption

class CheckoutHandler(
    val onDidCommenceCheckout: () -> Unit,
    val onShippingAddressDidChange: (ShippingAddress) -> Unit,
    val onShippingOptionDidChange: (ShippingOption) -> Unit
): AfterpayCheckoutV2Handler {
    private var onTokenLoaded: (Result<String>) -> Unit = {}

    override fun didCommenceCheckout(onTokenLoaded: (Result<String>) -> Unit) =
        onDidCommenceCheckout().also { this.onTokenLoaded = onTokenLoaded }

    fun provideTokenResult(tokenResult: Result<String>) = onTokenLoaded(tokenResult)
        .also { onTokenLoaded = {} }

    private var onProvideShippingOptions: (List<ShippingOption>) -> Unit = {}

    override fun shippingAddressDidChange(
        address: ShippingAddress,
        onProvideShippingOptions: (List<ShippingOption>) -> Unit
    ) = onShippingAddressDidChange(address).also {
        this.onProvideShippingOptions = onProvideShippingOptions
    }

    fun provideShippingOptions(shippingOptions: List<ShippingOption>) =
        onProvideShippingOptions(shippingOptions).also { onProvideShippingOptions = {} }

    override fun shippingOptionDidChange(shippingOption: ShippingOption) =
        onShippingOptionDidChange(shippingOption)
}

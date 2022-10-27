package com.example.afterpay.checkout

import com.afterpay.android.AfterpayCheckoutV2Handler
import com.afterpay.android.model.ShippingAddress
import com.afterpay.android.model.ShippingOption
import com.afterpay.android.model.ShippingOptionUpdateResult
import com.afterpay.android.model.ShippingOptionsResult

class CheckoutHandler(
    val onDidCommenceCheckout: () -> Unit,
    val onShippingAddressDidChange: (ShippingAddress) -> Unit,
    val onShippingOptionDidChange: (ShippingOption) -> Unit,
) : AfterpayCheckoutV2Handler {
    private var onTokenLoaded: (Result<String>) -> Unit = {}

    override fun didCommenceCheckout(onTokenLoaded: (Result<String>) -> Unit) =
        onDidCommenceCheckout().also { this.onTokenLoaded = onTokenLoaded }

    fun provideTokenResult(tokenResult: Result<String>) = onTokenLoaded(tokenResult)
        .also { onTokenLoaded = {} }

    private var onProvideShippingOptions: (ShippingOptionsResult) -> Unit = {}

    override fun shippingAddressDidChange(
        address: ShippingAddress,
        onProvideShippingOptions: (ShippingOptionsResult) -> Unit,
    ) = onShippingAddressDidChange(address).also {
        this.onProvideShippingOptions = onProvideShippingOptions
    }

    fun provideShippingOptionsResult(shippingOptionsResult: ShippingOptionsResult) =
        onProvideShippingOptions(shippingOptionsResult).also { onProvideShippingOptions = {} }

    private var onProvideShippingOptionUpdate: (ShippingOptionUpdateResult?) -> Unit = {}

    override fun shippingOptionDidChange(
        shippingOption: ShippingOption,
        onProvideShippingOptionUpdate: (ShippingOptionUpdateResult?) -> Unit,
    ) = onShippingOptionDidChange(shippingOption).also {
        this.onProvideShippingOptionUpdate = onProvideShippingOptionUpdate
    }

    fun provideShippingOptionUpdateResult(shippingOptionUpdateResult: ShippingOptionUpdateResult?) =
        onProvideShippingOptionUpdate(shippingOptionUpdateResult).also {
            onProvideShippingOptionUpdate = {}
        }
}

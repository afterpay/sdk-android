---
layout: default
title: Checkout V2
parent: Getting Started
nav_order: 3
---

# Checkout V2

{: .alert }
> Checkout v2 is not available at this time for the following regions: France, Italy, Spain.

Checkout V2 requires setting options of type `AfterpayCheckoutV2Options` and creating handler methods for user interactions.

## Launching the Checkout

Launch the Afterpay checkout v2 flow by starting the intent provided by the SDK for the given options.

{: .info }
> When creating a checkout token, `popupOriginUrl` must be set to `https://static.afterpay.com`. The SDK’s example merchant server sets the parameter [here](https://github.com/afterpay/sdk-example-server/blob/master/src/routes/checkout.ts#L28). See the [API reference][express-checkout] for more details! Failing to do so will cause undefined behavior.

For more information on express checkout, including the available options and callbacks, please check the [API reference][express-checkout].

```kotlin
class ExampleActivity: Activity {
    private companion object {
        const val CHECKOUT_WITH_AFTERPAY = 1234
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // ...

        Afterpay.setCheckoutV2Handler(object : AfterpayCheckoutV2Handler {
            override fun didCommenceCheckout(onTokenLoaded: (Result<String>) -> Unit) {
                TODO("Load the token passing the result to completion")
            }

            override fun shippingAddressDidChange(
                address: ShippingAddress,
                onProvideShippingOptions: (ShippingOptionsResult) -> Unit
            ) {
                TODO("Use the address to form shipping options and pass to completion")
            }

            override fun shippingOptionDidChange(
                shippingOption: ShippingOption,
                onProvideShippingOptionUpdate: (ShippingOptionUpdateResult?) -> Unit
            ) {
                TODO("Optionally update your application model with the selected shipping option")
            }
        })

        val afterpayCheckoutButton = findViewById<Button>(R.id.button_afterpay)
        afterpayCheckoutButton.setOnClickListener {
            val options = AfterpayCheckoutV2Options(isPickup, isBuyNow, isShippingOptionsRequired)
            val intent = Afterpay.createCheckoutV2Intent(this, options)
            startActivityForResult(intent, CHECKOUT_WITH_AFTERPAY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // ...

        when (requestCode to resultCode) {
            CHECKOUT_WITH_AFTERPAY to RESULT_OK -> {
                val token = Afterpay.parseCheckoutSuccessResponse(data!!)
                TODO("Capture payment with token")
            }
            CHECKOUT_WITH_AFTERPAY to RESULT_CANCELED -> {
                val status = Afterpay.parseCheckoutCancellationResponse(data!!)
                TODO("Notify user of checkout cancellation")
            }
        }
    }
}
```

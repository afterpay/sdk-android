---
layout: default
title: Checkout V1
parent: Getting Started
nav_order: 2
---

# Checkout V1

Checkout V1 requires a URL to be generated using the checkout API and using it to start the intent provided by the SDK.

## Launching the Checkout

Launch the Afterpay standard checkout flow by starting the intent provided by the SDK for a given checkout URL.

{: .info }
> When creating a checkout token, both `redirectConfirmUrl` and `redirectCancelUrl` must be set. Failing to do so will cause undefined behavior. The SDK’s example merchant server sets the parameters [here][example-server-props]{:target='_blank'}. See more by checking the [api reference][api-reference-props]{:target='_blank'}.

```kotlin
class ExampleActivity: Activity {
    private companion object {
        const val CHECKOUT_WITH_AFTERPAY = 1234
    }

    override fun onCreate(savedInstanceState: Bundle) {
        // ...

        val afterpayCheckoutButton = findViewById<Button>(R.id.button_afterpay)
        afterpayCheckoutButton.setOnClickListener {
            val checkoutUrl = api.checkoutWithAfterpay(cart)
            val intent = Afterpay.createCheckoutIntent(this, checkoutUrl)
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

[example-server-props]: https://github.com/afterpay/sdk-example-server/blob/5781eadb25d7f5c5d872e754fdbb7214a8068008/src/routes/checkout.ts#L26-L27
[api-reference-props]: https://developers.afterpay.com/afterpay-online/reference/javascript-afterpayjs#redirect-method

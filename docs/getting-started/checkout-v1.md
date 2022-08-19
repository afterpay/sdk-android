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

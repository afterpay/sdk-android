---
layout: default
title: Checkout V2
parent: Getting Started
nav_order: 3
---

# Checkout V2
{: .no_toc }

<details markdown="block" open>
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>

Checkout V2 requires setting options of type `AfterpayCheckoutV2Options` and creating handler methods for user interactions.

## Launching the Checkout

Launch the Afterpay checkout V2 flow by starting the intent provided by the SDK for the given options.

{: .info }
> When creating a checkout token, `popupOriginUrl` must be set to `https://static.afterpay.com`. The SDK’s example merchant server sets the parameter [here][example-server-param]{:target="_blank"}. See the [API reference][express-checkout]{:target="_blank"} for more details. Failing to do so will cause undefined behavior.

For more information on express checkout, including the available options and callbacks, please check the [API reference][express-checkout]{:target="_blank"}.

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

            // To update the shipping method, pass in a ShippingOptionUpdate object to
            // completion, otherwise pass nil
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

## Sequence Diagram

The below diagram describes the happy path.

``` mermaid
sequenceDiagram
  participant App
  participant Afterpay SDK
  participant Proxy Server
  participant Afterpay API

  Note over App,Afterpay API: Setup

  App->>Afterpay SDK: Configure the SDK

  App->>Afterpay SDK: Setup checkout handlers

  Note over App,Afterpay API: Create checkout and Capture

  App->>Proxy Server: Get Checkout Token Request

  Proxy Server->>Afterpay API: Create Checkout Request
  Note over Proxy Server,Afterpay API: Ensure same environment<br>as Afterpay SDK config

  Afterpay API-->>Proxy Server: Create Checkout Response
  Note over Afterpay API,Proxy Server: Body contains a Token

  Proxy Server-->>App: Get Token Response

  App->>Afterpay SDK: Launch the checkout<br>with the Token

  Note over App,Afterpay API: Consumer confirms Afterpay checkout

  Afterpay SDK-->>App: Checkout result

  App->>Proxy Server: Capture request

  Proxy Server->>Afterpay API: Capture request

  Afterpay API-->>Proxy Server: Capture response

  Proxy Server-->>App: Capture Response

  App->>App: Handle response
```

[example-server-param]: https://github.com/afterpay/sdk-example-server/blob/5781eadb25d7f5c5d872e754fdbb7214a8068008/src/routes/checkout.ts#L28
[express-checkout]: https://developers.afterpay.com/afterpay-online/reference#what-is-express-checkout

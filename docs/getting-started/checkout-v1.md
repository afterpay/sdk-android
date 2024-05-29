---
layout: default
title: Checkout V1
parent: Getting Started
nav_order: 2
---

# Checkout V1
{: .no_toc }

<details markdown="block" open>
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>

Checkout V1 requires a URL to be generated using the checkout API and using it to start the intent provided by the SDK.

## Launching the Checkout

Launch the Afterpay standard checkout flow by starting the intent provided by the SDK for a given checkout URL.

{: .info }
> When creating a checkout token, both `redirectConfirmUrl` and `redirectCancelUrl` must be set. Failing to do so will cause undefined behavior. The SDK’s example merchant server sets the parameters [here][example-server-props]{:target='_blank'}. See more by checking the [api reference][api-reference-props]{:target='_blank'}.
>
> By default the SDK *will not* load these redirect URLs when the checkout is confirmed or cancelled, but will allow the result to be handled as seen in the example below. If it is required that these URLs be loaded, the `loadRedirectUrls` parameter can be set to `true` on the `createCheckoutIntent` method.

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
      /**
        * The `createCheckoutIntent` method takes an optional 3rd parameter: `loadRedirectUrls`
        * of type boolean. Setting this to true will allow the redirect urls that were set when
        * creating the checkout to be loaded.
        *
        * The default & recommended value is false unless under specific circumstances this is required.
        */
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
  Note over App,Afterpay SDK: Only required if<br>setting consumer locale

  Note over App,Afterpay API: Create checkout and Capture

  App->>Proxy Server: Get Checkout URL Request

  Proxy Server->>Afterpay API: Create Checkout Request
  Note over Proxy Server,Afterpay API: Ensure same environment<br>as Afterpay SDK config

  Afterpay API-->>Proxy Server: Create Checkout Response
  Note over Afterpay API,Proxy Server: Body contains a URL

  Proxy Server-->>App: Get URL Response

  App->>Afterpay SDK: Launch the checkout<br>with the URL

  Note over App,Afterpay API: Consumer confirms Afterpay checkout

  Afterpay SDK-->>App: Checkout result

  App->>Proxy Server: Capture request

  Proxy Server->>Afterpay API: Capture request

  Afterpay API-->>Proxy Server: Capture response

  Proxy Server-->>App: Capture Response

  App->>App: Handle response
```

[example-server-props]: https://github.com/afterpay/sdk-example-server/blob/5781eadb25d7f5c5d872e754fdbb7214a8068008/src/routes/checkout.ts#L26-L27
[api-reference-props]: https://developers.afterpay.com/afterpay-online/reference/javascript-afterpayjs#redirect-method

---
layout: default
title: Checkout V3
parent: Getting Started
nav_order: 5
---

# Checkout V3
{: .no_toc }

<details markdown="block" open>
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>


{: .alert }
> Checkout V3 is currently available in the following region(s): US, UK, Australia and Canada

Checkout V3 generates a one-time payment card for every Afterpay order and provides the card number to insert into your credit card checkout. This allows for a front-end-only integration. Unlike V1 and V2, with V3 your server does not directly interact with Afterpay APIs. The one-time payment card is Visa for the US, UK, and Canada. In Australia the one-time payment card is Mastercard.

## How it works

The transaction uses a one-time virtual payment card, which has a unique card number. Once the virtual card exists, you use it to handle authorization, capture, and refunds. Your integration is simplified, as you don’t have to integrate with additional endpoints.

{: .note }
Always set V3 Configuration before presentation, otherwise you will incur an assertionFailure. See the **Set Configuration** section below.

## Step 1: Set the V3 Configuration


```kotlin
Afterpay.setConfigurationV3(
	CheckoutV3Configuration(
        shopDirectoryMerchantId = "your_merchant_id",
        region = AfterpayRegion.US,
        environment = AfterpayEnvironment.PRODUCTION,
    )
)
```

## Step 2: Start checkout on button click

On button click, start the checkout dialog by creating an `Intent` and launching `Activity` for result:

```kotlin
val intent =
    Afterpay.createCheckoutV3Intent(
        context = view.context,
        consumer = // TODO create customer object,
        orderTotal = // TODO create order total,
        items = // TODO create list of items,
        configuration = // TODO required if you didn't previously call setConfigurationV3(),
    )
activityResultLauncher.launch(intent)

```
## Step 3: Listen for Activity result

Results will contain either a `RESULT_OK` or `RESULT_CANCELED`. Parse the response with either `Afterpay.parseCheckoutSuccessResponseV3` or `Afterpay.parseCheckoutCancellationResponseV3` respectively.

```kotlin
private val activityResultLauncher =
registerForActivityResult(
    ActivityResultContracts.StartActivityForResult(),
) { result: ActivityResult ->
    val intent = result.data
    checkNotNull(intent)
    if (result.resultCode == RESULT_OK) {
        Afterpay.parseCheckoutSuccessResponseV3(intent)?.let {
	        // TODO next step
        }
    } else if (result.resultCode == RESULT_CANCELED) {
        Afterpay.parseCheckoutCancellationResponseV3(intent)
            ?.let {
                    (
                        cancellationStatusV3: CancellationStatusV3,
                        exception: Exception?,
                    ),
                ->
                Log.e(tag, cancellationStatusV3.toString(), exception)
            }
    }
}
```

## Step 4: Receive one-time use card details and process

The success result will contain card details, tokens, and a valid-until time. Pass these back to your own server and process them through your normal card processing infrastructure, or pass them on to another mobile card processing SDK .

```kotlin
Afterpay.parseCheckoutSuccessResponseV3(intent)?.let {
  it.cardDetails
  it.tokens
  it.cardValidUntil
```
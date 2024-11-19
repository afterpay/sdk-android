---
layout: default
title: Checkout V3 with Cash App Pay
parent: Getting Started
nav_order: 7
---

# Checkout V3 with Cash App Pay
{: .d-inline-block .no_toc }
NEW (v4.3.0)
{: .label .label-green }


<details markdown="block">
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>

{: .alert }
> Cash App Pay is currently available in the following region(s): US

This method requires importing and implementing the Cash App PayKit SDK in addition to the Afterpay SDK.

Follow along with the [example implementation](https://github.com/afterpay/sdk-android/blob/master/sample/src/main/java/com/example/CashAppV3SampleActivity.kt).

# Initial Setup

## Import the Cash App Pay Kit Dependency

You can get the latest version of the SDK from Maven. This is the import definition using Gradle:

```gradle
implementation "app.cash.paykit:core:2.3.0"
```

For definitions of other build systems, see [Cash App Pay Kit on Maven Central][cash-on-maven]{:target="_blank"}.

{: .info }
> Version `v2.3.0` of the SDK is `12.3 kB`.

## Implement Deep Linking

The authorization flow will bring Cash App to the foreground on the Customer’s device. After the Customer either authorizes or declines, the Cash App Pay SDK will attempt to return your app to the foreground.  This is accomplished by [declaring an intent filter][intent-filter]{:target="_blank"} on your app's Android Manifest. You will pass a corresponding redirect URI when launching Cash App Pay SDK, see steps below.

```xml
<!-- Intent filter to allow Cash App Pay SDK to redirect to your app.
	Consider creating a custom scheme to ensure only your app is launched. -->
<intent-filter>
  <action android:name="android.intent.action.VIEW" />
    <data
       android:host="example.com"
      android:scheme="example" />

  <category android:name="android.intent.category.BROWSABLE" />
  <category android:name="android.intent.category.DEFAULT" />
</intent-filter>
```

# Authorization steps

## Step 1: Set merchant configurations

{: .note }
> Confirm that the Afterpay SDK is configured per the [instructions][configure-afterpay] before attempting to access `Afterpay.environment.payKitClientId`.

```kotlin
Afterpay.setConfigurationV3(configurations)
```

## Step 2: Create `CashAppPay` instance and register a state listener

To create a new instance of `CashAppPay`, you must pass the `clientId`. This is a required field. This can be retrieved through the Afterpay object: `Afterpay.environment.payKitClientId`.

You should use `CashAppPayFactory` to create an instance of `CashAppPay`. When doing so, you must specify the environment you will use, Sandbox or Production. The function `createSandbox()` will create an instance in the Sandbox environment.

{: .info }
> You should use the Sandbox environment during the development phase and the Production environment for your production releases.


{: .info }
**Optional:** We recommend using the [Cash App Sandbox App][sandbox-app]{:target="_blank"} to test the payment flow in the Sandbox environment during your development phase.

```kotlin
private lateinit var cashAppPay: CashAppPay

private val cashAppPayListener =
    object : CashAppPayListener {
        override fun cashAppPayStateDidChange(newState: CashAppPayState) {
           // TODO()
        }
    }
    
...    
        
private fun initializeCashAppSDK() {
    cashAppPay = CashAppPayFactory.createSandbox(Afterpay.environment.payKitClientId) // sandbox
      
      or 
      
    cashAppPay = CashAppPayFactory.create(Afterpay.environment.payKitClientId) // production
    cashAppPay.registerForStateUpdates(cashAppPayListener)
}
```

### States

`CashAppPayState` is a sealed class. Some states are for information only, but most will drive the logic of your integration. The most critical states to handle are in the table below:

| State  | Description |
|:-------|:------------|
| `ReadyToAuthorize` | You should show the Cash App Pay button in your UI and call `authorizeCustomerRequest()` when it is tapped. |
| `Approved` | Grants are ready for your app to send to Afterpay SDK. |
| `Declined` | Customer has declined the Cash App Pay authorization and must start the flow over or choose a new payment method. |
| `CashAppPayExceptionState` | The general wrapper state for exceptions. These can range from integration errors to network errors. The exception states are emitted only for unrecoverable error states. |

Handling of each of these states is outlined in further detail below.

## Step 3: Begin checkout

Begin checkout process by requesting data from Afterpay. Optionally, supply data on the customer, order total, and items being purchased. Set `configuration` now if you failed to set it previously.

```kotlin
Afterpay.beginCheckoutV3WithCashAppPay(
    consumer = // TODO,
    orderTotal = // TODO,
    items = // TODO,
    configuration = // TODO, optional,
).let { result: Result<CheckoutV3CashAppPay> ->
   // see next step...
}
```

## Step 4: Save result

You will receive a `Result<CheckoutV3CashAppPay>` on success or failure. When successful, save the `result` for later and begin the customer request process.

```kotlin
private var checkoutV3CashAppPay: CheckoutV3CashAppPay? = null

    result.onSuccess {
        checkoutV3CashAppPay = it
        createCashAppPayCustomerRequest(it)
    }

    result.onFailure { error ->
        // TODO handle error
    }

```

## Step 5: Create customer request

Using the `CheckoutV3CashAppPay` from the previous step, create a `OneTimeAction` with Cash App Pay SDK. Also supply the redirect URI which matches the `IntentFilter` defined in your `AndroidManifest.xml` (see above)

```kotlin
fun createCashAppPayCustomerRequest(checkoutV3CashAppPay: CheckoutV3CashAppPay) {
    val redirectUri = "example://example.com/"
    // must convert from dollars to cents
    val cents = (checkoutV3CashAppPay.amount * 100).toInt()
    val action = CashAppPayPaymentAction.OneTimeAction(
        currency = USD,
        amount = cents,
        scopeId = checkoutV3CashAppPay.brandId,
    )

    cashAppPay.createCustomerRequest(
        action,
        redirectUri = redirectUri,
    )
}
```

## Step 6: Respond to ReadyToAuthorize

Inside the `CashAppPayListener` you created earlier, respond to `ReadyToAuthorize` state by enabling your "Pay With Cash App" button:

```kotlin
private val cashAppPayListener =
    object : CashAppPayListener {
        override fun cashAppPayStateDidChange(newState: CashAppPayState) {
            when (newState) {
                is ReadyToAuthorize -> {
                    lifecycleScope.launch { // jump back to UI thread to update UI
                        bindings.cashappPayButton.isEnabled = true
                    }
                }
               ... // other states 
            }
        }
    }

```

## Step 7: Authorize when customer clicks button

When beginning the authorization request, Cash App Pay SDK will redirect the Customer to Cash App, where they can approve or decline the grant request.

```kotlin
bindings.cashappPayButton.apply {
    setOnClickListener { _ ->
        cashAppPay.authorizeCustomerRequest()
    }
}
```

## Step 8: Disable button when authorization is in flight

Avoid duplicate button clicks by disabling once an authorization is detected.

```kotlin
private val cashAppPayListener =
    object : CashAppPayListener {
        override fun cashAppPayStateDidChange(newState: CashAppPayState) {
            when (newState) {
                Authorizing -> {
                    bindings.cashappPayButton.isEnabled = false
                }
               ... // other states 
            }
        }
    }
```

## Step 9: Respond to Approved or Declined

Cash App Pay SDK will redirect back to your app (per the supplied redirect URI). `CashAppPayListener` will receive either a `Approved` or `Declined` response.

```kotlin
private val cashAppPayListener =
    object : CashAppPayListener {
        override fun cashAppPayStateDidChange(newState: CashAppPayState) {
            Log.d(tag, "cashAppPayStateDidChange: ${newState::class.java}")
            when (newState) {
                is Approved -> {
                    newState.responseData.apply {
                        grants?.get(0)?.let { grant: Grant ->
                            confirmCheckoutWithAfterpay(
                                grantId = grant.id,
                                customerId = grant.customerId,
                            )
                        }
                    }
                }

                Declined -> {
                    // TODO 
                }
               ... // other states 
            }
        }
    }
```

# Step 10: Pass grant back to Afterpay

By combining the `CheckoutV3CashAppPay` you stored before, with the newly received `grantId` and `customerId`, you can now confirm checkout with Afterpay

```kotlin
fun confirmCheckoutWithAfterpay(
    grantId: String,
    customerId: String,
) {
    checkoutV3CashAppPay?.let { it ->
        Afterpay.confirmCheckoutV3WithCashAppPay(
            grantId = grantId,
            customerId = customerId,
            token = it.token,
            singleUseCardToken = it.singleUseCardToken,
            jwt = it.jwt,
            configuration = createCheckoutV3Configuration(),
        ).let { result: Result<CheckoutV3Data> ->
            result.onSuccess {
             // TODO next step
            }

            result.onFailure {
             // TODO
            }
        }
    }
}
```

## Step 11: Receive one-time use card details and process

The success result will contain card details, tokens, and a valid-until time. Pass these back to your own server and process them through your normal card processing infrastructure, or pass them on to another mobile card processing SDK .

```kotlin
result.onSuccess {
    it.cardDetails
    it.tokens
    it.cardValidUntil
}
```

## Step 12: Unregister

You should also use the **Unregister** function when you're done with the SDK:

``` kotlin
cashAppPay.unregisterFromStateUpdates()
```

[cash-on-maven]: https://central.sonatype.com/artifact/app.cash.paykit/core/2.3.0/overview
[intent-filter]: https://developer.android.com/training/app-links/deep-linking#adding-filters
[custom-url-schemes]: https://developer.apple.com/documentation/xcode/defining-a-custom-url-scheme-for-your-app
[sandbox-app]: https://developers.cash.app/docs/api/technical-documentation/sandbox/sandbox-app

---
layout: default
title: Cash App Pay
parent: Getting Started
nav_order: 4
---

# Cash App Pay
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

With our latest enhancements, you can now support taking Cash App Pay payments using your Afterpay merchant account. To do this, you must generate a token by sending a server-to-server call to the [Afterpay API Create Checkout endpoint][create-checkout-endpoint-docs]{:target="_blank"} with the parameter `isCashAppPay` set to `true`. This method requires importing and implementing the Cash App Pay Kit SDK.

{: .info }
> When creating a checkout token, you must set both `redirectConfirmUrl` and `redirectCancelUrl`. If they are not set, an error will be returned from the server and the SDK will output a malformed JSON error. The SDK’s example merchant server sets the parameters [here][example-server-props]{:target='_blank'}. ee more details at [Redirect Method][api-reference-props]{:target='_blank'} in the Standard Checkout API.

## Step 1: Import the Cash App Pay Kit Dependency

You can get the the latest version of the SDK from Maven. This is the import definition using Gradle:

```gradle
implementation "app.cash.paykit:core:2.0.0"
```

For definitions of other build systems, see [Cash App Pay Kit on Maven Central][cash-on-maven]{:target="_blank"}.

{: .info }
> Version `v2.0.0` of the SDK size is `12.3 kB`.

## Step 2: Create a Cash App Pay Kit SDK Instance

To create a new instance of the Cash App Pay Kit SDK, you must pass the `clientId`. This is a required field. This can be retrieved through the Afterpay object: `Afterpay.environment.payKitClientId`.

{: .note }
> Confirm that the Afterpay SDK is configured per the [instructions][configure-afterpay] before attempting to access `Afterpay.environment.payKitClientId`


You should use `CashAppPayFactory` to create an instance of the Cash App Pay Kit SDK. When doing so, you must specify the environment you will use, Sandbox or Production. The function `createSandbox()` will create an SDK instance in the Sandbox environment.

{: .info }
> You should use the Sandbox environment during the development phase and the Production environment for your production releases.

Creating a Sandbox Cash App Pay Kit SDK instance:

``` kotlin
val payKit : CashAppPay = CashAppPayFactory.createSandbox(Afterpay.environment.payKitClientId)
```

Creating a Production Cash App Pay Kit SDK instance:
``` kotlin
val payKit : CashAppPay = CashAppPayFactory.create(Afterpay.environment.payKitClientId)
```

{: .info }
**Optional:** We recommend using the [Cash App Sandbox App][sandbox-app]{:target="_blank"} to test the payment flow in the Sandbox environment during your development phase.

## Step 3: Register for State Updates

To receive updates from Pay Kit, you’ll need to implement the `CashAppPayListener` interface. The interface exposes a single function, which gets called whenever there’s an internal state change emitted by the SDK:

``` kotlin
interface CashAppPayListener {
   fun cashAppPayStateDidChange(newState: CashAppPayState)
}
```

You register with the SDK instance you’ve created above:

``` kotlin
payKit.registerForStateUpdates(this)
```

You should also use the **Unregister** function when you're done with the SDK:

``` kotlin
payKit.unregisterFromStateUpdates()
```

### States

`CashAppPayState` is a sealed class parameter. We suggest that you use a Kotlin `when` statement on it. Some of these possible states are for information only, but most drive the logic of your integration. The most critical states to handle are in the table below:

| State  | Description |
|:-------|:------------|
| `ReadyToAuthorize` | You should show the Cash App Pay button in your UI and call `authorizeCustomerRequest()` when it is tapped. |
| `Approved` | Grants are ready for your backend to use and to create a payment. |
| `Declined` | Customer has declined the Cash App Pay authorization and must start the flow over or choose a new payment method. |
| `CashAppPayExceptionState` | The general wrapper state for exceptions. These can range from integration errors to network errors. The exception states are emitted only for unrecoverable error states. |

## Step 4: Implement Deep Linking

The authorization flow will bring Cash App to the foreground on the Customer’s device. After the Customer either authorizes or declines, your app must be returned to the foreground, which means we need a way to call your app from Cash App.  This is accomplished by [declaring an incoming intent][intent-filter]{:target="_blank"} filter on your app's Android Manifest and passing a corresponding redirect URI that uses the SDK when creating a customer request (as can be seen on the next step).

Here’s an example of how this integration looks for your `AndroidManifest`:

``` xml
<intent-filter>
  <action android:name="android.intent.action.VIEW" />

  <category android:name="android.intent.category.DEFAULT" />
  <category android:name="android.intent.category.BROWSABLE" />

  <!-- Register the Cash App Pay Kit redirect URI or URL. Change this accordingly in your app. -->
  <data
      android:scheme="cashpaykit"
      android:host="checkout" />
</intent-filter>
```

## Step 5: Create a Customer Request

You can create a customer request as soon as you know the amount you’d like to charge or if you'd like to create an on-file payment request. You can create this request as soon as your **checkout view controller** loads, so that your customer can authorize the request without delay.

### Step 5A: Sign the Order Token

After retrieving the token from your server-to-server call, you must sign the order, so that you can retrieve the JWT and associated data. This can be done either by the suspending function or with the asynchronous version of it.

**Example of the suspending function:**

``` kotlin
Afterpay.signCashAppOrder(token) { cashAppData ->
  when (cashAppData) {
    is CashAppSignOrderResult.Success -> TODO("Create the Pay Kit customemr request")
    is CashAppSignOrderResult.Failure -> TODO("Display an error and restart payment flow")
  }
}
```

**Example of the asynchronous version of the suspending function:**

``` kotlin
Afterpay.signCashAppOrderAsync(token) { cashAppData ->
  when (cashAppData) {
    is CashAppSignOrderResult.Success -> TODO("Create the Pay Kit customemr request")
    is CashAppSignOrderResult.Failure -> TODO("Display an error and restart payment flow")
  }
}
```

### Step 5B: Create a Pay Kit Customer Request

To charge a one-time payment, your **Create Request** call might look like this (in the following example, `cashAppData` is the response object that is returned in the trailing lambda in step 5A, which would echo the amount from your server-to-server **Create Checkout** call):

``` kotlin
val request = CashAppPayPaymentAction.OneTimeAction(
  currency = CashAppPayCurrency.USD,
  amount = (cashAppData.amount * 100).toInt(),
  scopeId = cashAppData.merchantId,
)

payKit.createCustomerRequest(request, cashAppData.redirectUri)
```

## Step 6: Authorize the Customer Request

### Step 6A: Add an Authorize Request Event to Cash App Pay button

Once the Cash App Pay Kit SDK is in the `ReadyToAuthorize` state, you can display the Cash App Pay button. When the customer taps the button, you can authorize the customer request. See [Cash Button Docs][cash-button-docs]{:target='_blank'} to learn more about the Cash App Pay button component.

``` kotlin
payKit.authorizeCustomerRequest()
```

{: .info }
> Currently, the Button provided by the SDK is unmanaged. This means that it's a stylized button which isn't aware of SDK events out-of-the-box. It is the developer's responsibility to call the above method when the button is tapped and also manage any disabled and loading states.

Your app will redirect to Cash App for authorization. When the authorization is completed, your redirect URI will be called to open your app. The SDK will fetch your authorized request and return it to your callback listener as one of 2 states: `Approved` or `Declined`.

### Step 6B: Validate the Cash App Pay Order

{: .alert }
> This step must not be skipped

Finally, you must validate the Cash App order. This will look like the following example:

``` kotlin
Afterpay.validateCashAppOrder(
    jwt,
    customerResponseData.customerProfile!!.id,
    grant.id,
) { validationResult ->
    when (validationResult) {
      is CashAppValidationResponse.Success -> TODO("Capture payment with token and grant id")
      is CashAppValidationResponse.Failure -> TODO("Handle an invalid Cash App order")
    }
}
```


## Step 7: Pass Grants to the Backend and Capture Payment

The `Approved` state will contain a **Grants list** object associated with it and it can be used with Afterpay's **Immediate Payment Capture** or **Deferred Payment Auth** API. Pass the **grant ID** along with the token to capture/authorize using a server-to-server request.

[cash-on-maven]: https://central.sonatype.com/artifact/app.cash.paykit/core/1.0.3/overview
[configure-afterpay]: ../configuring-the-sdk
[sandbox-app]: https://cashapp-pay.stoplight.io/docs/api/technical-documentation/sandbox/sandbox-app
[intent-filter]: https://developer.android.com/training/app-links/deep-linking#adding-filters
[example-server-props]: https://github.com/afterpay/sdk-example-server/blob/5781eadb25d7f5c5d872e754fdbb7214a8068008/src/routes/checkout.ts#L26-L27
[api-reference-props]: https://developers.afterpay.com/afterpay-online/reference/javascript-afterpayjs#redirect-method
[cash-button-docs]: https://cashapp-pay.stoplight.io/docs/api/technical-documentation/sdks/pay-kit/android-getting-started#cashpaykitbutton
[create-checkout-endpoint-docs]: https://developers.afterpay.com/afterpay-online/reference/create-checkout-1

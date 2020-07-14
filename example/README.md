# Example integration of the Afterpay SDK

This example demonstrates typical integration of the Afterpay SDK with an Android application. It provides a simple shopping experience which offers Afterpay as a payment method during checkout.

## Requirements

The application requires the [sample integration server][sample-server] to checkout with Afterpay, and expects it to be running at http://localhost:3000. Refer to the sample server repository for [detailed instructions][sample-server-instructions] on how to build and run the server locally.

## Structure

The example application is intentionally simple and makes use of standard Android conventions and components to ensure familiarity with the majority of Android developers. It also utilizes Kotlin features such as [coroutines][kotlin-coroutines] and [Flows][kotlin-flow] to simplify asynchronous server and UI interactions.

The flow of the application can be broken down into the following screens:

- [Product list](#product-list)
- [Checkout](#checkout)
- [Transaction Receipt](#transaction-receipt)

### Product list

The product screen contains a list of grocery items which can be added to and removed from the cart. This screen has no contact with the Afterpay SDK and its role is merely to populate the cart with items to be purchased at checkout.

### Checkout

The checkout screen contains the logic for integration with the Afterpay SDK. It is also responsible for communicating with the server and acquiring the URL for initiating the Afterpay checkout flow.

> **NOTE:** The app uses [the provided sample server][sample-server] for integration with the Afterpay API. A similar approach should be included with the backend your application uses to query for configuration and generate a checkout URL required to initiate the Afterpay SDK checkout flow.

An email address is requested before proceeding with the purchase from our example store, and is sent to the server to generate a unique Afterpay checkout URL. This URL is provided to the [`Afterpay.createCheckoutIntent()`][sdk-create-checkout] SDK method which generates an `Intent` used to initiate the web checkout flow.

> **NOTE:** The intent is launched with [`startActivityForResult()`][activity-start] as the application will need to be notified when the transaction completes, and of its result.

A successful transaction completes with a `RESULT_OK` result code, and the intent passed to [`onActivityResult()`][activity-result] will contain information corresponding to the Afterpay transaction. This intent should be provided to the [`Afterpay.parseCheckoutResponse()`][sdk-parse-response] SDK method to obtain the Afterpay transaction token.

If the transaction is cancelled at any point during the web checkout flow, the `RESULT_CANCELED` result code will be returned to the calling activity.

### Transaction Receipt

The receipt screen displays the transaction token return by the SDK for a successful checkout flow. Navigating back from this screen will clear the cart and return to the product list to start a new shopping session.

<!-- Links: -->
[activity-start]: https://developer.android.com/reference/android/app/Activity#startActivityForResult(android.content.Intent,%20int)
[activity-result]: https://developer.android.com/reference/android/app/Activity#onActivityResult(int,%20int,%20android.content.Intent)
[kotlin-coroutines]: https://developer.android.com/kotlin/coroutines
[kotlin-flow]: https://kotlinlang.org/docs/reference/coroutines/flow.html
[sample-server]: https://github.com/afterpay/afterpay-example-server
[sample-server-instructions]: https://github.com/afterpay/afterpay-example-server#getting-started
[sdk-create-checkout]: https://github.com/afterpay/afterpay-android/blob/master/afterpay/src/main/kotlin/com/afterpay/android/Afterpay.kt#L19
[sdk-parse-response]: https://github.com/afterpay/afterpay-android/blob/master/afterpay/src/main/kotlin/com/afterpay/android/Afterpay.kt#L30

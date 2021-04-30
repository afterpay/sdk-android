# Afterpay Android SDK

![Build and Test][badge-ci] [![ktlint][badge-ktlint]][ktlint]

The Afterpay Android SDK makes it quick and easy to provide an excellent payment experience in your Android app. We provide powerful and customizable UI elements that can be used out-of-the-box to allow your users to shop now and pay later.

## Contents

- [Installation](#installation)
    - [Requirements](#requirements)
    - [Configuration](#configuration)
    - [ProGuard](#proguard)
- [Features](#features)
- [Getting Started](#getting-started)
    - [Configuring the SDK](#configuring-the-sdk)
    - [Launching the Checkout (v1)](#launching-the-checkout-(v1))
    - [Launching the Checkout (v2)](#launching-the-checkout-(v2))
- [UI Components](#ui-components)
    - [Widget](#widget)
    - [Badge](#badge)
    - [Pay Now Button](#pay-now-button)
    - [Price Breakdown](#price-breakdown)
- [Security](#security)
- [Examples](#examples)
- [Contributing](#contributing)
- [License](#license)

## Installation

### Requirements

- Android 5.0 (API level 21) and above

### Configuration

Add `afterpay-android` to your `build.gradle` dependencies.

```gradle
dependencies {
    implementation 'com.afterpay:afterpay-android:1.3.0'
}
```

### ProGuard

If you are using R8 the shrinking and obfuscation rules are included automatically.

Proguard users will need to manually apply the rules defined in [`consumer-rules.pro`][proguard-rules].

## Features

The SDK provides easy integration of the Afterpay web login and checkout process as well as ready-made UI components. There are two versions of checkout; v1 supports the standard flow while v2 adds support for the richer express experience, the callbacks for which are handled by the provided handler object.

## Getting Started

### Configuring the SDK

Each merchant has configuration specific to their account which is accessible from the `/configuration` API endpoint. This configuration is used by the SDK for rendering UI components and is applied globally using the [`Afterpay.setConfiguration`][docs-configuration] method.

The following sample demonstrates how the SDK can be configured using the data supplied by the Afterpay API. It is up to you to decide how to best supply the locale which will determine the terms and conditions provided and currency formatting of the SDK.

Environment is required only for checkout v2; it's recommended to use `AfterpayEnvironment.PRODUCTION` for release builds and `AfterpayEnvironment.SANDBOX` for all others.

```kotlin
val configuration = api.getConfiguration()

Afterpay.setConfiguration(
    minimumAmount = configuration.minimum?.amount,
    maximumAmount = configuration.maximum.amount,
    currency = configuration.maximum.currency,
    locale = Locale.US,
    environment = AfterpayEnvironment.SANDBOX
)
```

> **NOTE:** The merchant account is subject to change and it is recommended to update this configuration **once per day**. The example project provides a [reference][example-configuration] demonstrating how this may be implemented.

### Launching the Checkout (v1)

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

### Launching the Checkout (v2)

Launch the Afterpay express checkout flow by starting the intent provided by the SDK for the given options. For more information on express checkout, including the available options and callbacks, please check the [API reference][express-checkout].

> **NOTE:** Configuration must always be set before calling checkout v2.

```kotlin
class ExampleActivity: Activity {
    private companion object {
        const val CHECKOUT_WITH_AFTERPAY = 1234
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

            override fun shippingOptionDidChange(shippingOption: ShippingOption) {
                TODO("Optionally update your application model with the selected shipping option")
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle) {
        // ...

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

## UI Components

### Widget

The widget displays the consumer's payment schedule, provided by either a token or monetary amount, after checkout and can be updated to reflect changes such as promotional discounts or shipping costs. The widget will also present any issues that might prevent the order from completing. 

The widget can be added to a layout or instantiated in code but an instance must always be initialised with one of the two `init()` functions.

> **NOTE:** Configuration must always be set before initialising the widget.

### Badge

![Black on Mint badge][badge-black-on-mint] ![Mint on Black badge][badge-mint-on-black] ![White on Black badge][badge-white-on-black] ![Black on White badge][badge-black-on-white]

### Pay Now Button

![Black on Mint pay now button][button-black-on-mint]
![Mint on Black pay now button][button-mint-on-black]
![White on Black pay now button][button-white-on-black]

> **NOTE:** Setting the configured locale to `Locale.UK` (`"en_GB"`) will display Clearpay assets and branding.

### Price Breakdown

The price breakdown component displays information about Afterpay instalments and handles a number of common configurations.

A total payment amount (represented as a `BigDecimal`) must be programatically set on the component to display Afterpay instalment information.

```kotlin
val totalAmount: BigDecimal = getTotalAmount()

val paymentBreakdown = view.findViewById<AfterpayPriceBreakdown>(R.id.priceBreakdown)
paymentBreakdown.totalAmount = totalAmount
```

When the breakdown component is assigned a total amount that is valid for the merchant account, the 4 instalment amounts will be displayed.

![Price breakdown Afterpay instalments are available][breakdown-available]

When the total amount is not within the minimum and maximum payment values for the merchant account, the amounts available for Afterpay will be shown in the component.

![Price breakdown Afterpay instalments are unavailable][breakdown-unavailable-min-max]

When no minimum amount is set and the total amount is greater than the maximum payment values for the merchant account, the maximum amount available for Afterpay will be shown in the component.

![Price breakdown Afterpay instalments are unavailable][breakdown-unavailable-max]

When no payment amount has been set or the merchant account configuration has not been applied to the SDK, the component will default to a message stating Afterpay is available.

![Price breakdown no merchant account configuration][breakdown-no-configuration]

The **Info** link at the end of the component will display a window containing more information about Afterpay for the user.

> **NOTE:** The way the configured currency is formatted and the destination for the info link is determined by the configured locale.

## Security

To limit the possibility of a man-in-the-middle attack during the checkout process, certificate pinning can be configured for the Afterpay portal. Please refer to the Android [Network Security Configuration][network-config] documentation for more information.

Add the following configuration to your `res/xml/network_security_configuration.xml` to enforce certificate pinning for the Afterpay portal.

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config xmlns:tools="http://schemas.android.com/tools">
    <domain-config cleartextTrafficPermitted="false">
        <domain>portal.afterpay.com</domain>
        <pin-set expiration="2022-05-25">
            <pin digest="SHA-256">nQ1Tu17lpJ/Hsr3545eCkig+X9ZPcxRQoe5WMSyyqJI=</pin>
        </pin-set>
    </domain-config>
</network-security-config>
```

> **NOTE:** It is necessary to keep the certificate PINs updated to ensure pinning will not be bypassed beyond the expiry date of the certificate.

## Examples

The [example project][example] demonstrates how to include an Afterpay payment flow using our prebuilt UI components. This project is powered by the [example server][example-server] which shows a simple example of integration with the Afterpay API.

## Contributing

Contributions are welcome! Please read our [contributing guidelines][contributing].

## License

This project is licensed under the terms of the Apache 2.0 license. See the [LICENSE][license] file for more information.

<!-- Links: -->
[badge-ci]: https://github.com/afterpay/sdk-android/workflows/Build%20and%20Test/badge.svg?branch=master&event=push
[badge-ktlint]: https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg
[badge-black-on-mint]: images/badge_black_on_mint.png
[badge-mint-on-black]: images/badge_mint_on_black.png
[badge-white-on-black]: images/badge_white_on_black.png
[badge-black-on-white]: images/badge_black_on_white.png
[breakdown-available]: images/price_breakdown_available.png
[breakdown-no-configuration]: images/price_breakdown_no_configuration.png
[breakdown-unavailable-min-max]: images/price_breakdown_unavailable_min_max.png
[breakdown-unavailable-max]: images/price_breakdown_unavailable_max.png
[button-black-on-mint]: images/button_black_on_mint.png
[button-mint-on-black]: images/button_mint_on_black.png
[button-white-on-black]: images/button_white_on_black.png
[contributing]: CONTRIBUTING.md
[docs-configuration]: https://github.com/afterpay/sdk-android/blob/master/afterpay/src/main/kotlin/com/afterpay/android/Afterpay.kt#L65
[express-checkout]: https://developers.afterpay.com/afterpay-online/reference#what-is-express-checkout
[example]: example
[example-configuration]: https://github.com/afterpay/sdk-android/blob/master/example/src/main/kotlin/com/example/afterpay/MainActivity.kt#L92-L100
[example-server]: https://github.com/afterpay/sdk-example-server
[ktlint]: https://ktlint.github.io
[license]: LICENSE
[network-config]: https://developer.android.com/training/articles/security-config#CertificatePinning
[proguard-rules]: afterpay/consumer-rules.pro

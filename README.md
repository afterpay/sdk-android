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
    - [Configurating the SDK](#configuring-the-sdk)
    - [Launching the Checkout](launching-the-checkout)
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
    implementation 'com.afterpay:afterpay-android:1.0.2'
}
```

### ProGuard

If you are using R8 the shrinking and obfuscation rules are included automatically.

Proguard users will need to manually apply the rules defined in [`consumer-rules.pro`][proguard-rules].

## Features

The initial release of the SDK contains the web login and checkout process with more features to come in subsequent releases.

## Getting Started

### Configuring the SDK

Each merchant has configuration specific to their account which is accessible from the `/configuration` API endpoint. This configuration is used by the SDK for rendering UI components and is applied globally using the [`Afterpay.setConfiguration`][docs-configuration] method.

The following sample demonstrates how the SDK can be configured using the data supplied by the Afterpay API.

```kotlin
val configuration = api.getConfiguration()

Afterpay.setConfiguration(
    minimumAmount = configuration.minimum?.amount,
    maximumAmount = configuration.maximum.amount,
    currency = configuration.maximum.currency
)
```

> **NOTE:** The merchant account is subject to change and it is recommended to update this configuration **once per day**. The example project provides a [reference][example-configuration] demonstrating how this may be implemented.

### Launching the Checkout

Launch the Afterpay payment flow by starting the intent provided by the SDK for a given checkout URL.

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
                val token = Afterpay.parseCheckoutResponse(data!!)
                Toast.makeText(this, "Success: Completed order $token", Toast.LENGTH_SHORT).show()
            }
            CHECKOUT_WITH_AFTERPAY to RESULT_CANCELED -> {
                val status = Afterpay.parseCheckoutCancellationResponse(data!!)
                Toast.makeText(this, "Cancelled: $status", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

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

The [example project][example] demonstrates how to include an Afterpay payment flow using our prebuilt UI components.

## Contributing

Contributions are welcome! Please read our [contributing guidelines][contributing].

## License

This project is licensed under the terms of the Apache 2.0 license. See the [LICENSE][license] file for more information.

<!-- Links: -->
[badge-ci]: https://github.com/afterpay/sdk-android/workflows/Build%20and%20Test/badge.svg?branch=master&event=push
[badge-ktlint]: https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg
[contributing]: CONTRIBUTING.md
[docs-configuration]: https://github.com/afterpay/sdk-android/blob/master/afterpay/src/main/kotlin/com/afterpay/android/Afterpay.kt#L65
[example]: example
[example-configuration]: https://github.com/afterpay/sdk-android/blob/master/example/src/main/kotlin/com/example/afterpay/MainActivity.kt#L92-L100
[ktlint]: https://ktlint.github.io
[license]: LICENSE
[network-config]: https://developer.android.com/training/articles/security-config#CertificatePinning
[proguard-rules]: afterpay/consumer-rules.pro

# Afterpay Android SDK

![Build and Test][badge-ci] [![ktlint][badge-ktlint]][ktlint]

The Afterpay Android SDK makes it quick and easy to provide an excellent payment experience in your Android app. We provide powerful and customizable UI elements that can be used out-of-the-box to allow your users to shop now and pay later.

## Contents

- [Installation](#installation)
    - [Requirements](#requirements)
    - [Configuration](#configuration)
    - [Proguard](#proguard)
- [Features](#features)
- [Getting Started](#getting-started)
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
  implementation 'com.afterpay:afterpay-android:0.0.1'
}
```

### Proguard

The Afterpay Android SDK will configure your app's Proguard rules using [`proguard-rules.pro`][proguard-rules].

## Features

The initial release of the SDK contains the web login and checkout process with more features to come in subsequent releases.

## Getting Started

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
            val checkoutUrl = merchantServer.checkoutWithAfterpay(cart)
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
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

## Examples

The [example project][example] demonstrates how to include an Afterpay payment flow using our prebuilt UI components.

## Contributing

Contributions are welcome! Please read our [contributing guidelines][contributing].

## License

This project is licensed under the terms of the Apache 2.0 license. See the [LICENSE][license] file for more information.

<!-- Links: -->
[badge-ci]: https://github.com/AfterpayTouch/afterpay-android/workflows/Build%20and%20Test/badge.svg?branch=master&event=push
[badge-ktlint]: https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg
[contributing]: CONTRIBUTING.md
[example]: example
[ktlint]: https://ktlint.github.io
[license]: LICENSE
[proguard-rules]: afterpay/proguard-rules.pro

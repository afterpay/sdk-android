# Afterpay Android SDK

![Build and Test](https://github.com/ittybittyapps/afterpay-android/workflows/Build%20and%20Test/badge.svg?branch=master&event=push)

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
  implementation 'com.afterpay:afterpay-android:1.0.0'
}
```

### Proguard

The Afterpay Android SDK will configure your app's Proguard rules using `proguard-rules.txt`.

## Features

## Getting Started

Include the Afterpay payment button in you layout.

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.afterpay.android.view.PayWithAfterpay
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:id="@+id/button_afterpay" />

</FrameLayout>
```

Launch the Afterpay payment flow from your activity.

```kotlin
class ExampleActivity: Activity {
    companion object {
        private const val PAY_WITH_AFTERPAY = 1
    }

    override fun onCreate(savedInstanceState: Bundle) {
        // ...

        val payWithAfterpay = findViewById<PayWithAfterpay>(R.id.button_afterpay)
        payWithAfterpay.totalPrice = cart.totalPrice
        payWithAfterpay.setOnClickListener {
            val checkoutUrl = merchantServer.checkoutWithAfterpay(cart)
            val intent = Intent(this@ExampleActivity, ExampleActivity::class.java).apply {
                putExtra(AfterpayIntent.CHECKOUT_URL, checkoutUrl)
            }
            startActivityForResult(intent, PAY_WITH_AFTERPAY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // ...

        if (requestCode == PAY_WITH_AFTERPAY) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val token = data?.getParcelableExtra(AfterpayIntent.CHECKOUT_TOKEN)
                    merchantServer.purchaseCompleted(token)
                }
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(this@ExampleActivity, "Cancelled", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }
}
```

## Examples

The [example project](./example) demonstrates how to include an Afterpay payment flow using our prebuilt UI components.

## Contributing

Contributions are welcome! Please read our [contributing guidelines](./CONTRIBUTING.md).

## License

This project is licensed under the terms of the Apache 2.0 license. See the [LICENSE](LICENSE) file for more information.

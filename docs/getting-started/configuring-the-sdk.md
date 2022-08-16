---
layout: default
title: Configuring the SDK
parent: Getting Started
nav_order: 1
---

# Configuring the SDK

Each merchant has configuration specific to their account which is accessible from the `/configuration` API endpoint. This configuration is used by the SDK for rendering UI components with the correct branding and assets, T&Cs, web links, and currency formatting, and is applied globally using the [`Afterpay.setConfiguration`][docs-configuration]{:target="_blank"} method.

The following sample demonstrates how the SDK can be configured using the data supplied by the Afterpay API. It is up to you to decide how best to supply the locale.

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

{: .note }
> - The configuration must always be set when using the SDK, and before any included components are initialised.
> - The merchant account is subject to change and it is recommended to update this configuration **once per day**. The example project provides a [reference][example-configuration]{:target="_blank"} demonstrating how this may be implemented.
> - Configuring the SDK with a UK locale will display Clearpay assets and branding, T&Cs, and currency formatting.


[example-configuration]: https://github.com/afterpay/sdk-android/blob/master/example/src/main/kotlin/com/example/afterpay/MainActivity.kt#L108-L114
[docs-configuration]: https://github.com/afterpay/sdk-android/blob/master/afterpay/src/main/kotlin/com/afterpay/android/Afterpay.kt#L65

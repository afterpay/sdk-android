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
    - [Launching the Checkout (v1)](#launching-the-checkout-v1)
    - [Launching the Checkout (v2)](#launching-the-checkout-v2)
- [UI Components](#ui-components)
    - [Widget](#widget)
    - [Badge](#badge)
    - [Payment Buttons](#payment-buttons)
    - [Price Breakdown](#price-breakdown)
- [Security](#security)
- [Examples](#examples)
- [Contributing](#contributing)
- [License](#license)

## Installation

### Requirements

- Android 7.0 (API level 24) and above

### Configuration

Add `afterpay-android` to your `build.gradle` dependencies.

```gradle
dependencies {
    implementation 'com.afterpay:afterpay-android:3.3.1'
}
```

### ProGuard

If you are using R8 the shrinking and obfuscation rules are included automatically.

Proguard users will need to manually apply the rules defined in [`consumer-rules.pro`][proguard-rules].

## Features

The SDK provides easy integration of the Afterpay web login and checkout process as well as ready-made UI components. There are two versions of checkout; v1 supports the standard flow while v2 adds support for the richer express experience, the callbacks for which are handled by the provided handler object.

## Getting Started

### Configuring the SDK

Each merchant has configuration specific to their account which is accessible from the `/configuration` API endpoint. This configuration is used by the SDK for rendering UI components with the correct branding and assets, T&Cs, web links, and currency formatting, and is applied globally using the [`Afterpay.setConfiguration`][docs-configuration] method.

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

> **NOTES:**
> - The configuration must always be set when using the SDK, and before any included components are initialised.
> - The merchant account is subject to change and it is recommended to update this configuration **once per day**. The example project provides a [reference][example-configuration] demonstrating how this may be implemented.
> - Configuring the SDK with a UK locale will display Clearpay assets and branding, T&Cs, and currency formatting.

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

Launch the Afterpay checkout v2 flow by starting the intent provided by the SDK for the given options.

> When creating a checkout token, `popupOriginUrl` must be set to `https://static.afterpay.com`. The SDK’s example merchant server sets the parameter [here](https://github.com/afterpay/sdk-example-server/blob/master/src/routes/checkout.ts#L28). See the [API reference][express-checkout] for more details! Failing to do so will cause undefined behavior.

For more information on express checkout, including the available options and callbacks, please check the [API reference][express-checkout].

```kotlin
class ExampleActivity: Activity {
    private companion object {
        const val CHECKOUT_WITH_AFTERPAY = 1234
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // ...

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

            override fun shippingOptionDidChange(
                shippingOption: ShippingOption,
                onProvideShippingOptionUpdate: (ShippingOptionUpdateResult?) -> Unit
            ) {
                TODO("Optionally update your application model with the selected shipping option")
            }
        })

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

The checkout widget displays the consumer's payment schedule, and can be updated as the order total changes. It should be shown if the order value is going to change after the Afterpay Express checkout has finished. For example, the order total may change in response to shipping costs and promo codes. It can also be used to show if there are any barriers to completing the purchase, like if the customer has gone over their Afterpay payment limit.

The widget can be added to a layout or instantiated in code but an instance must always be initialised in one of the two ways demonstrated below and provided with the required callbacks which will notify your app when the widget is updated or an error occurs, or when an attempt to load an external URL is made.

The widget will resize to fit the internal content and should not be made smaller so as to maintain legibility.

![Payment Schedule Widget][widget]

### Adding the Widget

Initialising the widget with a token received upon completion of checkout v2 will populate it with information about the transaction.

```kotlin
class ReceiptFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // ...

        view.findViewById<AfterpayWidgetView>(R.id.afterpay_widget)
            .init(token, ::onWidgetExternalLink, ::onWidgetUpdate, ::onWidgetError)
    }

    private fun onWidgetExternalLink(url: Uri) {
        TODO("Launch external browser")
    }

    private fun onWidgetUpdate(dueToday: Money, checksum: String?) {
        Log.d("ReceiptFragment", "$dueToday, checksum: $checksum")
    }

    private fun onWidgetError(error: String) {
        Log.e("ReceiptFragment", error)
    }
}
```

Alternatively, if checkout has not been completed or your app is not using checkout v2, the widget may be initialised in tokenless mode with a `BigDecimal` representing the total cost of the purchase.
```kotlin
view.findViewById<AfterpayWidgetView>(R.id.afterpay_widget)
    .init("50.00".toBigDecimal(), ::onWidgetExternalLink, ::onWidgetUpdate, ::onWidgetError)
```

### Styling the Widget

By default the widget will show the Afterpay logo and header but these may be independently disabled when initialising the widget.

```kotlin
view.findViewById<AfterpayWidgetView>(R.id.afterpay_widget)
    .init(
        "50.00".toBigDecimal(),
        ::onWidgetExternalLink,
        ::onWidgetUpdate,
        ::onWidgetError,
        showLogo = true,
        showHeading = false
    )
```

### Updating the Widget

The widget can be updated to reflect changes to the order total caused by promo codes, shipping options, etc.

```kotlin
widget.update("50.00".toBigDecimal())
```

### Badge

The Afterpay badge can be added to your layout and scaled to suit the needs of your app. Per branding guidelines it requires a minimum width of `64dp`.

![Black on Mint badge][badge-black-on-mint]
![Mint on Black badge][badge-mint-on-black]

![Black on White badge][badge-black-on-white]
![White on Black badge][badge-white-on-black]

### Lockup

The Afterpay lockup can be added to your layout and scaled to suit the needs of your app. Per branding guidelines it requires a minimum width of `64dp`.

![Black Lockup][lockup-black]
![White Lockup][lockup-white]
![Mint Lockup][lockup-mint]

**Attributes**
```xml
app:afterpayColorScheme="blackOnMint|mintOnBlack|blackOnWhite|whiteOnBlack"
```

### Payment Buttons

The payment button may be added to your layout and scaled to suit the needs of your app but to maintain legibility the width must not exceed `256dp`.

![Black on Mint pay now button][button-black-on-mint]
![Mint on Black buy now button][button-mint-on-black]

![Black on White checkout with button][button-black-on-white]
![White on Black place order with button][button-white-on-black]

The button may be styled with different text to suit some common purchasing scenarios:
- Pay Now
- Buy Now
- Checkout with
- Place order with

**Attributes**
```xml
app:afterpayColorScheme="blackOnMint|mintOnBlack|blackOnWhite|whiteOnBlack"
app:afterpayButtonText="payNow|buyNow|checkout|placeOrder"
```

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

#### Configuring the Price Breakdown

##### Intro Text
Setting `introText` is optional, will default to `OR` and must be of type `AfterpayIntroText`.

Can be any of `OR`, `OR_TITLE`, `MAKE`, `MAKE_TITLE`, `PAY`, `PAY_TITLE`, `IN`, `IN_TITLE`, `PAY_IN`, `PAY_IN_TITLE` or `EMPTY` (no intro text).
Intro text will be rendered lowercase unless using an option suffixed with `_TITLE` in which case title case will be rendered.

##### Logo Type
Setting `logoType` is optional, will default to `BADGE` and must be of type `AfterpayLogoType`.

Can be either of `BADGE` or `LOCKUP`.
When setting color scheme on logo type of `LOCKUP`, only the foreground color will be applied. (See example)

```kotlin
val afterpayBreakdown = view.findViewById<AfterpayPriceBreakdown>(R.id.afterpayPriceBreakdown)
afterpayBreakdown.logoType = AfterpayLogoType.LOCKUP
afterpayBreakdown.colorScheme = AfterpayColorScheme.MINT_ON_BLACK
```

Given the above, the price breakdown will contain the lockup logo and will be of color mint.

##### Optional Words
Setting `showInterestFreeText` and / or `showWithText` is optional and is of type `Boolean`.

Both default to true. This will show the text `pay in 4 interest-free payents of $#.##`.
Setting `showInterestFreeText` to false will remove "interest-free" from the sentence.
Setting `showWithText` to false will remove the word "with" from the sentence.

```kotlin
val afterpayBreakdown = view.findViewById<AfterpayPriceBreakdown>(R.id.afterpayPriceBreakdown)
afterpayBreakdown.introText = AfterpayIntroText.MAKE_TITLE
```

Given the above, the price breakdown text will be rendered `Make 4 interest-free payments of $##.##`

##### More Info Options
Setting `moreInfoOptions` is optional and of type `AfterpayMoreInfoOptions`. This class has two constructors.
The first constructor takes two parameters:
- `modalId`: a `string` that is the filename of a modal hosted on Afterpay static.
- `modalLinkStyle`: an optional value of type `ModalLinkStyle`. See [Modal Link Style Options](#modal-link-style-options) for more details.

The second constructor takes three parameters:
- `modalTheme`: an enum of type `AfterpayModalTheme` with the following options: `MINT` (default) and `WHITE`.
- `isCbtEnabled`: a `boolean` to indicate if the modal should show the Cross Border Trade details in the modal
- `modalLinkStyle`: an optional value of type `ModalLinkStyle`. See [Modal Link Style Options](#modal-link-style-options) for more details.

**Note**
Not all combinations of Locales and CBT are available.

```kotlin
val afterpayBreakdown = view.findViewById<AfterpayPriceBreakdown>(R.id.afterpayPriceBreakdown)
afterpayBreakdown.moreInfoOptions = AfterpayMoreInfoOptions(
    modalTheme = AfterpayModalTheme.WHITE
)
```

Given the above, when clicking the more info "link", the modal that opens will be white in the current locale as set in configuration.

###### Modal Link Style Options
A value that can be set on `moreInfoOptions` when initialised. Setting this is optional and is of type `ModalLinkStyle`.

Available values are `CircledInfoIcon`, `MoreInfoText`, `LearnMoreText`, `CircledQuestionIcon`, `CircledLogo`, `Custom`, `None`.
`CircledInfoIcon` is the default & `None` will remove the link altogether.

When using `Custom` the `setContent` (takes a single parameter of type `SpannableStringBuilder`) method should be called first (see second example below).

```kotlin
val afterpayBreakdown = view.findViewById<AfterpayPriceBreakdown>(R.id.afterpayPriceBreakdown)
afterpayBreakdown.moreInfoOptions = AfterpayMoreInfoOptions(
    modalLinkStyle  = AfterpayModalLinkStyle.CircledInfoIcon
)
```

Given the above, the price breakdown modal link will be a circle containing a question mark.

```kotlin
val afterpayBreakdown = view.findViewById<AfterpayPriceBreakdown>(R.id.afterpayPriceBreakdown)
val content = SpannableStringBuilder().apply {
    append("Click ")
    append("Here ", StyleSpan(Typeface.BOLD), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
}
AfterpayModalLinkStyle.Custom.setContent(content)
afterpayBreakdown.moreInfoOptions = AfterpayMoreInfoOptions(
    modalLinkStyle  = AfterpayModalLinkStyle.Custom
)
```

Given the above, the price breakdown modal link will display "Click Here".

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
[widget]: images/widget.png
[badge-black-on-mint]: images/badge_black_on_mint.png
[badge-mint-on-black]: images/badge_mint_on_black.png
[badge-black-on-white]: images/badge_black_on_white.png
[badge-white-on-black]: images/badge_white_on_black.png
[lockup-black]: images/lockup_black.png
[lockup-white]: images/lockup_white.png
[lockup-mint]: images/lockup_mint.png
[breakdown-available]: images/price_breakdown_available.png
[breakdown-no-configuration]: images/price_breakdown_no_configuration.png
[breakdown-unavailable-min-max]: images/price_breakdown_unavailable_min_max.png
[breakdown-unavailable-max]: images/price_breakdown_unavailable_max.png
[button-black-on-mint]: images/button_black_on_mint.png
[button-mint-on-black]: images/button_mint_on_black.png
[button-black-on-white]: images/button_black_on_white.png
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

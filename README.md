# Afterpay Android SDK

![Build and Test][badge-ci] [![ktlint][badge-ktlint]][ktlint]

The Afterpay Android SDK makes it quick and easy to provide an excellent payment experience in your Android app. We provide powerful and customizable UI elements that can be used out-of-the-box to allow your users to shop now and pay later.

## Documentation
Documentation for usage can be found [here][docs], including the [getting started][docs-getting-started] guide and [UI component][docs-ui] docs.

## V3 Documentation

### Launching the Checkout

Launch the Afterpay checkout v3 flow by starting the intent provided by the SDK for the given options.

The activity result returns a `CheckoutV3Data` with the information required to complete the merchant transaction, as well as the tokens required by `Afterpay.updateMerchantReferenceV3|Async`.


```kotlin
class ExampleActivity: Activity {
    private companion object {
        const val CHECKOUT_WITH_AFTERPAY = 1234
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // ...

        val afterpayCheckoutButton = findViewById<Button>(R.id.button_afterpay)
        afterpayCheckoutButton.setOnClickListener {
            val options = Afterpay.createCheckoutV3Intent(
                requireContext(),
                consumer = // `CheckoutV3Consumer` interface
                orderTotal = OrderTotal(
                    total = BigDecimal.ZERO,
                    shipping = BigDecimal.ZERO,
                    tax = BigDecimal.ZERO
                )
                items = // Optional `List<CheckoutV3Item>`
                buyNow = command.buyNow // Changes checkout button title from 'Confirm' to 'Buy Now'
            )
            startActivityForResult(intent, CHECKOUT_WITH_AFTERPAY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // ...

        when (requestCode to resultCode) {
            CHECKOUT_WITH_AFTERPAY to AppCompatActivity.RESULT_OK -> {
                val intent = checkNotNull(data) {
                    "Intent should always be populated by the SDK"
                }
                // `CheckoutV3Data` containing values required
                val resultData = checkNotNull(Afterpay.parseCheckoutSuccessResponseV3(intent)) {
                    "Result data is always associated with a successful V3 Afterpay transaction"
                }
                findNavController().navigate(
                    nav_graph.action.to_details_v3,
                    bundleOf(nav_graph.args.result_data_v3 to resultData)
                )
            }
            CHECKOUT_WITH_AFTERPAY to AppCompatActivity.RESULT_CANCELED -> {
                val intent = requireNotNull(data) {
                    "Intent should always be populated by the SDK"
                }
                val pair = checkNotNull(Afterpay.parseCheckoutCancellationResponseV3(intent)) {
                    "A cancelled Afterpay transaction always contains a status, and optionally an `Exception`"
                }
                TODO("Notify user of checkout cancellation")
            }
        }
    }
}
```

### Configuration

V3 users can retrieve the confirmation object directly from Afterpay by using `Afterpay.fetchMerchantConfigurationV3|Async()`

## Contributing

Contributions are welcome! Please read our [contributing guidelines][contributing].

## License

This project is licensed under the terms of the Apache 2.0 license. See the [LICENSE][license] file for more information.

<!-- Links: -->
[badge-ci]: https://github.com/afterpay/sdk-android/workflows/Build%20and%20Test/badge.svg?branch=master&event=push
[badge-ktlint]: https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg
[ktlint]: https://ktlint.github.io
[contributing]: CONTRIBUTING.md
[license]: LICENSE
[docs]: https://afterpay.github.io/sdk-android
[docs-ui]: https://afterpay.github.io/sdk-android/ui-components/
[docs-getting-started]: https://afterpay.github.io/sdk-android/getting-started/

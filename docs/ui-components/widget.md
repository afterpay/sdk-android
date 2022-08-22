---
layout: default
title: Widget
parent: UI Components
nav_order: 1
---

# Widget
{: .no_toc }

<details open markdown="block">
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>

The checkout widget displays the consumer's payment schedule, and can be updated as the order total changes. It should be shown if the order value is going to change after the Afterpay Express checkout has finished. For example, the order total may change in response to shipping costs and promo codes. It can also be used to show if there are any barriers to completing the purchase, like if the consumer has exceeded their Afterpay payment limit.

The widget can be added to a layout or instantiated in code, but an instance must always be initialized in one of the two ways demonstrated below and provided with the required callbacks. These callbacks will notify your app when the widget is updated or an error occurs, or when an attempt to load an external URL is made.

The widget will resize to fit the internal content and should not be made smaller so as to maintain legibility.

![Payment Schedule Widget][widget]


## Adding the Widget

Initializing the widget with a token received upon completion of checkout V2 will populate it with information about the transaction.

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

Alternatively, if checkout has not been completed or your app is not using checkout V2, the widget may be initialized in tokenless mode with a `BigDecimal` representing the total cost of the purchase.
```kotlin
view.findViewById<AfterpayWidgetView>(R.id.afterpay_widget)
    .init("50.00".toBigDecimal(), ::onWidgetExternalLink, ::onWidgetUpdate, ::onWidgetError)
```

## Styling the Widget

By default the widget will show the Afterpay logo and header but these may be independently disabled when initializing the widget.

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

## Updating the Widget

The widget can be updated to reflect changes to the order total caused by promo codes, shipping options, etc.

```kotlin
widget.update("50.00".toBigDecimal())
```

[widget]: ../images/widget.png

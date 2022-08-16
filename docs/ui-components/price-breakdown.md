---
layout: default
title: Price Breakdown
parent: UI Components
nav_order: 4
---

# Price Breakdown
{: .no_toc }

<details open markdown="block">
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>


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

## Configuring the Price Breakdown
{: .no_toc }

### Intro Text
Setting `introText` is optional, will default to `OR` and must be of type `AfterpayIntroText`.

Can be any of `OR`, `OR_TITLE`, `MAKE`, `MAKE_TITLE`, `PAY`, `PAY_TITLE`, `IN`, `IN_TITLE`, `PAY_IN`, `PAY_IN_TITLE` or `EMPTY` (no intro text).
Intro text will be rendered lowercase unless using an option suffixed with `_TITLE` in which case title case will be rendered.

### Logo Type
Setting `logoType` is optional, will default to `BADGE` and must be of type `AfterpayLogoType`.

Can be either of `BADGE` or `LOCKUP`.
When setting color scheme on logo type of `LOCKUP`, only the foreground color will be applied. (See example)

```kotlin
val afterpayBreakdown = view.findViewById<AfterpayPriceBreakdown>(R.id.afterpayPriceBreakdown)
afterpayBreakdown.logoType = AfterpayLogoType.LOCKUP
afterpayBreakdown.colorScheme = AfterpayColorScheme.MINT_ON_BLACK
```

Given the above, the price breakdown will contain the lockup logo and will be of color mint.

### Optional Words
Setting `showInterestFreeText` and / or `showWithText` is optional and is of type `Boolean`.

Both default to true. This will show the text `pay in 4 interest-free payents of $#.##`.
Setting `showInterestFreeText` to false will remove "interest-free" from the sentence.
Setting `showWithText` to false will remove the word "with" from the sentence.

```kotlin
val afterpayBreakdown = view.findViewById<AfterpayPriceBreakdown>(R.id.afterpayPriceBreakdown)
afterpayBreakdown.introText = AfterpayIntroText.MAKE_TITLE
```

Given the above, the price breakdown text will be rendered `Make 4 interest-free payments of $##.##`

### More Info Options
Setting `moreInfoOptions` is optional and of type `AfterpayMoreInfoOptions`. This class has two constructors.
The first constructor takes two parameters:
- `modalId`: a `string` that is the filename of a modal hosted on Afterpay static.
- `modalLinkStyle`: an optional value of type `ModalLinkStyle`. See [Modal Link Style Options](#modal-link-style-options) for more details.

The second constructor takes three parameters:
- `modalTheme`: an enum of type `AfterpayModalTheme` with the following options: `MINT` (default) and `WHITE`.
- `isCbtEnabled`: a `boolean` to indicate if the modal should show the Cross Border Trade details in the modal
- `modalLinkStyle`: an optional value of type `ModalLinkStyle`. See [Modal Link Style Options](#modal-link-style-options) for more details.

{: .note }
Not all combinations of Locales and CBT are available.

```kotlin
val afterpayBreakdown = view.findViewById<AfterpayPriceBreakdown>(R.id.afterpayPriceBreakdown)
afterpayBreakdown.moreInfoOptions = AfterpayMoreInfoOptions(
    modalTheme = AfterpayModalTheme.WHITE
)
```

Given the above, when clicking the more info "link", the modal that opens will be white in the current locale as set in configuration.

### Modal Link Style Options
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

[breakdown-available]: ../images/price_breakdown_available.png
[breakdown-no-configuration]: ../images/price_breakdown_no_configuration.png
[breakdown-unavailable-min-max]: ../images/price_breakdown_unavailable_min_max.png
[breakdown-unavailable-max]: ../images/price_breakdown_unavailable_max.png

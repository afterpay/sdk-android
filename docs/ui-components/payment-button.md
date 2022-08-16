---
layout: default
title: Payment Button
parent: UI Components
nav_order: 3
---

# Payment Button
{: .no_toc }

<details open markdown="block">
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>

The payment button may be added to your layout and scaled to suit the needs of your app but to maintain legibility the width must not exceed `256dp`.

![Black on Mint pay now button][button-black-on-mint]
![Mint on Black buy now button][button-mint-on-black]

![Black on White checkout with button][button-black-on-white]
![White on Black place order with button][button-white-on-black]

## Button Text
The button may be styled with different text to suit some common purchasing scenarios:
- Pay Now
- Buy Now
- Checkout with
- Place order with

## Attributes
```xml
app:afterpayColorScheme="blackOnMint|mintOnBlack|blackOnWhite|whiteOnBlack"
app:afterpayButtonText="payNow|buyNow|checkout|placeOrder"
```

[button-black-on-mint]: ../images/button_black_on_mint.png
[button-mint-on-black]: ../images/button_mint_on_black.png
[button-black-on-white]: ../images/button_black_on_white.png
[button-white-on-black]: ../images/button_white_on_black.png

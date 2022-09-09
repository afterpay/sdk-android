---
layout: default
title: Getting Started
has_children: true
nav_order: 3
---

# Getting Started

Getting started with the SDK involves configuring the SDK with the response from the `/configuration` API endpoint as well as selecting and implementing a checkout method (V1 or V2).

[Checkout V1][checkout-v1] requires a url to be generated using the checkout API and using it to start the intent provided by the SDK. [Checkout V2][checkout-v2] requires setting options of type `AfterpayCheckoutV2Options` and creating handler methods for user interactions.

[checkout-v1]: checkout-v1
[checkout-v2]: checkout-v2

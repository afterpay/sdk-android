package com.example

import com.afterpay.android.model.CheckoutV3Consumer
import com.afterpay.android.model.CheckoutV3Contact
import com.afterpay.android.model.CheckoutV3Item
import com.afterpay.android.model.OrderTotal
import java.math.BigDecimal
import java.net.URL

/**
 * Functions to generate sample checkout data: customer, cart, items, etc...
 */

internal fun createItems(): Array<CheckoutV3Item> {
    return listOf(
        createCheckoutItem(),
    ).toTypedArray()
}

internal fun createOrderTotal(): OrderTotal {
    return OrderTotal(
        total = BigDecimal(10.00),
        shipping = BigDecimal(1.00),
        tax = BigDecimal(2.34),
    )
}

internal fun createCheckoutItem(): CheckoutV3Item {
    return object : CheckoutV3Item {
        override val categories: List<List<String>>?
            get() = emptyList()
        override val estimatedShipmentDate: String?
            get() = null
        override val imageUrl: URL?
            get() = null
        override val name: String
            get() = "a thing"
        override val pageUrl: URL?
            get() = null
        override val price: BigDecimal
            get() = BigDecimal(10.00)
        override val quantity: UInt
            get() = 1.toUInt()
        override val sku: String?
            get() = null
    }
}

internal fun createConsumer(): CheckoutV3Consumer {
    return object : CheckoutV3Consumer {
        override val billingInformation: CheckoutV3Contact?
            get() = createBillingInfo()
        override val email: String
            get() = customerEmail
        override val givenNames: String?
            get() = "Bob"
        override val phoneNumber: String?
            get() = "4041234567"
        override val shippingInformation: CheckoutV3Contact?
            get() = createShippingInfo()
        override val surname: String?
            get() = "Smith"
    }
}

internal fun createBillingInfo(): CheckoutV3Contact? {
    return object : CheckoutV3Contact {
        override var area1: String?
            get() = null
            set(value) {}
        override var area2: String?
            get() = null
            set(value) {}
        override var countryCode: String
            get() = "US"
            set(value) {}
        override var line1: String
            get() = "123 Main Street"
            set(value) {}
        override var line2: String?
            get() = null
            set(value) {}
        override var name: String
            get() = "Bob"
            set(value) {}
        override var phoneNumber: String?
            get() = null
            set(value) {}
        override var postcode: String?
            get() = "post code"
            set(value) {}
        override var region: String?
            get() = null
            set(value) {}
    }
}

internal fun createShippingInfo(): CheckoutV3Contact? {
    return object : CheckoutV3Contact {
        override var area1: String?
            get() = null
            set(value) {}
        override var area2: String?
            get() = null
            set(value) {}
        override var countryCode: String
            get() = "US"
            set(value) {}
        override var line1: String
            get() = "123 Main Street"
            set(value) {}
        override var line2: String?
            get() = null
            set(value) {}
        override var name: String
            get() = "Bob"
            set(value) {}
        override var phoneNumber: String?
            get() = null
            set(value) {}
        override var postcode: String?
            get() = "post code"
            set(value) {}
        override var region: String?
            get() = null
            set(value) {}
    }
}

val customerEmail = "example@squareup.com"

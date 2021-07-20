package com.afterpay.android.model

/**
 * A minimal implementation of [CheckoutV3Consumer]
 */
data class Consumer(
    override var email: String,
    override var givenNames: String? = null,
    override var surname: String? = null,
    override var phoneNumber: String? = null,
    override var shippingInformation: CheckoutV3Contact? = null,
    override var billingInformation: CheckoutV3Contact? = null
) : CheckoutV3Consumer {

}

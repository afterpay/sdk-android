package com.afterpay.android.model

interface CheckoutV3Consumer {
    /** The consumer’s email address. Limited to 128 characters. **/
    val email: String

    /** The consumer’s first name and any middle names. Limited to 128 characters. **/
    val givenNames: String?

    /** The consumer’s last name. Limited to 128 characters. **/
    val surname: String?

    /** The consumer’s phone number. Limited to 32 characters. **/
    val phoneNumber: String?

    /** The consumer's shipping information. **/
    val shippingInformation: CheckoutV3Contact?

    /** The consumer's billing information. **/
    val billingInformation: CheckoutV3Contact?
}

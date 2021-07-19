package com.afterpay.android.model

interface CheckoutV3Consumer {
    /** The consumer’s email address. Limited to 128 characters. **/
    var email: String
    /** The consumer’s first name and any middle names. Limited to 128 characters. **/
    var givenNames: String?
    /** The consumer’s last name. Limited to 128 characters. **/
    var surname: String?
    /** The consumer’s phone number. Limited to 32 characters. **/
    var phoneNumber: String?
    /** The consumer's shipping information. **/
    var shippingInformation: CheckoutV3Contact?
    /** The consumer's billing information. **/
    var billingInformation: CheckoutV3Contact?
}

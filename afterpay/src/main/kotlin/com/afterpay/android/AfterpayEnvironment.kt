package com.afterpay.android

import java.net.URL
import java.util.Locale

enum class AfterpayEnvironment(
    val payKitId: String,
    val cashAppPaymentSigningUrl: URL,
    val cashAppPaymentValidationUrl: URL,
) {
    SANDBOX(
        payKitId = "CAS-CI_AFTERPAY",
        cashAppPaymentSigningUrl = URL("https://api-plus.us-sandbox.afterpay.com/v2/payments/sign-payment"),
        cashAppPaymentValidationUrl = URL("https://api-plus.us-sandbox.afterpay.com/v2/payments/validate-payment"),
    ),

    PRODUCTION(
        payKitId = "CA-CI_AFTERPAY",
        cashAppPaymentSigningUrl = URL("https://api-plus.us.afterpay.com/v2/payments/sign-payment"),
        cashAppPaymentValidationUrl = URL("https://api-plus.us.afterpay.com/v2/payments/validate-payment"),
    ),
    ;

    override fun toString(): String = name.lowercase(Locale.ROOT)
}

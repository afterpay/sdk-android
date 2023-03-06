package com.afterpay.android

import java.net.URL
import java.util.Locale

const val API_PLUS_SANDBOX_BASE_URL = "https://api-plus.us-sandbox.afterpay.com"
const val API_PLUS_PRODUCTION_BASE_URL = "https://api-plus.us.afterpay.com"

enum class AfterpayEnvironment(
    val payKitId: String,
    val cashAppPaymentSigningUrl: URL,
    val cashAppPaymentValidationUrl: URL,
) {
    SANDBOX(
        payKitId = "CAS-CI_AFTERPAY",
        cashAppPaymentSigningUrl = URL("$API_PLUS_SANDBOX_BASE_URL/v2/payments/sign-payment"),
        cashAppPaymentValidationUrl = URL("$API_PLUS_SANDBOX_BASE_URL/v2/payments/validate-payment"),
    ),

    PRODUCTION(
        payKitId = "CA-CI_AFTERPAY",
        cashAppPaymentSigningUrl = URL("$API_PLUS_PRODUCTION_BASE_URL/v2/payments/sign-payment"),
        cashAppPaymentValidationUrl = URL("$API_PLUS_PRODUCTION_BASE_URL/v2/payments/validate-payment"),
    ),
    ;

    override fun toString(): String = name.lowercase(Locale.ROOT)
}

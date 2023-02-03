package com.afterpay.android

import java.util.Locale

enum class AfterpayEnvironment(
    val payKitId: String,
    val payKitSigningUrl: String,
) {
    SANDBOX(
        payKitId = "CAS-CI_AFTERPAY",
        payKitSigningUrl = "https://api-plus.us-sandbox.afterpay.com/v2/payments/sign-payment"
    ),

    PRODUCTION(
        payKitId = "CA-CI_AFTERPAY",
        payKitSigningUrl = "https://api-plus.us.afterpay.com/v2/payments/sign-payment"
    )
    ;

    override fun toString(): String = name.lowercase(Locale.ROOT)
}

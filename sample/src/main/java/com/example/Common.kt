package com.example

import com.afterpay.android.AfterpayEnvironment
import com.afterpay.android.model.AfterpayRegion
import com.afterpay.android.model.CheckoutV3Configuration

val AFTERPAY_REGION = AfterpayRegion.US
val AFTERPAY_ENVIRONMENT = AfterpayEnvironment.SANDBOX

/**
 * Here we use Gradle Secrets Plugin to avoid commiting merchant IDs to
 * repository. This is not required for SDK to work
 *
 * https://github.com/google/secrets-gradle-plugin
 *
 * To follow this usage add
 *
 *      merchantId= "xxxx"
 *
 * to local.properties
 */
const val MERCHANT_ID = BuildConfig.merchantId

fun createCheckoutV3Configuration(): CheckoutV3Configuration {
    return CheckoutV3Configuration(
        shopDirectoryMerchantId = MERCHANT_ID,
        region = AFTERPAY_REGION,
        environment = AFTERPAY_ENVIRONMENT,
    )
}

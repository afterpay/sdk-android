/*
 * Copyright (C) 2024 Afterpay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

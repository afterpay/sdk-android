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
package com.afterpay.android

import java.net.URL
import java.util.Locale

const val API_PLUS_SANDBOX_BASE_URL = "https://api-plus.us-sandbox.afterpay.com"
const val API_PLUS_PRODUCTION_BASE_URL = "https://api-plus.us.afterpay.com"

enum class AfterpayEnvironment(
  val payKitClientId: String,
  val cashAppPaymentSigningUrl: URL,
  val cashAppPaymentValidationUrl: URL,
) {
  SANDBOX(
    payKitClientId = "CAS-CI_AFTERPAY",
    cashAppPaymentSigningUrl = URL("$API_PLUS_SANDBOX_BASE_URL/v2/payments/sign-payment"),
    cashAppPaymentValidationUrl = URL("$API_PLUS_SANDBOX_BASE_URL/v2/payments/validate-payment"),
  ),

  PRODUCTION(
    payKitClientId = "CA-CI_AFTERPAY",
    cashAppPaymentSigningUrl = URL("$API_PLUS_PRODUCTION_BASE_URL/v2/payments/sign-payment"),
    cashAppPaymentValidationUrl = URL("$API_PLUS_PRODUCTION_BASE_URL/v2/payments/validate-payment"),
  ),
  ;

  override fun toString(): String = name.lowercase(Locale.ROOT)
}

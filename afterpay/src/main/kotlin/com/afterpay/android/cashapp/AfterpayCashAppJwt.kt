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
package com.afterpay.android.cashapp

import android.util.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class AfterpayCashAppSigningResponse(
  var externalBrandId: String,
  var jwtToken: String,
  var redirectUrl: String,
)

@Serializable
data class AfterpayCashAppValidationRequest(
  val jwt: String,
  val externalCustomerId: String,
  val externalGrantId: String,
)

@Serializable
data class AfterpayCashAppValidationResponse(
  var cashAppTag: String,
  var status: String,
  var callbackBaseUrl: String,
)

@Serializable
data class AfterpayCashAppJwt(
  var amount: AfterpayCashAppAmount,
  var token: String,
  var externalMerchantId: String,
  var redirectUrl: String,
) {
  companion object {
    fun decode(jwt: String): Result<AfterpayCashAppJwt> {
      return runCatching {
        val split = jwt.split(".").toTypedArray()
        val jwtBody = getJson(split[1])

        val json = Json { ignoreUnknownKeys = true }
        json.decodeFromString(jwtBody)
      }
    }

    private fun getJson(strEncoded: String): String {
      val decodedBytes: ByteArray = Base64.decode(strEncoded, Base64.URL_SAFE)
      return String(decodedBytes, Charsets.UTF_8)
    }
  }
}

@Serializable
data class AfterpayCashAppAmount(
  var amount: String,
  var currency: String,
  var symbol: String,
)

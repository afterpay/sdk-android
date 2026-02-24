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

import com.afterpay.android.Afterpay
import com.afterpay.android.internal.AfterpayLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

sealed class CashAppSignOrderResult {
  data class Success(val response: AfterpayCashApp) : CashAppSignOrderResult()
  data class Failure(val error: Throwable) : CashAppSignOrderResult()
}

sealed class CashAppValidationResponse {
  data class Success(val response: AfterpayCashAppValidationResponse) : CashAppValidationResponse()
  data class Failure(val error: Throwable) : CashAppValidationResponse()
}

object AfterpayCashAppCheckout {
  suspend fun performSignPaymentRequest(token: String): CashAppSignOrderResult {
    AfterpayLog.d { "Starting Cash App payment signing with token: ${AfterpayLog.sanitizeToken(token)}" }
    runCatching {
      signPayment(token)
        .let { result: Result<AfterpayCashAppSigningResponse> ->
          result.onSuccess { response ->
            AfterpayCashAppJwt.decode(response.jwtToken)
              .onSuccess { jwtBody ->
                val cashApp = AfterpayCashApp(
                  amount = jwtBody.amount.amount.toDouble(),
                  redirectUri = jwtBody.redirectUrl,
                  merchantId = jwtBody.externalMerchantId,
                  brandId = response.externalBrandId,
                  jwt = response.jwtToken,
                )

                AfterpayLog.d("Cash App payment signing successful")
                return CashAppSignOrderResult.Success(cashApp)
              }
              .onFailure { error ->
                AfterpayLog.e(error, "JWT decode failed")
                return CashAppSignOrderResult.Failure(error)
              }
          }
            .onFailure { error ->
              AfterpayLog.e(error, "Payment signing failed")
              return CashAppSignOrderResult.Failure(error)
            }
        }
    }
    // should never happen, compiler doesn't know success and failure are only options
    throw IllegalStateException()
  }

  // TODO stop using this, no need for suspend *and* callback
  suspend fun performSignPaymentRequest(token: String, complete: (CashAppSignOrderResult) -> Unit) {
    AfterpayLog.d { "Starting Cash App payment signing (callback) with token: ${AfterpayLog.sanitizeToken(token)}" }
    runCatching {
      signPayment(token)
        .onSuccess { response ->
          AfterpayCashAppJwt.decode(response.jwtToken)
            .onSuccess { jwtBody ->
              val cashApp = AfterpayCashApp(
                amount = jwtBody.amount.amount.toDouble(),
                redirectUri = jwtBody.redirectUrl,
                merchantId = jwtBody.externalMerchantId,
                brandId = response.externalBrandId,
                jwt = response.jwtToken,
              )

              AfterpayLog.d("Cash App payment signing successful (callback)")
              complete(CashAppSignOrderResult.Success(cashApp))
            }
            .onFailure { error ->
              AfterpayLog.e(error, "JWT decode failed (callback)")
              complete(CashAppSignOrderResult.Failure(error))
            }
        }
        .onFailure { error ->
          AfterpayLog.e(error, "Payment signing failed (callback)")
          complete(CashAppSignOrderResult.Failure(error))
        }
    }
  }

  private suspend fun signPayment(token: String): Result<AfterpayCashAppSigningResponse> {
    return runCatching {
      val url = Afterpay.environment?.cashAppPaymentSigningUrl ?: throw Exception("No signing url found")
      val payload = """{ "token": "$token" }"""

      val response = withContext(Dispatchers.IO) {
        AfterpayCashAppApi.cashRequest<AfterpayCashAppSigningResponse, String>(
          url = url,
          method = AfterpayCashAppApi.CashHttpVerb.POST,
          body = payload,
        )
      }.getOrThrow()

      response
    }
  }

  fun validatePayment(
    jwt: String,
    customerId: String,
    grantId: String,
    complete: (validationResponse: CashAppValidationResponse) -> Unit,
  ) {
    return runBlocking {
      Afterpay.environment?.cashAppPaymentValidationUrl?.let { url ->
        val request = AfterpayCashAppValidationRequest(
          jwt = jwt,
          externalCustomerId = customerId,
          externalGrantId = grantId,
        )

        val payload = Json.encodeToString(request)

        val response = withContext(Dispatchers.IO) {
          AfterpayCashAppApi.cashRequest<AfterpayCashAppValidationResponse, String>(
            url = url,
            method = AfterpayCashAppApi.CashHttpVerb.POST,
            body = payload,
          )
        }

        response
          .onSuccess {
            when (it.status) {
              "SUCCESS" -> {
                AfterpayLog.d("Cash App validation successful")
                complete(CashAppValidationResponse.Success(it))
              }
              else -> {
                AfterpayLog.w { "Cash App validation failed with status: ${it.status}" }
                complete(CashAppValidationResponse.Failure(Exception("status is ${it.status}")))
              }
            }
          }
          .onFailure { error ->
            AfterpayLog.e(error, "Cash App validation failed")
            complete(CashAppValidationResponse.Failure(Exception(error.message)))
          }

        Unit
      }
    } ?: complete(CashAppValidationResponse.Failure(Exception("environment not set")))
  }
}

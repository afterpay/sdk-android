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

                return CashAppSignOrderResult.Success(cashApp)
              }
              .onFailure {
                return CashAppSignOrderResult.Failure(it)
              }
          }
            .onFailure {
              return CashAppSignOrderResult.Failure(it)
            }
        }
    }
    // should never happen, compiler doesn't know success and failure are only options
    throw IllegalStateException()
  }

  // TODO stop using this, no need for suspend *and* callback
  suspend fun performSignPaymentRequest(token: String, complete: (CashAppSignOrderResult) -> Unit) {
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

              complete(CashAppSignOrderResult.Success(cashApp))
            }
            .onFailure {
              complete(CashAppSignOrderResult.Failure(it))
            }
        }
        .onFailure {
          complete(CashAppSignOrderResult.Failure(it))
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
              "SUCCESS" -> complete(CashAppValidationResponse.Success(it))
              else -> complete(CashAppValidationResponse.Failure(Exception("status is ${it.status}")))
            }
          }
          .onFailure {
            complete(CashAppValidationResponse.Failure(Exception(it.message)))
          }

        Unit
      }
    } ?: complete(CashAppValidationResponse.Failure(Exception("environment not set")))
  }
}

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

class AfterpayCashAppCheckout(cashHandler: AfterpayCashAppHandler?) {
    private var handler: AfterpayCashAppHandler? = cashHandler ?: Afterpay.cashAppHandler

    suspend fun performSignPaymentRequest(token: String) {
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

                            handler?.didReceiveCashAppData(CashAppSignOrderResult.Success(cashApp))
                        }
                        .onFailure {
                            handler?.didReceiveCashAppData(CashAppSignOrderResult.Failure(it))
                        }
                }
                .onFailure {
                    handler?.didReceiveCashAppData(CashAppSignOrderResult.Failure(it))
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

    companion object {
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

                    val response = withContext(Dispatchers.Unconfined) {
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
}

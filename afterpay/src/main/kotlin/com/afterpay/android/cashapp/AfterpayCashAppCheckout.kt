package com.afterpay.android.cashapp

import com.afterpay.android.Afterpay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.URL

class AfterpayCashAppCheckout(cashHandler: AfterpayCashAppHandler?) {
    private var handler: AfterpayCashAppHandler? = cashHandler ?: Afterpay.cashAppHandler

    init {
        require(Afterpay.cashAppHandler != null) { "CashApp handler must be set and not null before attempting to retrieve data" }
    }

    fun commenceCheckout() {
        handler?.didCommenceCheckout { result ->
            val token = result.getOrNull() ?: return@didCommenceCheckout handleTokenError()

            runBlocking {
                signPayment(token)
                    .onSuccess { response ->
                        val jwtBody = AfterpayCashAppJwt.decode(response.jwtToken)
                        jwtBody?.let {
                            val cashApp = AfterpayCashApp(
                                amount = jwtBody.amount.amount.toDouble(),
                                redirectUri = jwtBody.redirectUrl,
                                merchantId = jwtBody.externalMerchantId,
                                brandId = response.externalBrandId,
                            )

                            handler!!.didReceiveCashAppData(cashApp)
                        }
                    }
                    .onFailure {
                        // TODO: handle failure
                    }
            }
        }
    }

    private suspend fun signPayment(token: String): Result<AfterpayCashAppSigningResponse> {
        return runBlocking {
            Afterpay.environment?.payKitSigningUrl?.let {
                val urlString = it
                val url = URL(urlString)

                val payload = """{ "token": "$token" }"""

                val response = withContext(Dispatchers.Unconfined) {
                    AfterpayCashAppApi.cashRequest<AfterpayCashAppSigningResponse, String>(
                        url = url,
                        method = AfterpayCashAppApi.CashHttpVerb.POST,
                        body = payload
                    )
                }

                response
            } ?: Result.failure(Exception("Environment not set"))
        }
    }

    private fun handleTokenError() {
        // @TODO: need to do something here!!
    }
}

package com.afterpay.android.internal

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.afterpay.android.AfterpayCheckoutV3Options
import com.afterpay.android.model.CheckoutV3Data
import com.afterpay.android.model.CheckoutV3Tokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class CheckoutV3ViewModel(private var options: AfterpayCheckoutV3Options) : ViewModel() {

    suspend fun performCheckoutRequest(): Result<URL> {
        return runCatching {
            val checkoutUrl = requireNotNull(options.checkoutUrl)
            val checkoutPayload = requireNotNull(options.checkoutPayload)

            val response = withContext(Dispatchers.IO) {
                ApiV3.request<CheckoutV3.Response, String>(checkoutUrl, ApiV3.HttpVerb.POST, checkoutPayload)
            }.getOrThrow()

            val builder = Uri.parse(response.redirectCheckoutUrl)
                .buildUpon()
                .appendQueryParameter("buyNow", options.buyNow.toString())
                .build()
            val url = URL(builder.toString())

            options = options.copy(
                redirectUrl = url,
                singleUseCardToken = response.singleUseCardToken,
                token = response.token
            )
            url
        }
    }

    suspend fun performConfirmationRequest(ppaConfirmToken: String): Result<CheckoutV3Data> {
        return runCatching {
            val confirmationUrl = requireNotNull(options.confirmUrl)

            val tokens = CheckoutV3Tokens(
                token = requireNotNull(options.token),
                singleUseCardToken = requireNotNull(options.singleUseCardToken),
                ppaConfirmToken = ppaConfirmToken
            )

            val response = withContext(Dispatchers.IO) {
                ApiV3.request<CheckoutV3.Confirmation.Response, CheckoutV3Tokens>(
                    confirmationUrl,
                    ApiV3.HttpVerb.POST,
                    tokens
                )
            }.getOrThrow()

            CheckoutV3Data(
                cardDetails = response.paymentDetails.virtualCard ?: response.paymentDetails.virtualCardToken!!,
                cardValidUntilInternal = response.cardValidUntil,
                tokens = tokens
            )
        }
    }
}

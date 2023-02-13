package com.afterpay.android.cashapp

import android.util.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class AfterpayCashAppSigningResponse(
    var externalBrandId: String,
    var jwtToken: String,
    var redirectUrl: String,
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

                Json.decodeFromString(jwtBody)
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

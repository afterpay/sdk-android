package com.afterpay.android.cashapp

import android.util.Base64
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.UnsupportedEncodingException

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
        fun decode(jwt: String): AfterpayCashAppJwt? {
            try {
                val split = jwt.split(".").toTypedArray()
                val jwtBody = getJson(split[1])

                return Json.decodeFromString<AfterpayCashAppJwt>(jwtBody)
            } catch (e: UnsupportedEncodingException) {
                Log.d("mylogger JWT_UNSUPPORTED_ENCODING_EXCEPTION", e.toString())
                // TODO: handle this better
            } catch (e: Exception) {
                Log.d("mylogger JWT_EXCEPTION", e.toString())
                // TODO: handle this better
            }

            return null
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

package com.example.afterpay.data

import retrofit2.http.Body
import retrofit2.http.POST

interface MerchantApi {
    @POST("checkouts")
    suspend fun checkout(@Body request: CheckoutRequest): CheckoutResponse
}

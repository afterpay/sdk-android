package com.example.afterpay.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MerchantApi {
    @GET("configuration")
    suspend fun configuration(): ConfigurationResponse

    @POST("checkouts")
    suspend fun checkout(@Body request: CheckoutRequest): CheckoutResponse
}

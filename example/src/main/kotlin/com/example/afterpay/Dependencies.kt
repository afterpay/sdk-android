package com.example.afterpay

import com.example.afterpay.data.MerchantApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object Dependencies {
    val merchantApi: MerchantApi = Retrofit.Builder()
        .baseUrl("https://10.0.2.2:3001")
        .addConverterFactory(
            MoshiConverterFactory.create(
                Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()
            )
        )
        .build()
        .create(MerchantApi::class.java)
}

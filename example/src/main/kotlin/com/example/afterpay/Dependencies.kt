package com.example.afterpay

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.afterpay.data.MerchantApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class Dependencies(val merchantApi: MerchantApi, val sharedPreferences: SharedPreferences)

private var dependencies: Dependencies? = null

fun initializeDependencies(application: Application) = check(dependencies == null) {
    "Dependencies should only be initialized once"
}.also {
    dependencies = Dependencies(
        merchantApi = Retrofit.Builder()
            .baseUrl("https://10.0.2.2:3001")
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build()
                )
            )
            .build()
            .create(MerchantApi::class.java),
        sharedPreferences = application.getSharedPreferences(
            application.getString(R.string.preferences),
            Context.MODE_PRIVATE
        )
    )
}

fun getDependencies(): Dependencies = checkNotNull(dependencies) {
    "Dependencies should be initialized prior to access"
}

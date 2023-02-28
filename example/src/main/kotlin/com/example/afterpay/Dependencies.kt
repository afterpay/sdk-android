package com.example.afterpay

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.afterpay.data.MerchantApi
import com.example.afterpay.util.getHostname
import com.example.afterpay.util.getPort
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class Dependencies(val merchantApi: MerchantApi, val sharedPreferences: SharedPreferences)

private var dependencies: Dependencies? = null

fun initializeDependencies(application: Application) = check(dependencies == null) {
    "Dependencies should only be initialized once"
}.also {
    // Create SharedPreferences object.
    val preferences = application.getSharedPreferences(
        application.getString(R.string.preferences),
        Context.MODE_PRIVATE,
    )

    // Get hostname from SharedPreferences.
    var hostname = preferences.getHostname()
    val port = preferences.getPort()
    if (port.isNotBlank()) {
        hostname = "$hostname:$port"
    }
    dependencies = Dependencies(
        merchantApi = Retrofit.Builder()
            .baseUrl(hostname)
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build(),
                ),
            )
            .build()
            .create(MerchantApi::class.java),
        sharedPreferences = preferences,
    )
}

fun getDependencies(): Dependencies = checkNotNull(dependencies) {
    "Dependencies should be initialized prior to access"
}

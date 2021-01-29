package com.example.afterpay

import android.app.Application
import android.os.StrictMode
import com.jakewharton.threetenabp.AndroidThreeTen

@Suppress("unused")
class ExampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initializeDependencies(this)

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyDeath()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectActivityLeaks()
                    .detectLeakedClosableObjects()
                    .detectLeakedRegistrationObjects()
                    .penaltyDeath()
                    .build()
            )
        }

        AndroidThreeTen.init(this)
    }
}

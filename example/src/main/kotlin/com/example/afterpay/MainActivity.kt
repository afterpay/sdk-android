package com.example.afterpay

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.example.afterpay.checkout.CheckoutFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                add<CheckoutFragment>(R.id.fragment_container, null)
            }
        }
    }
}

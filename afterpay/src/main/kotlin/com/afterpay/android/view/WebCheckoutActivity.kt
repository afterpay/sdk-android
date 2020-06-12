package com.afterpay.android.view

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.afterpay.android.R
import com.afterpay.android.getCheckoutRequestExtra

class WebCheckoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_checkout)

        intent.getCheckoutRequestExtra()?.let {
            findViewById<WebView>(R.id.checkout_webView).apply {
                loadUrl(it.checkoutUrl)
            }
        }
    }
}

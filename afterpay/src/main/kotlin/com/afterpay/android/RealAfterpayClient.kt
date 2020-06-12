package com.afterpay.android

import android.content.Context
import android.content.Intent
import com.afterpay.android.view.WebCheckoutActivity

class RealAfterpayClient(private val context: Context) : AfterpayClient {
    override fun createCheckoutIntent(checkoutRequest: CheckoutRequest): Intent =
        Intent(context, WebCheckoutActivity::class.java)
            .putExtra(checkoutRequest)

    override fun parseCheckoutResponse(intent: Intent): CheckoutStatus =
        intent.getCheckoutStatusExtra() ?: CheckoutStatus.ERROR
}

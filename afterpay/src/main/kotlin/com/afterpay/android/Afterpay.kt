package com.afterpay.android

import android.content.Context
import android.content.Intent
import com.afterpay.android.util.getCheckoutStatusExtra
import com.afterpay.android.util.putCheckoutUrlExtra
import com.afterpay.android.view.WebCheckoutActivity

object Afterpay {
    /**
     * Creates an [Intent] that can be used to initiate an Afterpay transaction. Provide the
     * new [Intent] in [startActivityForResult][android.app.Activity.startActivityForResult]
     * to initiate the checkout process.
     *
     * @param context The calling activity context.
     * @param checkoutUrl The URL used to initiate the transaction.
     * @return An intent to initiate the Afterpay transaction.
     */
    fun createCheckoutIntent(context: Context, checkoutUrl: String): Intent =
        Intent(context, WebCheckoutActivity::class.java)
            .putCheckoutUrlExtra(checkoutUrl)

    /**
     * Parses the status of the Afterpay checkout transaction.
     *
     * @param intent The intent returned in
     * [startActivityForResult][android.app.Activity.startActivityForResult].
     * @return The checkout status.
     */
    fun parseCheckoutResponse(intent: Intent): CheckoutStatus =
        intent.getCheckoutStatusExtra() ?: CheckoutStatus.ERROR
}

package com.afterpay.android

import android.content.Context
import android.content.Intent
import com.afterpay.android.util.getCancellationStatusExtra
import com.afterpay.android.util.getOrderTokenExtra
import com.afterpay.android.util.putCheckoutUrlExtra
import com.afterpay.android.view.WebCheckoutActivity

object Afterpay {
    internal var configuration: Configuration? = null

    /**
     * Creates an [Intent] that can be used to initiate an Afterpay transaction. Provide the
     * new [Intent] in [startActivityForResult][android.app.Activity.startActivityForResult]
     * to initiate the checkout process.
     *
     * @param context The calling activity context.
     * @param checkoutUrl The URL used to initiate the transaction.
     * @return An intent to initiate the Afterpay transaction.
     */
    @JvmStatic
    fun createCheckoutIntent(context: Context, checkoutUrl: String): Intent =
        Intent(context, WebCheckoutActivity::class.java)
            .putCheckoutUrlExtra(checkoutUrl)

    /**
     * Parses the order token associated with a successful Afterpay transaction.
     *
     * @param intent The intent returned in
     * [startActivityForResult][android.app.Activity.startActivityForResult].
     * @return The order token associated with the transaction.
     */
    @JvmStatic
    fun parseCheckoutSuccessResponse(intent: Intent): String? =
        intent.getOrderTokenExtra()

    /**
     * Parses the status associated with a cancelled Afterpay transaction.
     *
     * @param intent The intent returned in
     * [startActivityForResult][android.app.Activity.startActivityForResult].
     * @return The status indicating why the transaction was cancelled.
     */
    @JvmStatic
    fun parseCheckoutCancellationResponse(intent: Intent): CancellationStatus? =
        intent.getCancellationStatusExtra()

    /**
     * Sets the global payment limit configuration for a merchant account.
     *
     * @param configuration The merchant account payment limit configuration.
     */
    @JvmStatic
    fun setConfiguration(configuration: Configuration) {
        this.configuration = configuration
    }
}

package com.afterpay.android

import android.content.Context
import android.content.Intent
import com.afterpay.android.internal.Configuration
import com.afterpay.android.internal.getCancellationStatusExtra
import com.afterpay.android.internal.getOrderTokenExtra
import com.afterpay.android.internal.putCheckoutUrlExtra
import com.afterpay.android.view.AfterpayCheckoutActivity
import java.lang.IllegalArgumentException
import java.lang.NumberFormatException
import java.math.BigDecimal
import java.util.Currency

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
        Intent(context, AfterpayCheckoutActivity::class.java)
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
     * Sets global payment configuration for the merchant account.
     *
     * @param minimumAmount The minimum order amount.
     * @param maximumAmount The maximum order amount.
     * @param currencyCode The currency code in ISO 4217 format.
     *
     * @throws NumberFormatException if the amount is not a valid representation of a number.
     * @throws IllegalArgumentException if the currency is not a valid ISO 4217 currency code.
     */
    @JvmStatic
    fun setConfiguration(minimumAmount: String?, maximumAmount: String, currencyCode: String) {
        configuration = Configuration(
            minimumAmount = minimumAmount?.toBigDecimal(),
            maximumAmount = maximumAmount.toBigDecimal(),
            currency = Currency.getInstance(currencyCode)
        ).also { configuration ->
            if (configuration.maximumAmount < BigDecimal.ZERO) {
                throw IllegalArgumentException("Maximum order amount is invalid")
            }
            configuration.minimumAmount?.let { minimumAmount ->
                if (minimumAmount < BigDecimal.ZERO || minimumAmount > configuration.maximumAmount) {
                    throw IllegalArgumentException("Minimum order amount is invalid")
                }
            }
        }
    }
}

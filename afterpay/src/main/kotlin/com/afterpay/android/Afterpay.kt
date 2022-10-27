package com.afterpay.android

import android.content.Context
import android.content.Intent
import com.afterpay.android.internal.AfterpayDrawable
import com.afterpay.android.internal.AfterpayString
import com.afterpay.android.internal.Brand
import com.afterpay.android.internal.Configuration
import com.afterpay.android.internal.ConfigurationObservable
import com.afterpay.android.internal.Locales
import com.afterpay.android.internal.getCancellationStatusExtra
import com.afterpay.android.internal.getOrderTokenExtra
import com.afterpay.android.internal.getRegionLanguage
import com.afterpay.android.internal.putCheckoutShouldLoadRedirectUrls
import com.afterpay.android.internal.putCheckoutUrlExtra
import com.afterpay.android.internal.putCheckoutV2OptionsExtra
import com.afterpay.android.view.AfterpayCheckoutActivity
import com.afterpay.android.view.AfterpayCheckoutV2Activity
import java.math.BigDecimal
import java.util.Currency
import java.util.Locale
import kotlin.properties.Delegates.observable

object Afterpay {
    internal var configuration by observable<Configuration?>(initialValue = null) { _, old, new ->
        if (new != old) {
            ConfigurationObservable.configurationChanged(new)
        }
    }
        private set

    internal val locale: Locale
        get() = configuration?.locale ?: Locales.EN_US

    internal val brand: Brand
        get() = Brand.forLocale(locale)

    internal val language: Locale?
        get() = getRegionLanguage(locale, Locale.getDefault())

    internal val enabled: Boolean
        get() = language != null

    internal val strings: AfterpayString
        get() = AfterpayString.forLocale()

    internal val drawables: AfterpayDrawable
        get() = AfterpayDrawable.forLocale()

    internal var checkoutV2Handler: AfterpayCheckoutV2Handler? = null
        private set

    /**
     * Returns an [Intent] for the given [context] and [checkoutUrl] that can be passed to
     * [startActivityForResult][android.app.Activity.startActivityForResult] to initiate the
     * Afterpay checkout.
     */
    @JvmStatic
    fun createCheckoutIntent(context: Context, checkoutUrl: String, loadRedirectUrls: Boolean = false): Intent {
        val url = if (enabled) { checkoutUrl } else { "LANGUAGE_NOT_SUPPORTED" }
        return Intent(context, AfterpayCheckoutActivity::class.java)
            .putCheckoutUrlExtra(url)
            .putCheckoutShouldLoadRedirectUrls(loadRedirectUrls)
    }

    /**
     * Returns an [Intent] for the given [context] and [options] that can be passed to
     * [startActivityForResult][android.app.Activity.startActivityForResult] to initiate the
     * Afterpay checkout.
     */
    @JvmStatic
    fun createCheckoutV2Intent(
        context: Context,
        options: AfterpayCheckoutV2Options = AfterpayCheckoutV2Options(),
    ): Intent = Intent(context, AfterpayCheckoutV2Activity::class.java)
        .putCheckoutV2OptionsExtra(options)

    /**
     * Returns the [token][String] parsed from the given [intent] returned by a successful
     * Afterpay checkout.
     */
    @JvmStatic
    fun parseCheckoutSuccessResponse(intent: Intent): String? =
        intent.getOrderTokenExtra()

    /**
     * Returns the [status][CancellationStatus] parsed from the given [intent] returned by a
     * cancelled Afterpay checkout.
     */
    @JvmStatic
    fun parseCheckoutCancellationResponse(intent: Intent): CancellationStatus? =
        intent.getCancellationStatusExtra()

    /**
     * Sets the global checkout configuration comprising the [minimum order amount][minimumAmount],
     * [maximum order amount][maximumAmount], [currency code in ISO 4217 format][currencyCode],
     * [locale] for formatting of terms and conditions and currency, and the [environment] in which
     * to launch the checkout.
     *
     * Results in a [NumberFormatException] if an amount is not a valid representation of a number
     * or an [IllegalArgumentException] if the currency is not a valid ISO 4217 currency code, if
     * the minimum and maximum amount isn't correctly ordered, or if the locale is not supported.
     */
    @JvmStatic
    fun setConfiguration(
        minimumAmount: String?,
        maximumAmount: String,
        currencyCode: String,
        locale: Locale,
        environment: AfterpayEnvironment,
    ) {
        configuration = Configuration(
            minimumAmount = minimumAmount?.toBigDecimal(),
            maximumAmount = maximumAmount.toBigDecimal(),
            currency = Currency.getInstance(currencyCode),
            locale = locale.clone() as Locale,
            environment = environment,
        ).also { configuration ->
            if (configuration.maximumAmount < BigDecimal.ZERO) {
                throw IllegalArgumentException("Maximum order amount is invalid")
            }
            configuration.minimumAmount?.let { minimumAmount ->
                if (minimumAmount < BigDecimal.ZERO || minimumAmount > configuration.maximumAmount) {
                    throw IllegalArgumentException("Minimum order amount is invalid")
                }
            }
            if (!Locales.validSet.contains(configuration.locale)) {
                val validCountries = Locales.validSet.map { it.country }
                throw IllegalArgumentException(
                    "Locale contains an unsupported country: ${configuration.locale.country}. " +
                        "Supported countries include: ${validCountries.joinToString(",")}",
                )
            }
        }
    }

    /**
     * Sets the global [handler] used to provide callbacks for the v2 checkout.
     */
    @JvmStatic
    fun setCheckoutV2Handler(handler: AfterpayCheckoutV2Handler?) {
        checkoutV2Handler = handler
    }
}

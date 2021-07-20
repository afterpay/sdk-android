package com.afterpay.android

import android.content.Context
import android.content.Intent
import com.afterpay.android.internal.ApiV3
import com.afterpay.android.internal.CheckoutV3
import com.afterpay.android.model.CheckoutV3Tokens
import com.afterpay.android.model.Configuration
import com.afterpay.android.internal.ConfigurationObservable
import com.afterpay.android.internal.Locales
import com.afterpay.android.internal.getCancellationStatusExtra
import com.afterpay.android.internal.getOrderTokenExtra
import com.afterpay.android.internal.getResultDataExtra
import com.afterpay.android.internal.putCheckoutUrlExtra
import com.afterpay.android.internal.putCheckoutV2OptionsExtra
import com.afterpay.android.internal.putCheckoutV3OptionsExtra
import com.afterpay.android.model.CheckoutV3Configuration
import com.afterpay.android.model.CheckoutV3Consumer
import com.afterpay.android.model.CheckoutV3Data
import com.afterpay.android.model.CheckoutV3Item
import com.afterpay.android.model.MerchantConfigurationV3
import com.afterpay.android.model.OrderTotal
import com.afterpay.android.view.AfterpayCheckoutActivity
import com.afterpay.android.view.AfterpayCheckoutV2Activity
import com.afterpay.android.view.AfterpayCheckoutV3Activity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
        get() = configuration?.locale ?: Locales.US

    internal var checkoutV2Handler: AfterpayCheckoutV2Handler? = null
        private set

    /**
     * Returns an [Intent] for the given [context] and [checkoutUrl] that can be passed to
     * [startActivityForResult][android.app.Activity.startActivityForResult] to initiate the
     * Afterpay checkout.
     */
    @JvmStatic
    fun createCheckoutIntent(context: Context, checkoutUrl: String): Intent =
        Intent(context, AfterpayCheckoutActivity::class.java)
            .putCheckoutUrlExtra(checkoutUrl)

    /**
     * Returns an [Intent] for the given [context] and [options] that can be passed to
     * [startActivityForResult][android.app.Activity.startActivityForResult] to initiate the
     * Afterpay checkout.
     */
    @JvmStatic
    fun createCheckoutV2Intent(
        context: Context,
        options: AfterpayCheckoutV2Options = AfterpayCheckoutV2Options()
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
        environment: AfterpayEnvironment
    ) {
        configuration = Configuration(
            minimumAmount = minimumAmount?.toBigDecimal(),
            maximumAmount = maximumAmount.toBigDecimal(),
            currency = Currency.getInstance(currencyCode),
            locale = locale.clone() as Locale,
            environment = environment
        ).also { validateConfiguration(it) }
    }

    /**
     * Sets the global checkout configuration object.
     *
     * Results in a [NumberFormatException] if an amount is not a valid representation of a number
     * or an [IllegalArgumentException] if the currency is not a valid ISO 4217 currency code, if
     * the minimum and maximum amount isn't correctly ordered, or if the locale is not supported.
     */
    @JvmStatic
    fun setConfigurationV3(newConfiguration: Configuration) {
        configuration = newConfiguration.also { validateConfiguration(it) }
    }

    private fun validateConfiguration(configuration: Configuration) {
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
                    "Supported countries include: ${validCountries.joinToString(",")}"
            )
        }
    }

    /**
     * Sets the global [handler] used to provide callbacks for the v2 checkout.
     */
    @JvmStatic
    fun setCheckoutV2Handler(handler: AfterpayCheckoutV2Handler?) {
        checkoutV2Handler = handler
    }

    // V3 work

    private var checkoutV3Configuration: CheckoutV3Configuration? = null

    @JvmStatic
    fun setCheckoutV3Configuration(configuration: CheckoutV3Configuration) {
        checkoutV3Configuration = configuration
    }

    @JvmStatic
    fun updateMerchantReferenceV3(
        merchantReference: String,
        tokens: CheckoutV3Tokens,
        configuration: CheckoutV3Configuration? = checkoutV3Configuration
    ): Result<Unit> {
        val configuration = configuration
            ?: throw IllegalArgumentException("`configuration` must be set via `setCheckoutV3Configuration` or passed into this function")

        val payload = CheckoutV3.MerchantReferenceUpdate(
            merchantReference,
            token = tokens.token,
            ppaConfirmToken = tokens.ppaConfirmToken,
            singleUseCardToken =  tokens.singleUseCardToken
        )

        return ApiV3.requestUnit(
            configuration.v3CheckoutUrl,
            ApiV3.HttpVerb.PUT,
            payload
        )
    }

    @JvmStatic
    fun fetchMerchantConfigurationV3(
        configuration: CheckoutV3Configuration? = checkoutV3Configuration
    ): Result<Configuration> {
        val configuration = configuration
            ?: throw IllegalArgumentException("`configuration` must be set via `setCheckoutV3Configuration` or passed into this function")

        return ApiV3.get<MerchantConfigurationV3>(configuration.v3ConfigurationUrl)
            .map {
                Configuration(
                    minimumAmount = it.minimumAmount.amount,
                    maximumAmount = it.maximumAmount.amount,
                    currency = Currency.getInstance(configuration.region.currencyCode),
                    locale = configuration.region.locale,
                    environment = configuration.environment
                )
            }
    }

    @JvmStatic
    fun createCheckoutV3Intent(
        context: Context,
        consumer: CheckoutV3Consumer,
        orderTotal: OrderTotal,
        items: Array<CheckoutV3Item> = arrayOf(),
        buyNow: Boolean,
        configuration: CheckoutV3Configuration? = checkoutV3Configuration
    ): Intent {
        val configuration = configuration
            ?: throw IllegalArgumentException("`configuration` must be set via `setCheckoutV3Configuration` or passed into this function")

        val checkoutRequest = CheckoutV3.Request.create(
            consumer = consumer,
            orderTotal = orderTotal,
            items = items,
            configuration = configuration,
        )
        val options = AfterpayCheckoutV3Options(
            buyNow = buyNow,
            checkoutPayload = Json.encodeToString(checkoutRequest),
            checkoutUrl = configuration.v3CheckoutUrl,
            confirmUrl = configuration.v3CheckoutConfirmationUrl
        )

        return Intent(context, AfterpayCheckoutV3Activity::class.java)
            .putCheckoutV3OptionsExtra(options)
    }

    /**
     * Returns the [CheckoutV3Data] returned by a successful Afterpay checkout.
     */
    @JvmStatic
    fun parseCheckoutSuccessResponseV3(intent: Intent): CheckoutV3Data? =
        intent.getResultDataExtra()
}

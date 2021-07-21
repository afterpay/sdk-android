package com.afterpay.android

import android.content.Context
import android.content.Intent
import com.afterpay.android.internal.ApiV3
import com.afterpay.android.internal.CheckoutV3
import com.afterpay.android.internal.ConfigurationObservable
import com.afterpay.android.internal.Locales
import com.afterpay.android.internal.getCancellationStatusExtra
import com.afterpay.android.internal.getCancellationStatusExtraErrorV3
import com.afterpay.android.internal.getCancellationStatusExtraV3
import com.afterpay.android.internal.getOrderTokenExtra
import com.afterpay.android.internal.getResultDataExtra
import com.afterpay.android.internal.putCheckoutUrlExtra
import com.afterpay.android.internal.putCheckoutV2OptionsExtra
import com.afterpay.android.internal.putCheckoutV3OptionsExtra
import com.afterpay.android.model.CheckoutV3Configuration
import com.afterpay.android.model.CheckoutV3Consumer
import com.afterpay.android.model.CheckoutV3Data
import com.afterpay.android.model.CheckoutV3Item
import com.afterpay.android.model.CheckoutV3Tokens
import com.afterpay.android.model.Configuration
import com.afterpay.android.model.MerchantConfigurationV3
import com.afterpay.android.model.OrderTotal
import com.afterpay.android.view.AfterpayCheckoutActivity
import com.afterpay.android.view.AfterpayCheckoutV2Activity
import com.afterpay.android.view.AfterpayCheckoutV3Activity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.util.Currency
import java.util.Locale
import java.util.concurrent.CompletableFuture
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

    private fun validateConfiguration(configuration: Configuration) {
        require(configuration.maximumAmount >= BigDecimal.ZERO) { "Maximum order amount is invalid" }
        configuration.minimumAmount?.let { minimumAmount ->
            require(minimumAmount > BigDecimal.ZERO && minimumAmount < configuration.maximumAmount) {
                "Minimum order amount is invalid"
            }
        }
        require(Locales.validSet.contains(configuration.locale)) {
            val validCountries = Locales.validSet.joinToString(",") { it.country }
            "Locale contains an unsupported country: ${configuration.locale.country}. Supported countries include: $validCountries"
        }
    }

    /**
     * Sets the global [handler] used to provide callbacks for the v2 checkout.
     */
    @JvmStatic
    fun setCheckoutV2Handler(handler: AfterpayCheckoutV2Handler?) {
        checkoutV2Handler = handler
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

    private var checkoutV3Configuration: CheckoutV3Configuration? = null

    /**
     * Sets the collection of options and values required to interact with the Afterpay API.
     */
    @JvmStatic
    fun setCheckoutV3Configuration(configuration: CheckoutV3Configuration) {
        checkoutV3Configuration = configuration
    }

    /**
     * Returns an [Intent] for the given [context] and options that can be passed to
     * [startActivityForResult][android.app.Activity.startActivityForResult] to initiate the
     * Afterpay checkout.
     */
    @JvmStatic
    fun createCheckoutV3Intent(
        context: Context,
        consumer: CheckoutV3Consumer,
        orderTotal: OrderTotal,
        items: Array<CheckoutV3Item> = arrayOf(),
        buyNow: Boolean,
        configuration: CheckoutV3Configuration? = checkoutV3Configuration
    ): Intent {
        requireNotNull(configuration) {
            "`configuration` must be set via `setCheckoutV3Configuration` or passed into this function"
        }
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
     * Updates the [merchantReference] corresponding to the checkout represented by the provided [tokens].
     */
    @JvmSynthetic
    fun updateMerchantReferenceV3(
        merchantReference: String,
        tokens: CheckoutV3Tokens,
        configuration: CheckoutV3Configuration? = checkoutV3Configuration
    ): Result<Unit> {
        requireNotNull(configuration) {
            "`configuration` must be set via `setCheckoutV3Configuration` or passed into this function"
        }

        val payload = CheckoutV3.MerchantReferenceUpdate(
            merchantReference,
            token = tokens.token,
            ppaConfirmToken = tokens.ppaConfirmToken,
            singleUseCardToken = tokens.singleUseCardToken
        )

        return ApiV3.requestUnit(
            configuration.v3CheckoutUrl,
            ApiV3.HttpVerb.PUT,
            payload
        )
    }

    /**
     * Updates the [merchantReference] corresponding to the checkout represented by the provided [tokens].
     */
    @JvmStatic
    @JvmOverloads
    fun updateMerchantReferenceV3Async(
        merchantReference: String,
        tokens: CheckoutV3Tokens,
        configuration: CheckoutV3Configuration? = checkoutV3Configuration,
    ): CompletableFuture<Void?> {
        return GlobalScope.future {
            updateMerchantReferenceV3(merchantReference, tokens, configuration)
                .fold(
                    onSuccess = { null },
                    onFailure = { throw it }
                )
        }
    }

    /**
     * Returns the [Configuration] inclusive of minimum and maximum spend available.
     */
    @JvmSynthetic
    fun fetchMerchantConfigurationV3(
        configuration: CheckoutV3Configuration? = checkoutV3Configuration
    ): Result<Configuration> {
        requireNotNull(configuration) {
            "`configuration` must be set via `setCheckoutV3Configuration` or passed into this function"
        }

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

    /**
     * Returns the [Configuration] inclusive of minimum and maximum spend available.
     */
    @JvmStatic
    @JvmOverloads
    fun fetchMerchantConfigurationV3Async(
        configuration: CheckoutV3Configuration? = checkoutV3Configuration
    ): CompletableFuture<Configuration> {
        requireNotNull(configuration) {
            "`configuration` must be set via `setCheckoutV3Configuration` or passed into this function"
        }

        return GlobalScope.future {
            val result = ApiV3.get<MerchantConfigurationV3>(configuration.v3ConfigurationUrl)
                .map {
                    Configuration(
                        minimumAmount = it.minimumAmount.amount,
                        maximumAmount = it.maximumAmount.amount,
                        currency = Currency.getInstance(configuration.region.currencyCode),
                        locale = configuration.region.locale,
                        environment = configuration.environment
                    )
                }

            result.fold(
                onSuccess = { it },
                onFailure = { throw it }
            )
        }
    }

    /**
     * Returns the [CheckoutV3Data] returned by a successful Afterpay checkout.
     */
    @JvmStatic
    fun parseCheckoutSuccessResponseV3(intent: Intent): CheckoutV3Data? =
        intent.getResultDataExtra()

    /**
     * Returns the [status][CancellationStatusV3] and [Exception] parsed from the given [intent] returned by a
     * cancelled Afterpay checkout.
     */
    @JvmStatic
    fun parseCheckoutCancellationResponseV3(intent: Intent): Pair<CancellationStatusV3, Exception?>? =
        intent.getCancellationStatusExtraV3()?.let {
            Pair(it, intent.getCancellationStatusExtraErrorV3())
        }
}

/*
 * Copyright (C) 2024 Afterpay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afterpay.android

import android.content.Context
import android.content.Intent
import androidx.annotation.WorkerThread
import com.afterpay.android.cashapp.AfterpayCashAppCheckout
import com.afterpay.android.cashapp.CashAppSignOrderResult
import com.afterpay.android.cashapp.CashAppSignOrderResult.Failure
import com.afterpay.android.cashapp.CashAppSignOrderResult.Success
import com.afterpay.android.cashapp.CashAppValidationResponse
import com.afterpay.android.internal.AfterpayDrawable
import com.afterpay.android.internal.AfterpayString
import com.afterpay.android.internal.ApiV3
import com.afterpay.android.internal.Brand
import com.afterpay.android.internal.CheckoutV3
import com.afterpay.android.internal.CheckoutV3.Confirmation.CashAppPayRequest
import com.afterpay.android.internal.CheckoutV3.Confirmation.CashAppPayResponse
import com.afterpay.android.internal.ConfigurationObservable
import com.afterpay.android.internal.Locales
import com.afterpay.android.internal.getCancellationStatusExtra
import com.afterpay.android.internal.getCancellationStatusExtraErrorV3
import com.afterpay.android.internal.getCancellationStatusExtraV3
import com.afterpay.android.internal.getOrderTokenExtra
import com.afterpay.android.internal.getRegionLanguage
import com.afterpay.android.internal.getResultDataExtra
import com.afterpay.android.internal.putCheckoutShouldLoadRedirectUrls
import com.afterpay.android.internal.putCheckoutUrlExtra
import com.afterpay.android.internal.putCheckoutV2OptionsExtra
import com.afterpay.android.internal.putCheckoutV3OptionsExtra
import com.afterpay.android.model.CheckoutV3CashAppPay
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
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.util.Currency
import java.util.Locale
import java.util.concurrent.CompletableFuture
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
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
    get() = getRegionLanguage(locale, configuration?.consumerLocale ?: Locale.getDefault())

  internal val enabled: Boolean
    get() = language != null && configuration?.locale != null

  internal val strings: AfterpayString
    get() = AfterpayString.forLocale()

  internal val drawables: AfterpayDrawable
    get() = AfterpayDrawable.forLocale()

  internal var checkoutV2Handler: AfterpayCheckoutV2Handler? = null
    private set

  val environment: AfterpayEnvironment?
    get() = configuration?.environment

  /**
   * Returns an [Intent] for the given [context] and [checkoutUrl] that can be passed to
   * [startActivityForResult][android.app.Activity.startActivityForResult] to initiate the
   * Afterpay checkout.
   */
  @JvmStatic
  fun createCheckoutIntent(context: Context, checkoutUrl: String, loadRedirectUrls: Boolean = false): Intent {
    val url = if (enabled) {
      checkoutUrl
    } else {
      "LANGUAGE_NOT_SUPPORTED"
    }
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
   * Signs an Afterpay Cash App order for the relevant [token] and calls
   * calls [complete] when done. This method should be called prior to calling
   * createCustomerRequest on the Cash App Pay Kit SDK
   */
  @JvmStatic
  @WorkerThread
  suspend fun signCashAppOrderToken(
    token: String,
    complete: (CashAppSignOrderResult) -> Unit,
  ) {
    AfterpayCashAppCheckout.performSignPaymentRequest(token, complete)
  }

  /**
   * Async version of the [signCashAppOrderToken] method.
   *
   * Signs an Afterpay Cash App order for the relevant [token] and calls
   * [complete] when done. This method should be called prior to calling
   * createCustomerRequest on the Cash App Pay Kit SDK
   */
  @DelicateCoroutinesApi
  @JvmStatic
  fun signCashAppOrderTokenAsync(
    token: String,
    complete: (CashAppSignOrderResult) -> Unit,
  ): CompletableFuture<Unit?> {
    return GlobalScope.future {
      signCashAppOrderToken(token, complete)
    }
  }

  /**
   * Validates the Cash App order for the relevant [jwt], [customerId] and [grantId]
   * and calls [complete] once finished. This method should be called for a One Time payment
   * once the Cash App order is in the approved state
   */
  @JvmStatic
  @WorkerThread
  fun validateCashAppOrder(
    jwt: String,
    customerId: String,
    grantId: String,
    complete: (CashAppValidationResponse) -> Unit,
  ) {
    AfterpayCashAppCheckout.validatePayment(jwt, customerId, grantId, complete)
  }

  /**
   * Async version of the [validateCashAppOrder] method.
   *
   * Validates the Cash App order for the relevant [jwt], [customerId] and [grantId]
   * and calls [complete] once finished. This method should be called for a One Time payment
   * once the Cash App order is in the approved state
   */
  @DelicateCoroutinesApi
  @JvmStatic
  fun validateCashAppOrderAsync(
    jwt: String,
    customerId: String,
    grantId: String,
    complete: (CashAppValidationResponse) -> Unit,
  ): CompletableFuture<Unit> {
    return GlobalScope.future {
      validateCashAppOrder(jwt, customerId, grantId, complete)
    }
  }

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
   *
   * Must be called from main thread
   */
  @JvmStatic
  fun setConfiguration(
    minimumAmount: String?,
    maximumAmount: String,
    currencyCode: String,
    locale: Locale,
    environment: AfterpayEnvironment,
    consumerLocale: Locale? = null,
  ) {
    configuration = Configuration(
      minimumAmount = minimumAmount?.toBigDecimal(),
      maximumAmount = maximumAmount.toBigDecimal(),
      currency = Currency.getInstance(currencyCode),
      locale = locale.clone() as Locale,
      environment = environment,
      consumerLocale = consumerLocale,
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

  // region: V3
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
   * Start checkout process by requesting data to pass to Cash App Pay SDK
   *
   * @param consumer information about customer
   * @param orderTotal pricing information about this order
   * @param items list of items in customer's cart
   * @param configuration must be provided if not previously set via [setCheckoutV3Configuration]
   */
  suspend fun beginCheckoutV3WithCashAppPay(
    consumer: CheckoutV3Consumer,
    orderTotal: OrderTotal,
    items: Array<CheckoutV3Item> = arrayOf(),
    configuration: CheckoutV3Configuration? = checkoutV3Configuration,
  ): Result<CheckoutV3CashAppPay> {
    requireNotNull(configuration) {
      "`configuration` must be set via `setCheckoutV3Configuration` or passed into this function"
    }
    val checkoutRequest = CheckoutV3.Request.create(
      consumer = consumer,
      orderTotal = orderTotal,
      items = items,
      configuration = configuration,
      isCashAppPay = true,
    )

    val checkoutResponseResult = runCatching {
      val checkoutUrl = configuration.v3CheckoutUrl
      val checkoutPayload = requireNotNull(Json.encodeToString(checkoutRequest))
      return@runCatching withContext(Dispatchers.IO) {
        ApiV3.request<CheckoutV3.Response, String>(
          checkoutUrl,
          ApiV3.HttpVerb.POST,
          checkoutPayload,
        )
      }.getOrThrow()
    }

    // requesting checkout data failed
    checkoutResponseResult.onFailure { error: Throwable ->
      return failure(error)
    }

    // requesting checkout data success: sign the checkout response token
    checkoutResponseResult.onSuccess { result: CheckoutV3.Response ->
      AfterpayCashAppCheckout.performSignPaymentRequest(result.token)
        .let { cashAppSignOrderResult: CashAppSignOrderResult ->
          return when (cashAppSignOrderResult) {
            // signing failed
            is Failure -> failure(cashAppSignOrderResult.error)

            // signing success
            is Success -> {
              // map Result<CashAppSignOrderResult> to Result<CheckoutV3CashAppPay>
              success(
                CheckoutV3CashAppPay(
                  token = result.token,
                  singleUseCardToken = result.singleUseCardToken,
                  amount = cashAppSignOrderResult.response.amount,
                  redirectUri = cashAppSignOrderResult.response.redirectUri,
                  merchantId = cashAppSignOrderResult.response.merchantId,
                  brandId = cashAppSignOrderResult.response.brandId,
                  jwt = cashAppSignOrderResult.response.jwt,
                ),
              )
            }
          }
        }
    }

    // should never happen, compiler doesn't know success and failure are only options
    throw IllegalStateException()
  }

  /**
   * Confirm that checkout was completed with Cash App Pay SDK
   *
   * @param customerId customer ID, received from Cash App Pay SDK
   * @param grantId grant ID received from Cash App Pay SDK
   * @param token  token received from  [beginCheckoutV3WithCashAppPay]
   * @param singleUseCardToken singleUseCardToken received from  [beginCheckoutV3WithCashAppPay]
   * @param jwt JSON web token received from [beginCheckoutV3WithCashAppPay]
   * @param configuration must be provided if not previously set via [setCheckoutV3Configuration]
   */
  suspend fun confirmCheckoutV3WithCashAppPay(
    customerId: String,
    grantId: String,
    token: String,
    singleUseCardToken: String,
    jwt: String,
    configuration: CheckoutV3Configuration? = checkoutV3Configuration,
  ): Result<CheckoutV3Data> {
    return runCatching {
      requireNotNull(configuration) {
        "`configuration` must be set via `setCheckoutV3Configuration` or passed into this function"
      }

      val confirmUrl = configuration.v3CheckoutConfirmationUrl

      val request = CashAppPayRequest(
        token = token,
        singleUseCardToken = singleUseCardToken,
        cashAppPspInfo = CashAppPayRequest.CashAppPspInfo(
          externalCustomerId = customerId,
          externalGrantId = grantId,
          jwt = jwt,
        ),
      )

      val response = withContext(Dispatchers.IO) {
        ApiV3.request<CashAppPayResponse, CashAppPayRequest>(
          url = confirmUrl,
          method = ApiV3.HttpVerb.POST,
          body = request,
        )
      }.getOrThrow()

      CheckoutV3Data(
        cardDetails = response.paymentDetails.virtualCard
          ?: response.paymentDetails.virtualCardToken!!,
        cardValidUntilInternal = response.cardValidUntil,
        tokens = CheckoutV3Tokens(
          token = token,
          singleUseCardToken = singleUseCardToken,
          ppaConfirmToken = "", // this token is not used in Cash App flow
        ),
      )
    }
  }

  /**
   * Returns an [Intent] for the given [context] and options that can be passed to
   * [startActivityForResult][android.app.Activity.startActivityForResult] to initiate the
   * Afterpay checkout.
   */
  @JvmStatic
  @JvmOverloads
  fun createCheckoutV3Intent(
    context: Context,
    consumer: CheckoutV3Consumer,
    orderTotal: OrderTotal,
    items: Array<CheckoutV3Item> = arrayOf(),
    buyNow: Boolean,
    configuration: CheckoutV3Configuration? = checkoutV3Configuration,
  ): Intent {
    requireNotNull(configuration) {
      "`configuration` must be set via `setCheckoutV3Configuration` or passed into this function"
    }
    val checkoutRequest = CheckoutV3.Request.create(
      consumer = consumer,
      orderTotal = orderTotal,
      items = items,
      configuration = configuration,
      isCashAppPay = false,
    )
    val options = AfterpayCheckoutV3Options(
      buyNow = buyNow,
      checkoutPayload = Json.encodeToString(checkoutRequest),
      checkoutUrl = configuration.v3CheckoutUrl,
      confirmUrl = configuration.v3CheckoutConfirmationUrl,
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
    configuration: CheckoutV3Configuration? = checkoutV3Configuration,
  ): Result<Unit> {
    requireNotNull(configuration) {
      "`configuration` must be set via `setCheckoutV3Configuration` or passed into this function"
    }

    val payload = CheckoutV3.MerchantReferenceUpdate(
      merchantReference,
      token = tokens.token,
      ppaConfirmToken = tokens.ppaConfirmToken,
      singleUseCardToken = tokens.singleUseCardToken,
    )

    return ApiV3.requestUnit(
      configuration.v3CheckoutUrl,
      ApiV3.HttpVerb.PUT,
      payload,
    )
  }

  /**
   * Updates the [merchantReference] corresponding to the checkout represented by the provided [tokens].
   */
  @DelicateCoroutinesApi
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
          onFailure = { throw it },
        )
    }
  }

  /**
   * Returns the [Configuration] inclusive of minimum and maximum spend available.
   */
  @JvmSynthetic
  fun fetchMerchantConfigurationV3(
    configuration: CheckoutV3Configuration? = checkoutV3Configuration,
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
          environment = configuration.environment,
        )
      }
  }

  /**
   * Returns the [Configuration] inclusive of minimum and maximum spend available.
   */
  @DelicateCoroutinesApi
  @JvmStatic
  @JvmOverloads
  fun fetchMerchantConfigurationV3Async(
    configuration: CheckoutV3Configuration? = checkoutV3Configuration,
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
            environment = configuration.environment,
          )
        }

      result.fold(
        onSuccess = { it },
        onFailure = { throw it },
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
  // endregion: v3
}

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
package com.example

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import app.cash.paykit.core.CashAppPay
import app.cash.paykit.core.CashAppPayFactory
import app.cash.paykit.core.CashAppPayListener
import app.cash.paykit.core.CashAppPayState
import app.cash.paykit.core.CashAppPayState.Approved
import app.cash.paykit.core.CashAppPayState.Authorizing
import app.cash.paykit.core.CashAppPayState.CashAppPayExceptionState
import app.cash.paykit.core.CashAppPayState.CreatingCustomerRequest
import app.cash.paykit.core.CashAppPayState.Declined
import app.cash.paykit.core.CashAppPayState.NotStarted
import app.cash.paykit.core.CashAppPayState.PollingTransactionStatus
import app.cash.paykit.core.CashAppPayState.ReadyToAuthorize
import app.cash.paykit.core.CashAppPayState.Refreshing
import app.cash.paykit.core.CashAppPayState.RetrievingExistingCustomerRequest
import app.cash.paykit.core.CashAppPayState.UpdatingCustomerRequest
import app.cash.paykit.core.models.response.Grant
import app.cash.paykit.core.models.sdk.CashAppPayCurrency.USD
import app.cash.paykit.core.models.sdk.CashAppPayPaymentAction
import com.afterpay.android.Afterpay
import com.afterpay.android.model.CheckoutV3CashAppPay
import com.afterpay.android.model.CheckoutV3Data
import com.afterpay.android.model.Configuration
import com.example.databinding.CashAppV3LayoutBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Activity showing the Cash App Pay V3 checkout flow
 */
class CashAppV3SampleActivity : AppCompatActivity() {

  private lateinit var bindings: CashAppV3LayoutBinding
  private lateinit var cashAppPay: CashAppPay

  private var checkoutV3CashAppPay: CheckoutV3CashAppPay? = null

  private val cashAppPayListener =
    object : CashAppPayListener {
      override fun cashAppPayStateDidChange(newState: CashAppPayState) {
        Log.d(tag, "cashAppPayStateDidChange: ${newState::class.java}")
        when (newState) {
          is ReadyToAuthorize -> {
            /**
             * Step 6: Cash App Pay SDK will response when an authorization attempt is ready
             * We can now enable the button to let customer proceed with checkout
             */
            lifecycleScope.launch { // jump back to UI thread to update UI
              bindings.cashappPayButton.isEnabled = true
            }
          }

          Authorizing -> {
            /**
             * Step 8: Disable button while auth in process
             */
            bindings.cashappPayButton.isEnabled = false
          }

          is Approved -> {
            /**
             * Step 9: After successful approval, confirm checkout with Afterpay
             */
            Log.d(tag, newState.responseData.toString())

            newState.responseData.apply {
              // optionally, retrieve customer's cash tag
              val cashTag = customerProfile?.cashTag
              showToast(
                this@CashAppV3SampleActivity,
                "Grant approved for customer: $cashTag",
              )

              grants?.get(0)?.let { grant: Grant ->
                CoroutineScope(Dispatchers.IO).launch {
                  confirmCheckoutWithAfterpay(
                    grantId = grant.id,
                    customerId = grant.customerId,
                  )
                }
              }
            }
          }

          is CashAppPayExceptionState -> {
            showToast(this@CashAppV3SampleActivity, "Cash App Pay Exception")
            Log.e(tag, "Cash App Pay Exception", newState.exception)
          }

          Declined -> {
            showToast(this@CashAppV3SampleActivity, "Payment Declined")
          }

          NotStarted -> {
            bindings.cashappPayButton.isEnabled = false
          }

          CreatingCustomerRequest -> {
            // Use this state to display loading status if desired.
          }

          PollingTransactionStatus -> {
            // Use this state to display loading status if desired.
          }

          UpdatingCustomerRequest -> {
            // Use this state to display loading status if desired.
          }

          RetrievingExistingCustomerRequest -> {
            // Use this state to display loading status if desired.
            // Used only when retrieving existing customer request. Corner case (eg.: app killed)
          }

          Refreshing -> {
            // Optional. Use this state to display loading status if desired.
            bindings.cashappPayButton.isEnabled = false
          }
        }
      }
    }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    /**
     * If this activity was killed during Cash App flow, and recreated, then this intent
     * can be used to start with an existing customer request. See
     * Cash App Pay SDK for more information
     * https://developers.cash.app/docs/api/technical-documentation/sdks/pay-kit/android-getting-started#start-with-an-existing-customer-request
     *
     * If this activity was not killed and instead just resumed then the [CashAppPayListener]
     * above will continue to function and events should be handled there
     */
    Log.d(tag, "onNewIntent $intent  + ${intent?.extras}")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    /**
     * The SDK is agnostic to the UI library of your choice: inflate XML, view binding, Compose, etc.
     * Here we use view binding to keep it simple.
     */
    bindings = CashAppV3LayoutBinding.inflate(LayoutInflater.from(this))
    bindings.cashappPayButton.apply {
      isEnabled = false
      setOnClickListener { _ ->
        /**
         * Step 7: When customer clicks Cash App Pay button
         *  begin authorization. This will begin UI flow into Cash App
         */
        cashAppPay.authorizeCustomerRequest()
      }
    }

    /**
     * Step 0: Periodically check for new Merchant configurations. Doesn't need
     * to happen before *every* transaction. Does need to happen before *first* transaction
     * Use the tool of your choice to enable async non-main-thread network request (i.e. coroutines, rxjava)
     *
     * Using [lifecycleScope] is for simplicity. Ideally you would not tie this network
     * request to an Activity's lifecycle. Use the networking, storage, lifecycle libraries of
     * your choice.
     */
    lifecycleScope.launch {
      getMerchantConfig()
    }

    val view = bindings.root
    setContentView(view)
  }

  private fun getMerchantConfig() =
    CoroutineScope(Dispatchers.IO).launch {
      Afterpay.fetchMerchantConfigurationV3(createCheckoutV3Configuration())
        .let { result: Result<Configuration> ->
          result.onSuccess { merchantConfiguration: Configuration ->
            /**
             * Step 1: Set configurations and being checkout process
             *
             * It is up to you to save this Configuration (e.g. local storage) to avoid repeat calls
             * to fetch it. You will need to pass Configuration to Afterpay on each app
             * restart (before first transaction) by calling .setConfigurationV3()
             */
            Log.d(tag, "Fetched merchant configs")
            Afterpay.setConfigurationV3(merchantConfiguration)
            initializeCashAppSDK()
            beginCheckout()
          }

          result.onFailure {
            showToastFromBackground(
              this@CashAppV3SampleActivity,
              "Failed to fetch merchant configs",
            )
          }
        }
    }

  private fun initializeCashAppSDK() {
    /**
     * Step 2: create and register a state listener with Cash APp Pay SDK
     */
    Log.d(tag, "Initializing Cash App Pay SDK")
    cashAppPay = CashAppPayFactory.createSandbox(AFTERPAY_ENVIRONMENT.payKitClientId)
    cashAppPay.registerForStateUpdates(cashAppPayListener)
  }

  private suspend fun beginCheckout() {
    /**
     * Step 3: Begin checkout process by requesting data from Afterpay, to be used later
     * with Cash App Pay SDK
     */
    Afterpay.beginCheckoutV3WithCashAppPay(
      consumer = createConsumer(),
      orderTotal = createOrderTotal(),
      items = createItems(),
      configuration = createCheckoutV3Configuration(),
    ).let { result: Result<CheckoutV3CashAppPay> ->
      result.onSuccess {
        /**
         * Step 4: Store [CheckoutV3CashAppPay] for use later and create Customer Request
         */
        checkoutV3CashAppPay = it
        createCashAppPayCustomerRequest(it)
      }

      result.onFailure { error ->
        val msg = "Failed to begin checkout"
        Log.e(tag, msg, error)
        showToastFromBackground(this, msg)
      }
    }
  }

  private fun createCashAppPayCustomerRequest(checkoutV3CashAppPay: CheckoutV3CashAppPay) {
    /**
     * Step 5: Create customer request with Cash App Pay SDK
     *
     * [redirectUri] defines where customer returns to after completing Cash App flow
     * You are not required to use [CheckoutV3CashAppPay.redirectUri] . You can set your own
     * value. Here in sample app we just redirect back to this Activity
     *
     * See intent filter in AndroidManifest.xml
     */
    val redirectUri = "example://example.com/"

    /**
     * Important: Afterpay SDK returns amount in dollars. We need to convert to cents before
     * passing to Cash App Pay SDK
     */
    val cents = (checkoutV3CashAppPay.amount * 100).toInt()

    val action = CashAppPayPaymentAction.OneTimeAction(
      currency = USD,
      amount = cents,
      scopeId = checkoutV3CashAppPay.brandId,
    )

    cashAppPay.createCustomerRequest(
      action,
      redirectUri = redirectUri,
    )
  }

  private suspend fun confirmCheckoutWithAfterpay(
    grantId: String,
    customerId: String,
  ) {
    requireNotNull(checkoutV3CashAppPay)
    checkoutV3CashAppPay?.let { it ->
      /**
       * Step 10: confirm checkout with Afterpay by combining newly received [grantId] and
       * [customerId] with previously-saved [CheckoutV3CashAppPay].
       */
      Afterpay.confirmCheckoutV3WithCashAppPay(
        grantId = grantId,
        customerId = customerId,
        token = it.token,
        singleUseCardToken = it.singleUseCardToken,
        jwt = it.jwt,
        configuration = createCheckoutV3Configuration(),
      ).let { result: Result<CheckoutV3Data> ->

        result.onSuccess {
          /**
           * Step 11: Receive single-use card details. Pass these back to your server
           * and on to your payment processor:
           */
          Log.d(tag, "Received card details, etc ${it.cardDetails}")
          it.cardDetails
          it.tokens
          it.cardValidUntil
        }

        result.onFailure {
          showToastFromBackground(
            this@CashAppV3SampleActivity,
            "Failed to confirm payment with Afterpay",
          )
        }
      }
    }
  }

  private val tag = CashAppV3SampleActivity::class.java.simpleName
}

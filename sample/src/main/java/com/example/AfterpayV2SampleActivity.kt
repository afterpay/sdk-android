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

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.afterpay.android.Afterpay
import com.afterpay.android.AfterpayCheckoutV2Handler
import com.afterpay.android.AfterpayCheckoutV2Options
import com.afterpay.android.model.ShippingAddress
import com.afterpay.android.model.ShippingOption
import com.afterpay.android.model.ShippingOptionUpdateResult
import com.afterpay.android.model.ShippingOptionsResult
import com.example.api.CheckoutMode
import com.example.api.GetConfigurationResponse
import com.example.api.GetTokenRequest
import com.example.api.merchantApi
import com.example.databinding.AfterpayV2LayoutBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Activity showing the Afterpay V2 checkout flow
 */
class AfterpayV2SampleActivity : AppCompatActivity() {
    private lateinit var bindings: AfterpayV2LayoutBinding

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result: ActivityResult ->
            result.data?.let { data ->
                /**
                 * Step 6: Afterpay flow will complete and return result to your app. If checkout was
                 * successful you will receive an order token which you pass back to your server
                 * for final processing.
                 */
                when (result.resultCode) {
                    Activity.RESULT_OK -> {
                        val orderToken = Afterpay.parseCheckoutSuccessResponse(data)
                        val message = "Checkout Complete, Received order token: $orderToken"
                        Log.d(tag, message)
                        showToast(this, message)
                    }

                    Activity.RESULT_CANCELED -> {
                        val status = Afterpay.parseCheckoutCancellationResponse(data)
                        showToast(this, "Checkout Cancelled: $status")
                    }
                }
            }
        }

    private val checkoutHandler =
        object : AfterpayCheckoutV2Handler {
            override fun didCommenceCheckout(onTokenLoaded: (Result<String>) -> Unit) {
                Log.d(tag, "didCommenceCheckout")
                /**
                 * Step 4: After starting Afterpay flow, Afterpay SDK will call back asking you to
                 * load your token. You will need to make another network request to your own server
                 * to fetch this token.
                 *
                 * You can fetch this token *before* customer clicks button and "have it ready".
                 * However you will need to re-request this token any time order amount changes.
                 * (e.g. customer adds items to cart) .
                 *
                 * Here in the sample app we request token from a sample merchant API / server.
                 * You must first have the sample server running:
                 *  https://github.com/afterpay/sdk-example-server
                 */
                CoroutineScope(Dispatchers.IO).launch {
                    Log.d(tag, "Getting token from merchant server")
                    merchantApi()
                        .getToken(
                            GetTokenRequest(
                                email = customerEmail,
                                amount = "12.00",
                                // Our example server uses the same endpoint to get configuration for
                                // both V1 and V2. Server calls them "standard" and "express" respectively.
                                mode = CheckoutMode.STANDARD,
                                isCashAppPay = false,
                            ),
                        )
                        /**
                         * Step 5: Pass that token back to Afterpay SDK via the supplied
                         * [onTokenLoaded] callback
                         */
                        .onSuccess { response ->
                            // Because the example server uses the same endpoint for both V1 and V2
                            // it returns more data than we need. Ideally your server would return
                            // only the token. (i.e. response.url is not needed here)
                            Log.d(tag, "Token received and loaded")
                            onTokenLoaded(Result.success(response.token))
                        }
                        .onFailure {
                            // If fetching token failed for any reason you need to tell Afterpay
                            // so it can correctly return to your app
                            val msg = "Failed to fetch token"
                            Log.e(tag, msg)
                            onTokenLoaded(Result.failure(Throwable(msg)))
                        }
                }
            }

            override fun shippingAddressDidChange(
                address: ShippingAddress,
                onProvideShippingOptions: (ShippingOptionsResult) -> Unit,
            ) {
                TODO("Not yet implemented")
            }

            override fun shippingOptionDidChange(
                shippingOption: ShippingOption,
                onProvideShippingOption: (ShippingOptionUpdateResult?) -> Unit,
            ) {
                TODO("Not yet implemented")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindings = AfterpayV2LayoutBinding.inflate(LayoutInflater.from(this))
        val view = bindings.root
        setContentView(view)

        bindings.afterpayButton.setOnClickListener {
            /**
             * Step 3: Once configuration is set, Afterpay SDK will enable the button, allowing
             * customer to start checkout flow.
             *
             * Create [AfterpayCheckoutV2Options], create an [android.content.Intent], and start flow.
             *
             * Here we use Android's ActivityResult APIs but you can use older [startActivityForResult]
             */
            val afterpayCheckoutV2Options =
                AfterpayCheckoutV2Options(
                    pickup = bindings.pickup.isChecked,
                    buyNow = bindings.buyNow.isChecked,
                    shippingOptionRequired = bindings.shippingOptionRequiredCheckbox.isChecked,
                    enableSingleShippingOptionUpdate = true,
                )

            val intent =
                Afterpay.createCheckoutV2Intent(
                    context = this@AfterpayV2SampleActivity,
                    options = afterpayCheckoutV2Options,
                )

            Log.d(tag, "Launching intent")
            activityResultLauncher.launch(intent)
        }

        /**
         * Step 0: Periodically check for new configurations from your own server. Doesn't need
         * to happen before *every* transaction. Does need to happen before *first* transaction
         * Use the tool of your choice to enable async non-main-thread network request
         * (i.e. coroutines, rxjava, retrofit)
         *
         * Here in the sample app we request configuration from a sample merchant API / server.
         * You must first have the sample server running:
         *  https://github.com/afterpay/sdk-example-server
         *
         * Using [lifecycleScope] is a quick-and-dirty example. Ideally you would not tie this network
         * request to an Activity's lifecycle.
         */
        lifecycleScope.launch {
            getConfiguration()
        }

        /**
         * Step 2: Set a handler which manages callbacks between your app and Afterpay SDK flow
         */
        Afterpay.setCheckoutV2Handler(checkoutHandler)
    }

    private fun getConfiguration() {
        CoroutineScope(Dispatchers.IO).launch {
            merchantApi().getConfiguration().apply {
                onFailure { error ->
                    val msg = "Failed to get configuration"
                    showToastFromBackground(this@AfterpayV2SampleActivity, msg)
                    Log.e(tag, msg, error)
                }

                onSuccess { response: GetConfigurationResponse ->
                    withContext(Dispatchers.Main) {
                        Log.d(tag, "Fetched and setting configs")
                        /**
                         * Step 1: Pass configuration to Afterpay
                         *
                         * It is up to you to save this Configuration (e.g. local storage) to avoid repeat calls
                         * to fetch them. You will need to pass Configuration to Afterpay on each app
                         * restart (before first transaction) by calling .setConfiguration()
                         *
                         * Once this function is called, Afterpay will enable the button.
                         */
                        Afterpay.setConfiguration(
                            minimumAmount = response.minimumAmount?.amount,
                            maximumAmount = response.maximumAmount.amount,
                            currencyCode = response.maximumAmount.currency,
                            locale =
                            Locale(response.locale.language, response.locale.country),
                            environment = AFTERPAY_ENVIRONMENT,
                        )
                    }
                }
            }
        }
    }
}

private val tag = AfterpayV2SampleActivity::class.java.simpleName

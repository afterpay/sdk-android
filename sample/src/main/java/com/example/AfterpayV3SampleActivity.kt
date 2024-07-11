package com.example

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.afterpay.android.Afterpay
import com.afterpay.android.CancellationStatusV3
import com.afterpay.android.model.CheckoutV3Configuration
import com.afterpay.android.model.Configuration
import com.example.databinding.AfterpayV3LayoutBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity showing the Afterpay V3 checkout flow
 */
class AfterpayV3SampleActivity : AppCompatActivity() {
    private lateinit var bindings: AfterpayV3LayoutBinding

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result: ActivityResult ->
            /**
             * Step 3: Respond to intent result and show error if [android.app.Activity.RESULT_CANCELED]
             * or signPayment with token if [android.app.Activity.RESULT_OK]
             */
            val intent = result.data
            checkNotNull(intent)
            if (result.resultCode == RESULT_OK) {
                Afterpay.parseCheckoutSuccessResponseV3(intent)?.let {
                    /**
                     * Step 4: Receive single-use card details. Pass these back to your server and on
                     * to your payment processor:
                     */
                    it.cardDetails
                    it.tokens
                    it.cardValidUntil

                    showToast(this, "Payment details received. Ready to process payment")
                }
            } else if (result.resultCode == RESULT_CANCELED) {
                Afterpay.parseCheckoutCancellationResponseV3(intent)
                    ?.let {
                            (
                                cancellationStatusV3: CancellationStatusV3,
                                exception: Exception?,
                            ),
                        ->
                        Log.e(tag, cancellationStatusV3.toString(), exception)
                    }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /**
         * The SDK is agnostic to the UI library of your choice: inflate XML, view binding, Compose, etc.
         * Here we use view binding to keep it simple.
         */
        bindings = AfterpayV3LayoutBinding.inflate(LayoutInflater.from(this))
        bindings.afterpayButton.apply {
            setOnClickListener { view ->
                /**
                 * Step 2: Once configuration is set, Afterpay SDK will automatically enable button,
                 * allowing customer to start checkout flow
                 *
                 * Create [CheckoutV3Configuration], create an [Intent], and start flow.
                 *
                 * Here we use Android's ActivityResult APIs but you can use older [startActivityForResult]
                 */
                val intent =
                    Afterpay.createCheckoutV3Intent(
                        context = view.context,
                        consumer = createConsumer(),
                        orderTotal = createOrderTotal(),
                        items = createItems(),
                        buyNow = true,
                        configuration = createCheckoutV3Configuration(),
                    )
                activityResultLauncher.launch(intent)
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
                    withContext(Dispatchers.Main) {
                        result.onSuccess { merchantConfiguration: Configuration ->
                            /**
                             * Step 1: Pass configuration to Afterpay
                             *
                             * It is up to you to save this Configuration (e.g. local storage) to avoid repeat calls
                             * to fetch them. You will need to pass Configuration to Afterpay on each app
                             * restart (before first transaction) by calling .setConfiguration()
                             *
                             * Only once this function is called, will Afterpay enable the button.
                             */
                            Log.d(tag, "Fetched merchant configs")
                            Afterpay.setConfigurationV3(merchantConfiguration)
                        }
                        result.onFailure {
                            val msg = "Failed to fetch merchant configs"
                            Log.e(tag, msg, it)
                            showToastFromBackground(this@AfterpayV3SampleActivity, msg)
                        }
                    }
                }
        }
}

private val tag = AfterpayV3SampleActivity::class.java.simpleName

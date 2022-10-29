package com.example.afterpay

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.createGraph
import androidx.navigation.findNavController
import androidx.navigation.fragment.fragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.afterpay.android.Afterpay
import com.afterpay.android.AfterpayEnvironment
import com.afterpay.android.model.AfterpayRegion
import com.afterpay.android.model.CheckoutV3Configuration
import com.afterpay.android.model.CheckoutV3Data
import com.example.afterpay.checkout.CheckoutFragment
import com.example.afterpay.data.AfterpayRepository
import com.example.afterpay.detailsv3.DetailsFragment
import com.example.afterpay.receipt.ReceiptFragment
import com.example.afterpay.shopping.ShoppingFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class MainActivity : AppCompatActivity() {
    private val afterpayRepository by lazy {
        AfterpayRepository(
            merchantApi = getDependencies().merchantApi,
            preferences = getDependencies().sharedPreferences,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        val navController = findNavController(R.id.nav_host_fragment).apply {
            graph = createGraph(NavGraph.id, NavGraph.dest.shopping) {
                fragment<ShoppingFragment>(NavGraph.dest.shopping) {
                    label = getString(R.string.title_shopping)
                    action(NavGraph.action.to_checkout) {
                        destinationId = NavGraph.dest.checkout
                    }
                }
                fragment<CheckoutFragment>(NavGraph.dest.checkout) {
                    label = getString(R.string.title_checkout)
                    argument(NavGraph.args.total_cost) {
                        type = NavType.ParcelableType(BigDecimal::class.java)
                    }
                    action(NavGraph.action.to_receipt) {
                        destinationId = NavGraph.dest.receipt
                    }
                    action(NavGraph.action.to_details_v3) {
                        destinationId = NavGraph.dest.details_v3
                    }
                }
                fragment<ReceiptFragment>(NavGraph.dest.receipt) {
                    label = getString(R.string.title_receipt)
                    argument(NavGraph.args.checkout_token) {
                        type = NavType.StringType
                    }
                    action(NavGraph.action.back_to_shopping) {
                        destinationId = NavGraph.dest.shopping
                        navOptions {
                            popUpTo(NavGraph.dest.shopping) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }
                fragment<DetailsFragment>(NavGraph.dest.details_v3) {
                    label = "Single Use Card"
                    argument(NavGraph.args.result_data_v3) {
                        type = NavType.ParcelableType(CheckoutV3Data::class.java)
                    }
                    action(NavGraph.action.back_to_shopping) {
                        destinationId = NavGraph.dest.shopping
                        navOptions {
                            popUpTo(NavGraph.dest.shopping) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }
            }
        }

        findViewById<Toolbar>(R.id.main_toolbar).apply {
            setupWithNavController(navController, AppBarConfiguration(navController.graph))
            setNavigationOnClickListener {
                onBackPressed()
            }

            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_refresh_config -> {
                        lifecycleScope.launch {
                            applyAfterpayConfiguration(forceRefresh = true)
                        }

                        true
                    }

                    else -> false
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            applyAfterpayConfiguration()
        }
    }

    private suspend fun applyAfterpayConfiguration(forceRefresh: Boolean = false) {
        try {
            val afterpayConfigV3 = CheckoutV3Configuration(
                shopDirectoryMerchantId = "822ce7ffc2fa41258904baad1d0fe07351e89375108949e8bd951d387ef0e932",
                region = AfterpayRegion.US,
                environment = AfterpayEnvironment.SANDBOX,
            )
            Afterpay.setCheckoutV3Configuration(afterpayConfigV3)

            val merchantConfig = withContext(Dispatchers.IO) {
                Afterpay.fetchMerchantConfigurationV3()
            }.getOrThrow()

            Afterpay.setConfigurationV3(merchantConfig)
        } catch (e: Exception) {
            Snackbar
                .make(
                    findViewById(android.R.id.content),
                    R.string.configuration_error_message,
                    Snackbar.LENGTH_INDEFINITE,
                )
                .setAction(R.string.configuration_error_action_retry) {
                    lifecycleScope.launch {
                        applyAfterpayConfiguration()
                    }
                }
                .show()
        }
    }
}

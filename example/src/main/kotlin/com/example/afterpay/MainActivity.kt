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
import com.example.afterpay.checkout.CheckoutFragment
import com.example.afterpay.data.AfterpayRepository
import com.example.afterpay.receipt.ReceiptFragment
import com.example.afterpay.shopping.ShoppingFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.Locale

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
            val configuration = withContext(Dispatchers.IO) {
                afterpayRepository.fetchConfiguration(forceRefresh)
            }

            Afterpay.setConfiguration(
                minimumAmount = configuration.minimumAmount,
                maximumAmount = configuration.maximumAmount,
                currencyCode = configuration.currency,
                locale = Locale(configuration.language, configuration.country),
                environment = AfterpayEnvironment.SANDBOX,
            )
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

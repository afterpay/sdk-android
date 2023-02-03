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
import app.cash.paykit.core.CashAppPayKit
import app.cash.paykit.core.CashAppPayKitFactory
import app.cash.paykit.core.CashAppPayKitListener
import app.cash.paykit.core.PayKitState
import com.afterpay.android.Afterpay
import com.afterpay.android.AfterpayEnvironment
import com.example.afterpay.checkout.CheckoutFragment
import com.example.afterpay.data.AfterpayRepository
import com.example.afterpay.data.CashResponseData
import com.example.afterpay.receipt.CashReceiptFragment
import com.example.afterpay.receipt.ReceiptFragment
import com.example.afterpay.shopping.ShoppingFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.Locale

class MainActivity : AppCompatActivity(), CashAppPayKitListener {
    private val afterpayRepository by lazy {
        AfterpayRepository(
            merchantApi = getDependencies().merchantApi,
            preferences = getDependencies().sharedPreferences,
        )
    }

    var payKit : CashAppPayKit? = null

    private fun setupPayKit() {
        Afterpay.environment?.let { env ->
            if (payKit != null) {
                payKit?.unregisterFromStateUpdates()
            }

            payKit = when (env) {
                AfterpayEnvironment.PRODUCTION -> CashAppPayKitFactory.create(env.payKitId)
                else -> CashAppPayKitFactory.createSandbox(env.payKitId)
            }

            payKit?.registerForStateUpdates(this)
        }
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
                    action(NavGraph.action.to_cash_receipt) {
                        destinationId = NavGraph.dest.cash_receipt
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
                fragment<CashReceiptFragment>(NavGraph.dest.cash_receipt) {
                    label = getString(R.string.title_cash_receipt)
                    argument(NavGraph.args.cash_response_data) {
                        type = NavType.ParcelableType(CashResponseData::class.java)
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

            setupPayKit()
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

    override fun payKitStateDidChange(newState: PayKitState) {
        MainCommands.commandChannel.trySend(MainCommands.Command.PayKitStateChange(newState))
    }
}

object MainCommands {
    sealed class Command {
        data class PayKitStateChange(val state: PayKitState): Command()
    }

    internal val commandChannel = Channel<Command>(Channel.CONFLATED)

    fun commands(): Flow<Command> = commandChannel.receiveAsFlow()
}

package com.example.afterpay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.paykit.core.CashAppPay
import app.cash.paykit.core.CashAppPayFactory
import app.cash.paykit.core.CashAppPayListener
import app.cash.paykit.core.CashAppPayState
import com.afterpay.android.Afterpay
import com.afterpay.android.AfterpayEnvironment
import com.afterpay.android.model.AfterpayRegion
import com.afterpay.android.model.CheckoutV3Configuration
import com.example.afterpay.data.AfterpayRepository
import com.example.afterpay.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel(), CashAppPayListener {

    private val _payKitState = MutableStateFlow<CashAppPayState>(CashAppPayState.NotStarted)
    val payKitState: StateFlow<CashAppPayState> = _payKitState.asStateFlow()

    var environment: AfterpayEnvironment = AfterpayEnvironment.SANDBOX
        private set

    private val afterpayRepository by lazy {
        AfterpayRepository(
            merchantApi = getDependencies().merchantApi,
            preferences = getDependencies().sharedPreferences,
        )
    }

    var payKit: CashAppPay? = null

    suspend fun applyAfterpayConfiguration(forceRefresh: Boolean = false) {
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
            Logger().error(message = "Failed to get afterpay configuration.", tr = e)
        }
    }

    fun setEnvironment(environment: AfterpayEnvironment) {
        this.environment = environment
        viewModelScope.launch {
            applyAfterpayConfiguration(true)
        }
    }

    override fun cashAppPayStateDidChange(newState: CashAppPayState) {
        _payKitState.value = newState
        MainCommands.commandChannel.trySend(MainCommands.Command.PayKitStateChange(newState))
    }

    private fun setupPayKit() {
        Afterpay.environment?.let { env ->
            if (payKit != null) {
                payKit?.unregisterFromStateUpdates()
            }

            payKit = when (env) {
                AfterpayEnvironment.PRODUCTION -> CashAppPayFactory.create(env.payKitClientId)
                else -> CashAppPayFactory.createSandbox(env.payKitClientId)
            }

            payKit?.registerForStateUpdates(this)
        }
    }
}

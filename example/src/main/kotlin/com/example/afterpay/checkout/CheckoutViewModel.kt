package com.example.afterpay.checkout

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.util.Patterns
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.paykit.core.CashAppPayKit
import app.cash.paykit.core.models.response.CustomerResponseData
import app.cash.paykit.core.models.sdk.PayKitCurrency
import app.cash.paykit.core.models.sdk.PayKitPaymentAction
import com.afterpay.android.AfterpayCheckoutV2Options
import com.afterpay.android.cashapp.AfterpayCashApp
import com.afterpay.android.model.Money
import com.afterpay.android.model.ShippingAddress
import com.afterpay.android.model.ShippingOption
import com.afterpay.android.model.ShippingOptionUpdate
import com.afterpay.android.model.ShippingOptionUpdateResult
import com.afterpay.android.model.ShippingOptionUpdateSuccessResult
import com.afterpay.android.model.ShippingOptionsResult
import com.afterpay.android.model.ShippingOptionsSuccessResult
import com.example.afterpay.data.AfterpayRepository
import com.example.afterpay.data.CheckoutMode
import com.example.afterpay.data.CheckoutRequest
import com.example.afterpay.data.MerchantApi
import com.example.afterpay.getDependencies
import com.example.afterpay.util.asCurrency
import com.example.afterpay.util.getBuyNow
import com.example.afterpay.util.getEmail
import com.example.afterpay.util.getExpress
import com.example.afterpay.util.getPickup
import com.example.afterpay.util.getShippingOptionsRequired
import com.example.afterpay.util.getVersion
import com.example.afterpay.util.putBuyNow
import com.example.afterpay.util.putEmail
import com.example.afterpay.util.putExpress
import com.example.afterpay.util.putPickup
import com.example.afterpay.util.putShippingOptionsRequired
import com.example.afterpay.util.putVersion
import com.example.afterpay.util.update
import com.example.afterpay.util.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Currency
import java.util.Locale

class CheckoutViewModel(
    totalCost: BigDecimal,
    private val merchantApi: MerchantApi,
    private val preferences: SharedPreferences,
) : ViewModel() {
    data class State(
        val emailAddress: String,
        val total: BigDecimal,
        val useV1: Boolean,
        val express: Boolean,
        val buyNow: Boolean,
        val pickup: Boolean,
        val shippingOptionsRequired: Boolean,
    ) {
        val totalCost: String
            get() = total.asCurrency()

        val enableCheckoutButton: Boolean
            get() = Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()
    }

    sealed class Command {
        data class ShowAfterpayCheckoutV1(val checkoutUrl: String) : Command()
        data class ShowAfterpayCheckoutV2(val options: AfterpayCheckoutV2Options) : Command()
        data class LaunchCashAppPay(val token: String?) : Command()
        data class ProvideCheckoutTokenResult(val tokenResult: Result<String>) : Command()
        data class SignCashAppOrder(val tokenResult: Result<String>) : Command()
        data class ProvideShippingOptionsResult(val shippingOptionsResult: ShippingOptionsResult) :
            Command()
        data class ProvideShippingOptionUpdateResult(
            val shippingOptionUpdateResult: ShippingOptionUpdateResult?,
        ) : Command()
        data class CashReceipt(val customerResponseData: CustomerResponseData) : Command()
    }

    fun createCustomerRequest(cashAppData: AfterpayCashApp, payKitInstance: CashAppPayKit?) {
        if (payKitInstance != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val request = PayKitPaymentAction.OneTimeAction(
                    redirectUri = "aftersnack://callback",
                    currency = PayKitCurrency.USD,
                    amount = (cashAppData.amount * 100).toInt(),
                    scopeId = cashAppData.merchantId,
                )

                payKitInstance.createCustomerRequest(request)
            }
        }
    }

    fun authorizePayKitCustomerRequest(context: Context, payKitInstance: CashAppPayKit?) {
        payKitInstance?.authorizeCustomerRequest(context)
    }

    private val state = MutableStateFlow(
        State(
            emailAddress = preferences.getEmail(),
            total = totalCost,
            useV1 = preferences.getVersion(),
            express = preferences.getExpress(),
            buyNow = preferences.getBuyNow(),
            pickup = preferences.getPickup(),
            shippingOptionsRequired = preferences.getShippingOptionsRequired(),
        ),
    )
    private val commandChannel = Channel<Command>(Channel.CONFLATED)

    fun state(): Flow<State> = state

    fun commands(): Flow<Command> = commandChannel.receiveAsFlow()

    fun enterEmailAddress(email: String) = state.update { copy(emailAddress = email) }

    fun checkExpress(checked: Boolean) = state.update { copy(express = checked) }

    fun checkVersion(checked: Boolean) = state.update { copy(useV1 = checked) }

    fun checkBuyNow(checked: Boolean) = state.update { copy(buyNow = checked) }

    fun checkPickup(checked: Boolean) = state.update { copy(pickup = checked) }

    fun checkShippingOptionsRequired(checked: Boolean) = state.update {
        copy(shippingOptionsRequired = checked)
    }

    fun showAfterpayCheckout(cashAppPay: Boolean = false) {
        val (
            email,
            total,
            useV1,
            isExpress,
            isBuyNow,
            isPickup,
            isShippingOptionsRequired,
        ) = state.value

        preferences.edit {
            putEmail(email)
            putVersion(useV1)
            putExpress(isExpress)
            putBuyNow(isBuyNow)
            putPickup(isPickup)
            putShippingOptionsRequired(isShippingOptionsRequired)
        }

        when {
            cashAppPay -> {
                viewModelScope.launch {
                    commandChannel.trySend(Command.LaunchCashAppPay(null))
                }
            }
            useV1 -> {
                viewModelScope.launch {
                    try {
                        val symbols = DecimalFormatSymbols(Locale.US)
                        val formatter = DecimalFormat("#,###.00", symbols)
                        val response = withContext(Dispatchers.IO) {
                            merchantApi.checkout(
                                CheckoutRequest(
                                    email = email,
                                    amount = formatter.format(total),
                                    mode = CheckoutMode.STANDARD,
                                ),
                            )
                        }
                        commandChannel.trySend(Command.ShowAfterpayCheckoutV1(response.url))
                    } catch (error: Exception) {
                        val message = error.message ?: "Failed to fetch checkout url"
                        Log.e("ExampleError", "useV1: $message")
                    }
                }
            }
            else -> {
                val options = AfterpayCheckoutV2Options(
                    isPickup,
                    isBuyNow,
                    isShippingOptionsRequired,
                    enableSingleShippingOptionUpdate = true,
                )
                commandChannel.trySend(Command.ShowAfterpayCheckoutV2(options))
            }
        }
    }

    fun loadCheckoutToken(isCashApp: Boolean = false) {
        val (email, total, _, isExpress) = state.value
        val symbols = DecimalFormatSymbols(Locale.US)
        val amount = DecimalFormat("0.00", symbols).format(total)
        val mode = if (isExpress && !isCashApp) CheckoutMode.EXPRESS else CheckoutMode.STANDARD

        viewModelScope.launch {
            val request = CheckoutRequest(email, amount, mode, isCashApp)
            val response = runCatching {
                withContext(Dispatchers.IO) { merchantApi.checkout(request) }
            }
            val tokenResult = response.map { it.token }
            val command = when (isCashApp) {
                true -> Command.SignCashAppOrder(tokenResult)
                else -> Command.ProvideCheckoutTokenResult(tokenResult)
            }
            commandChannel.trySend(command)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun selectAddress(address: ShippingAddress) {
        viewModelScope.launch {
            val configuration = withContext(Dispatchers.IO) {
                getDependencies()
                    .run { AfterpayRepository(merchantApi, sharedPreferences) }
                    .fetchConfiguration()
            }

            val currency = Currency.getInstance(configuration.currency)

            val shippingOptions = listOf(
                ShippingOption(
                    "standard",
                    "Standard",
                    "",
                    Money("0.00".toBigDecimal(), currency),
                    Money("50.00".toBigDecimal(), currency),
                    Money("0.00".toBigDecimal(), currency),
                ),
                ShippingOption(
                    "priority",
                    "Priority",
                    "Next business day",
                    Money("10.00".toBigDecimal(), currency),
                    Money("60.00".toBigDecimal(), currency),
                    null,
                ),
            )

            val result = ShippingOptionsSuccessResult(shippingOptions)
            commandChannel.trySend(Command.ProvideShippingOptionsResult(result))
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun selectShippingOption(shippingOption: ShippingOption) {
        viewModelScope.launch {
            val configuration = withContext(Dispatchers.IO) {
                getDependencies()
                    .run { AfterpayRepository(merchantApi, sharedPreferences) }
                    .fetchConfiguration()
            }

            val currency = Currency.getInstance(configuration.currency)
            val result: ShippingOptionUpdateResult?

            // if standard shipping was selected, update the amounts
            // otherwise leave as is by passing null
            if (shippingOption.id == "standard") {
                val updatedShippingOption = ShippingOptionUpdate(
                    "standard",
                    Money("0.00".toBigDecimal(), currency),
                    Money("50.00".toBigDecimal(), currency),
                    Money("2.00".toBigDecimal(), currency),
                )

                result = ShippingOptionUpdateSuccessResult(updatedShippingOption)
            } else {
                result = null
            }

            commandChannel.trySend(Command.ProvideShippingOptionUpdateResult(result))
        }
    }

    fun cashReceipt(customerResponseData: CustomerResponseData) {
        viewModelScope.launch {
            commandChannel.trySend(Command.CashReceipt(customerResponseData = customerResponseData)).isSuccess
        }
    }

    companion object {
        fun factory(
            totalCost: BigDecimal,
            merchantApi: MerchantApi,
            preferences: SharedPreferences,
        ) = viewModelFactory {
            CheckoutViewModel(
                totalCost = totalCost,
                merchantApi = merchantApi,
                preferences = preferences,
            )
        }
    }
}

package com.example.afterpay.checkout

import android.content.SharedPreferences
import android.util.Patterns
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afterpay.android.AfterpayCheckoutV2Options
import com.afterpay.android.model.ShippingAddress
import com.afterpay.android.model.ShippingOption
import com.example.afterpay.data.CheckoutMode
import com.example.afterpay.data.CheckoutRequest
import com.example.afterpay.data.MerchantApi
import com.example.afterpay.util.asCurrency
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

class CheckoutViewModel(
    totalCost: BigDecimal,
    private val merchantApi: MerchantApi,
    private val preferences: SharedPreferences
) : ViewModel() {
    data class State(
        val emailAddress: String,
        val total: BigDecimal,
        val express: Boolean,
        val buyNow: Boolean,
        val pickup: Boolean,
        val shippingOptionsRequired: Boolean
    ) {
        val totalCost: String
            get() = total.asCurrency()

        val enableCheckoutButton: Boolean
            get() = Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()
    }

    sealed class Command {
        data class ShowAfterpayCheckout(val options: AfterpayCheckoutV2Options) : Command()
        data class ProvideCheckoutTokenResult(val tokenResult: Result<String>) : Command()
        data class ProvideShippingOptions(val shippingOptions: List<ShippingOption>): Command()
    }

    private val state = MutableStateFlow(
        State(
            emailAddress = preferences.getEmail(),
            total = totalCost,
            express = preferences.getExpress(),
            buyNow = preferences.getBuyNow(),
            pickup = preferences.getPickup(),
            shippingOptionsRequired = preferences.getShippingOptionsRequired()
        )
    )
    private val commandChannel = Channel<Command>(Channel.CONFLATED)

    fun state(): Flow<State> = state

    fun commands(): Flow<Command> = commandChannel.receiveAsFlow()

    fun enterEmailAddress(email: String) = state.update { copy(emailAddress = email) }

    fun checkExpress(checked: Boolean) = state.update { copy(express = checked) }

    fun checkBuyNow(checked: Boolean) = state.update { copy(buyNow = checked) }

    fun checkPickup(checked: Boolean) = state.update { copy(pickup = checked) }

    fun checkShippingOptionsRequired(checked: Boolean) = state.update {
        copy(shippingOptionsRequired = checked)
    }

    fun showAfterpayCheckout() {
        val (email, _, isExpress, isBuyNow, isPickup, isShippingOptionsRequired) = state.value

        preferences.edit {
            putEmail(email)
            putExpress(isExpress)
            putBuyNow(isBuyNow)
            putPickup(isPickup)
            putShippingOptionsRequired(isShippingOptionsRequired)
        }

        val options = AfterpayCheckoutV2Options(isPickup, isBuyNow, isShippingOptionsRequired)
        commandChannel.offer(Command.ShowAfterpayCheckout(options))
    }

    fun loadCheckoutToken() {
        val (email, total, isExpress) = state.value
        val amount = DecimalFormat("0.00").format(total)
        val mode = if (isExpress) CheckoutMode.EXPRESS else CheckoutMode.STANDARD

        viewModelScope.launch {
            val request = CheckoutRequest(email, amount, mode)
            val response = runCatching {
                withContext(Dispatchers.IO) { merchantApi.checkout(request) }
            }
            val tokenResult = response.map { it.token }
            val command = Command.ProvideCheckoutTokenResult(tokenResult)
            commandChannel.offer(command)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun selectAddress(address: ShippingAddress) {
        val shippingOptions = listOf(
            ShippingOption(
                "standard",
                "Standard",
                "",
                ShippingOption.Money("0.00", "AUD"),
                ShippingOption.Money("50.00", "AUD"),
                null
            ),
            ShippingOption(
                "priority",
                "Priority",
                "Next business day",
                ShippingOption.Money("10.00", "AUD"),
                ShippingOption.Money("60.00", "AUD"),
                null
            )
        )

        commandChannel.offer(Command.ProvideShippingOptions(shippingOptions))
    }

    companion object {
        fun factory(
            totalCost: BigDecimal,
            merchantApi: MerchantApi,
            preferences: SharedPreferences
        ) = viewModelFactory {
            CheckoutViewModel(
                totalCost = totalCost,
                merchantApi = merchantApi,
                preferences = preferences
            )
        }
    }
}

private object PreferenceKey {
    const val email = "email"
    const val express = "express"
    const val buyNow = "buyNow"
    const val pickup = "pickup"
    const val shippingOptionsRequired = "shippingOptionsRequired"
}

private fun SharedPreferences.getEmail(): String = getString(PreferenceKey.email, null) ?: ""
private fun SharedPreferences.Editor.putEmail(email: String) = putString(PreferenceKey.email, email)

private fun SharedPreferences.getExpress(): Boolean = getBoolean(PreferenceKey.express, false)
private fun SharedPreferences.Editor.putExpress(isExpress: Boolean) = putBoolean(PreferenceKey.express, isExpress)

private fun SharedPreferences.getBuyNow(): Boolean = getBoolean(PreferenceKey.buyNow, false)
private fun SharedPreferences.Editor.putBuyNow(isBuyNow: Boolean) = putBoolean(PreferenceKey.buyNow, isBuyNow)

private fun SharedPreferences.getPickup(): Boolean = getBoolean(PreferenceKey.pickup, false)
private fun SharedPreferences.Editor.putPickup(isPickup: Boolean) = putBoolean(PreferenceKey.pickup, isPickup)

private fun SharedPreferences.getShippingOptionsRequired(): Boolean = getBoolean(PreferenceKey.shippingOptionsRequired, true)
private fun SharedPreferences.Editor.putShippingOptionsRequired(isShippingOptionsRequired: Boolean) =
    putBoolean(PreferenceKey.shippingOptionsRequired, isShippingOptionsRequired)

package com.example.afterpay.checkout

import android.content.SharedPreferences
import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
        val pickup: Boolean
    ) {
        val totalCost: String
            get() = total.asCurrency()

        val enableCheckoutButton: Boolean
            get() = Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()
    }

    sealed class Command {
        data class DisplayCheckout(val checkoutUrl: String) : Command()
        data class DisplayError(val checkoutError: Throwable) : Command()
        data class DisplayShippingOptions(val shippingOptions: List<ShippingOption>): Command()
    }

    private val state = MutableStateFlow(
        State(emailAddress = "", total = totalCost, express = false, buyNow = false, pickup = false)
    )
    private val commandChannel = Channel<Command>(Channel.CONFLATED)

    fun state(): Flow<State> = state

    fun commands(): Flow<Command> = commandChannel.receiveAsFlow()

    fun enterEmailAddress(email: String) = state.update { copy(emailAddress = email) }

    fun checkExpress(checked: Boolean) = state.update { copy(express = checked) }

    fun checkBuyNow(checked: Boolean) = state.update { copy(buyNow = checked) }

    fun checkPickup(checked: Boolean) = state.update { copy(pickup = checked) }

    fun loadCheckout() {
        val (email, total, isExpress, isBuyNow, isPickup) = state.value
        val amount = DecimalFormat("0.00").format(total)
        val mode = if (isExpress) CheckoutMode.EXPRESS else CheckoutMode.STANDARD

        viewModelScope.launch {
            try {
                val request = CheckoutRequest(email, amount, mode)
                val response = withContext(Dispatchers.IO) {  merchantApi.checkout(request) }

                val uri = Uri
                    .parse(response.url)
                    .buildUpon()
                    .appendQueryParameter("buyNow", isBuyNow.toString())
                    .appendQueryParameter("pickup", isPickup.toString())
                    .build()

                commandChannel.offer(Command.DisplayCheckout(uri.toString()))
            } catch (error: Exception) {
                commandChannel.offer(Command.DisplayError(error))
            }
        }
    }

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

        commandChannel.offer(Command.DisplayShippingOptions(shippingOptions))
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

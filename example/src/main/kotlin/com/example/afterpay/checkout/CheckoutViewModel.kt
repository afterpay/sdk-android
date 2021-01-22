package com.example.afterpay.checkout

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afterpay.android.model.ShippingAddress
import com.afterpay.android.model.ShippingOption
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
    private val merchantApi: MerchantApi
) : ViewModel() {
    data class State(
        val emailAddress: String,
        val total: BigDecimal
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
        State(emailAddress = "", total = totalCost)
    )
    private val commandChannel = Channel<Command>(Channel.CONFLATED)

    fun state(): Flow<State> = state

    fun commands(): Flow<Command> = commandChannel.receiveAsFlow()

    fun enterEmailAddress(email: String) {
        state.update { copy(emailAddress = email) }
    }

    fun loadCheckout() {
        val (email, total) = state.value
        val amount = DecimalFormat("0.00").format(total)

        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    merchantApi.checkout(CheckoutRequest(email, amount))
                }
                commandChannel.offer(Command.DisplayCheckout(response.url))
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
        fun factory(totalCost: BigDecimal, merchantApi: MerchantApi) = viewModelFactory {
            CheckoutViewModel(totalCost = totalCost, merchantApi = merchantApi)
        }
    }
}

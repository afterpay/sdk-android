package com.example.afterpay.checkout

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afterpay.data.CheckoutRequest
import com.example.afterpay.data.MerchantApi
import com.example.afterpay.util.asCurrency
import com.example.afterpay.util.update
import com.example.afterpay.util.viewModelFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.math.BigDecimal
import java.text.DecimalFormat

class CheckoutViewModel(
    totalCost: BigDecimal,
    private val merchantApi: MerchantApi
) : ViewModel() {
    data class State(
        val emailAddress: String,
        val total: BigDecimal,
        private val isLoading: Boolean
    ) {
        val totalCost: String
            get() = total.asCurrency()

        val showProgressBar: Boolean
            get() = isLoading

        val enableCheckoutButton: Boolean
            get() = !isLoading && Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()
    }

    sealed class Command {
        data class StartAfterpayCheckout(val url: String) : Command()
        data class ApplyAfterpayConfiguration(val configuration: Configuration) : Command()
        data class DisplayError(val message: String) : Command()
    }

    data class Configuration(
        val minimumAmount: String?,
        val maximumAmount: String,
        val currency: String
    )

    private val state = MutableStateFlow(
        State(emailAddress = "", total = totalCost, isLoading = false)
    )
    private val commandChannel = Channel<Command>(Channel.CONFLATED)

    fun state(): Flow<State> = state

    fun commands(): Flow<Command> = commandChannel.receiveAsFlow()

    fun enterEmailAddress(email: String) {
        state.update { copy(emailAddress = email) }
    }

    fun fetchConfiguration() {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    merchantApi.configuration()
                }
                val configuration = Configuration(
                    minimumAmount = response.minimumAmount?.amount,
                    maximumAmount = response.maximumAmount.amount,
                    currency = response.maximumAmount.currency
                )
                commandChannel.offer(Command.ApplyAfterpayConfiguration(configuration))
            } catch (error: Exception) {
                val message = error.message ?: "Failed to fetch configuration"
                commandChannel.offer(Command.DisplayError(message))
            }
        }
    }

    fun checkoutWithAfterpay() {
        val (email, total) = state.value
        val amount = DecimalFormat("0.00").format(total)

        viewModelScope.launch {
            state.update { copy(isLoading = true) }

            try {
                val response = withContext(Dispatchers.IO) {
                    merchantApi.checkout(CheckoutRequest(email, amount))
                }
                commandChannel.offer(Command.StartAfterpayCheckout(response.url))
            } catch (error: Exception) {
                val message = error.message ?: "Failed to fetch checkout url"
                commandChannel.offer(Command.DisplayError(message))
            }

            state.update { copy(isLoading = false) }
        }
    }

    companion object {
        fun factory(totalCost: BigDecimal) = viewModelFactory {
            CheckoutViewModel(
                totalCost = totalCost,
                merchantApi = Retrofit.Builder()
                    .baseUrl("https://10.0.2.2:3001")
                    .addConverterFactory(
                        MoshiConverterFactory.create(
                            Moshi.Builder()
                                .add(KotlinJsonAdapterFactory())
                                .build()
                        )
                    )
                    .build()
                    .create(MerchantApi::class.java)
            )
        }
    }
}

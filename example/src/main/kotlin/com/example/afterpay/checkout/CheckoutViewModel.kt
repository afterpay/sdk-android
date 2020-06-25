package com.example.afterpay.checkout

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afterpay.data.MerchantApi
import com.example.afterpay.data.MerchantCheckoutRequest
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

class CheckoutViewModel(
    totalCost: BigDecimal,
    private val merchantApi: MerchantApi
) : ViewModel() {
    data class State(
        val emailAddress: String,
        private val total: BigDecimal,
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
        data class DisplayError(val message: String) : Command()
    }

    private val state = MutableStateFlow(
        State(emailAddress = "", total = totalCost, isLoading = false)
    )
    private val commandChannel = Channel<Command>(Channel.CONFLATED)

    fun state(): Flow<State> = state

    fun commands(): Flow<Command> = commandChannel.receiveAsFlow()

    fun enterEmailAddress(email: String) {
        state.update { copy(emailAddress = email) }
    }

    fun checkoutWithAfterpay() {
        val email = state.value.emailAddress

        viewModelScope.launch {
            state.update { copy(isLoading = true) }

            try {
                val response = withContext(Dispatchers.IO) {
                    merchantApi.checkout(MerchantCheckoutRequest(email))
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
                    .baseUrl("http://10.0.2.2:3000")
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

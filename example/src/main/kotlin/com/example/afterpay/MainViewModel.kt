package com.example.afterpay

import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.example.afterpay.data.MerchantApi
import com.example.afterpay.data.MerchantCheckoutRequest
import com.example.afterpay.util.update
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainViewModel(private val merchantApi: MerchantApi) : ViewModel() {
    data class State(
        val emailAddress: String,
        private val isLoading: Boolean
    ) {
        val showProgressBar: Boolean
            get() = isLoading

        val enableCheckoutButton: Boolean
            get() = !isLoading && Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()
    }

    sealed class Command {
        data class StartAfterpayCheckout(val url: String) : Command()
        data class DisplayError(val message: String) : Command()
    }

    private val state = MutableLiveData(State(emailAddress = "", isLoading = false))
    private val commandChannel = Channel<Command>(Channel.Factory.CONFLATED)

    fun state(): Flow<State> = state.asFlow()

    fun commands(): Flow<Command> = commandChannel.receiveAsFlow()

    fun enterEmailAddress(email: String) {
        state.update { copy(emailAddress = email) }
    }

    fun checkoutWithAfterpay() {
        val email = state.value?.emailAddress
        if (email == null) {
            commandChannel.offer(Command.DisplayError("No email address provided"))
            return
        }

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
}

class MainViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            MainViewModel(
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
            ) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
}

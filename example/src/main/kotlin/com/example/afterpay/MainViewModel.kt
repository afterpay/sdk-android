package com.example.afterpay

import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.example.afterpay.data.MerchantApi
import com.example.afterpay.data.MerchantCheckoutRequest
import com.example.afterpay.util.combineWith
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
        val canSubmit: Boolean,
        val isLoading: Boolean
    )

    sealed class Event {
        data class StartAfterpayCheckout(val url: String) : Event()
        data class CheckoutFailed(val message: String) : Event()
    }

    private val emailAddress = MutableLiveData("")
    private val isLoading = MutableLiveData(false)
    private val eventsChannel = Channel<Event>(Channel.Factory.CONFLATED)

    fun events(): Flow<Event> = eventsChannel.receiveAsFlow()

    fun state(): Flow<State> = emailAddress
        .combineWith(isLoading) { email, isLoading ->
            if (isLoading == true) {
                State(canSubmit = false, isLoading = true)
            } else {
                State(
                    canSubmit = email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches(),
                    isLoading = false
                )
            }
        }
        .asFlow()

    fun enterEmailAddress(email: String) {
        emailAddress.value = email
    }

    fun checkoutWithAfterpay() {
        val email = emailAddress.value
        if (email == null) {
            eventsChannel.offer(Event.CheckoutFailed("No email address provided"))
            return
        }

        viewModelScope.launch {
            isLoading.value = true

            try {
                val response = withContext(Dispatchers.IO) {
                    merchantApi.checkout(MerchantCheckoutRequest(email))
                }
                eventsChannel.offer(Event.StartAfterpayCheckout(response.url))
            } catch (error: Exception) {
                val message = error.message ?: "Failed to fetch checkout url"
                eventsChannel.offer(Event.CheckoutFailed(message))
            }

            isLoading.value = false
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

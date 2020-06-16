package com.example.afterpay

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.afterpay.data.MerchantApi
import com.example.afterpay.data.MerchantCheckoutRequest
import com.example.afterpay.data.Result
import com.example.afterpay.util.combineWith
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.lang.Exception

class MainViewModel(private val merchantApi: MerchantApi) : ViewModel() {
    val checkoutRequest: LiveData<Result<String>>
    val canSubmit: LiveData<Boolean>

    private val emailAddress = MutableLiveData("")
    private val request = MutableLiveData<Result<String>>(Result.Idle)

    init {
        canSubmit = emailAddress.combineWith(request) { email, request ->
            if (request is Result.Loading) {
                false
            } else {
                email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }
        }
        checkoutRequest = request
    }

    fun enterEmailAddress(email: String) {
        emailAddress.value = email
    }

    fun checkoutWithAfterpay() {
        val email = emailAddress.value ?: return
        if (canSubmit.value != true) {
            return
        }

        viewModelScope.launch {
            request.value = Result.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    merchantApi.checkout(MerchantCheckoutRequest(email))
                }
                request.value = Result.Success(response.url)
            } catch (error: Exception) {
                request.value = Result.Failure(error.message ?: "Failed to fetch checkout url.")
            }
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
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()
                    .create(MerchantApi::class.java)
            ) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
}

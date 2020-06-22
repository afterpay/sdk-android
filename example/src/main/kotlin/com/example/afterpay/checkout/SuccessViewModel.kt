package com.example.afterpay.checkout

import androidx.lifecycle.ViewModel
import com.example.afterpay.util.viewModelFactory

class SuccessViewModel(private val token: String) : ViewModel() {
    val message: String get() = "Checkout successful with token:\n$token"

    companion object {
        fun factory(token: String) = viewModelFactory {
            SuccessViewModel(token)
        }
    }
}

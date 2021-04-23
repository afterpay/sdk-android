package com.example.afterpay.receipt

import androidx.lifecycle.ViewModel
import com.example.afterpay.util.viewModelFactory

class ReceiptViewModel(val token: String) : ViewModel() {

    companion object {
        fun factory(token: String) = viewModelFactory {
            ReceiptViewModel(token)
        }
    }
}

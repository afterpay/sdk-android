package com.example.afterpay.receipt

import androidx.lifecycle.ViewModel
import com.example.afterpay.util.viewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.math.BigDecimal

class ReceiptViewModel(val token: String) : ViewModel() {

    private val totalCost = MutableSharedFlow<BigDecimal>(replay = 1)

    fun totalCost(): Flow<BigDecimal> = totalCost

    fun onTotalCost(cost: BigDecimal) {
        totalCost.tryEmit(cost)
    }

    companion object {
        fun factory(token: String) = viewModelFactory {
            ReceiptViewModel(token)
        }
    }
}

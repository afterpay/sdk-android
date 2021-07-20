package com.example.afterpay.detailsv3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afterpay.android.Afterpay
import com.afterpay.android.model.CheckoutV3Data
import com.example.afterpay.util.viewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID

class DetailsViewModel(private val resultData: CheckoutV3Data) : ViewModel() {

    private val merchantReferenceUpdate = MutableSharedFlow<String>(replay = 1)

    fun merchantReference(): Flow<String> = merchantReferenceUpdate

    private fun onMerchantReference(reference: String) {
        merchantReferenceUpdate.tryEmit(reference)
    }

    fun updateMerchantReference() {
        val reference = UUID.randomUUID().toString()
        viewModelScope.launch {
            val result = Afterpay.updateMerchantReferenceV3(reference, resultData.tokens)
            result.fold(
                onSuccess = { onMerchantReference(reference) },
                onFailure = { onMerchantReference("Update failed!") }
            )
        }
    }

    companion object {
        fun factory(resultData: CheckoutV3Data) = viewModelFactory {
            DetailsViewModel(resultData)
        }
    }
}

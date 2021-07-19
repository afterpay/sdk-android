package com.afterpay.android.internal

import com.afterpay.android.model.CheckoutV3Tokens
import kotlinx.serialization.Serializable

object CheckoutV3 {
    @Serializable
    data class MerchantReferenceUpdate(
        val merchantReference: String,
        val token: String,
        val singleUseCardToken: String,
        val ppaConfirmToken: String
    )
}

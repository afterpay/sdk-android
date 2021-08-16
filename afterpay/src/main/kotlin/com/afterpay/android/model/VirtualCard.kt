package com.afterpay.android.model

import com.afterpay.android.internal.VirtualCardSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable(with = VirtualCardSerializer::class)
sealed class VirtualCard {
    @Serializable
    data class Card(
        val cardType: String,
        val cardNumber: String?,
        val cvc: String,
        private val expiry: String,
        @Transient var expiryYear: Int = -1,
        @Transient var expiryMonth: Int = -1
    ) : VirtualCard() {
        init {
            val components = expiry.split("-").map { it.toInt() }
            expiryYear = components[0]
            expiryMonth = components[1]
        }
    }

    @Serializable
    data class TokenizedCard(
        val paymentGateway: String,
        val cardToken: String?,
        private val expiry: String,
        @Transient var expiryYear: Int = -1,
        @Transient var expiryMonth: Int = -1
    ) : VirtualCard() {
        init {
            val components = expiry.split("-").map { it.toInt() }
            expiryYear = components[0]
            expiryMonth = components[1]
        }
    }
}

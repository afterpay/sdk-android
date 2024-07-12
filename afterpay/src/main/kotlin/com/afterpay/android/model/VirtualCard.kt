/*
 * Copyright (C) 2024 Afterpay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        @Transient var expiryMonth: Int = -1,
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
        @Transient var expiryMonth: Int = -1,
    ) : VirtualCard() {
        init {
            val components = expiry.split("-").map { it.toInt() }
            expiryYear = components[0]
            expiryMonth = components[1]
        }
    }
}

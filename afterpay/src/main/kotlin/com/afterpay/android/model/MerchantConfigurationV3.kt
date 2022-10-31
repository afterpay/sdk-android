package com.afterpay.android.model

import kotlinx.serialization.Serializable

@Serializable
data class MerchantConfigurationV3(
    val minimumAmount: Money,
    val maximumAmount: Money,
)

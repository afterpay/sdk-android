package com.afterpay.android

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AfterpayCheckoutV2Options(
    val pickup: Boolean? = null,
    val buyNow: Boolean? = null,
    val shippingOptionRequired: Boolean? = null
) : Parcelable

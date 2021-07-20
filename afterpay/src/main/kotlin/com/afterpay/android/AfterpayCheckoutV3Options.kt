package com.afterpay.android

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.net.URL

@Parcelize
data class AfterpayCheckoutV3Options(
    val buyNow: Boolean? = null,
    val checkoutPayload: String? = null,
    val token: String? = null,
    val ppaConfirmToken: String? = null,
    val singleUseCardToken: String? = null,
    val checkoutUrl: URL? = null,
    val redirectUrl: URL? = null,
    val confirmUrl: URL? = null,
) : Parcelable

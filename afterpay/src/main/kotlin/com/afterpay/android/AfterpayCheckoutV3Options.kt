package com.afterpay.android

import android.os.Parcel
import android.os.Parcelable
import java.net.URL

data class AfterpayCheckoutV3Options(
    val buyNow: Boolean? = null,
    val checkoutPayload: String? = null,
    var token: String? = null,
    var ppaConfirmToken: String? = null,
    var singleUseCardToken: String? = null,
    val checkoutUrl: URL? = null,
    var redirectUrl: URL? = null,
    val confirmUrl: URL? = null,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readValue(String::class.java.classLoader) as? String,
        parcel.readValue(String::class.java.classLoader) as? String,
        parcel.readValue(String::class.java.classLoader) as? String,
        parcel.readValue(String::class.java.classLoader) as? String,
        parcel.readValue(URL::class.java.classLoader) as? URL,
        parcel.readValue(URL::class.java.classLoader) as? URL,
        parcel.readValue(URL::class.java.classLoader) as? URL
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(buyNow)
        parcel.writeValue(checkoutPayload)
        parcel.writeValue(token)
        parcel.writeValue(ppaConfirmToken)
        parcel.writeValue(singleUseCardToken)
        parcel.writeValue(checkoutUrl)
        parcel.writeValue(redirectUrl)
        parcel.writeValue(confirmUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AfterpayCheckoutV3Options> {
        override fun createFromParcel(parcel: Parcel): AfterpayCheckoutV3Options {
            return AfterpayCheckoutV3Options(parcel)
        }

        override fun newArray(size: Int): Array<AfterpayCheckoutV3Options?> {
            return arrayOfNulls(size)
        }
    }
}

package com.afterpay.android

import android.os.Parcelable
import android.os.Parcel

data class AfterpayCheckoutV2Options(
    val pickup: Boolean? = null,
    val buyNow: Boolean? = null,
    val shippingOptionRequired: Boolean? = null,
    val enableSingleShippingOptionUpdate: Boolean? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(pickup)
        parcel.writeValue(buyNow)
        parcel.writeValue(shippingOptionRequired)
        parcel.writeValue(enableSingleShippingOptionUpdate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AfterpayCheckoutV2Options> {
        override fun createFromParcel(parcel: Parcel): AfterpayCheckoutV2Options {
            return AfterpayCheckoutV2Options(parcel)
        }

        override fun newArray(size: Int): Array<AfterpayCheckoutV2Options?> {
            return arrayOfNulls(size)
        }
    }
}

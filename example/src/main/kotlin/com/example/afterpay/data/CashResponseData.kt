package com.example.afterpay.data

import android.os.Parcel
import android.os.Parcelable

data class CashResponseData(
    val cashTag: String? = null,
    val amount: String? = null,
    val grantId: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(cashTag)
        parcel.writeString(amount)
        parcel.writeString(grantId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CashResponseData> {
        override fun createFromParcel(parcel: Parcel): CashResponseData {
            return CashResponseData(parcel)
        }

        override fun newArray(size: Int): Array<CashResponseData?> {
            return arrayOfNulls(size)
        }
    }
}

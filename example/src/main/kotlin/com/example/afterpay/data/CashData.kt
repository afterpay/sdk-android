package com.example.afterpay.data

import android.os.Parcel
import android.os.Parcelable

data class CashData(
    val cashTag: String? = null,
    val amount: String? = null,
    val grantId: String? = null,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(cashTag)
        parcel.writeString(amount)
        parcel.writeString(grantId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CashData> {
        override fun createFromParcel(parcel: Parcel): CashData {
            return CashData(parcel)
        }

        override fun newArray(size: Int): Array<CashData?> {
            return arrayOfNulls(size)
        }
    }
}

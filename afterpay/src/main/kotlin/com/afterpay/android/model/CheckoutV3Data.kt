package com.afterpay.android.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/** Data returned from a successful V3 checkout */
@Serializable
data class CheckoutV3Data(
    /** The virtual card details */
    val cardDetails: VirtualCard,
    /** The time before which an authorization needs to be made on the virtual card. */
    val cardValidUntil: String?,
    /** The collection of tokens required to update the merchant reference or cancel the virtual card */
    val tokens: CheckoutV3Tokens
) : Parcelable {
    constructor(parcel: Parcel) : this(
        cardDetails = parcel.readString()?.let { Json.decodeFromString(it) } ?: throw IllegalArgumentException("Missing Serialized value for `cardDetails`"),
        cardValidUntil = parcel.readString(),
        tokens = parcel.readString()?.let { Json.decodeFromString(it) } ?: throw IllegalArgumentException("Missing Serialized value `tokens`")
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(Json.encodeToString(cardDetails))
        parcel.writeString(cardValidUntil)
        parcel.writeString(Json.encodeToString(tokens))
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CheckoutV3Data> {
        override fun createFromParcel(parcel: Parcel): CheckoutV3Data {
            return CheckoutV3Data(parcel)
        }

        override fun newArray(size: Int): Array<CheckoutV3Data?> {
            return arrayOfNulls(size)
        }
    }
}

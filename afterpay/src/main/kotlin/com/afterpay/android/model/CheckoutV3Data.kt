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

import android.os.Parcel
import android.os.Parcelable
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

/** Data returned from a successful V3 checkout */
@Serializable
data class CheckoutV3Data(
  /** The virtual card details */
  val cardDetails: VirtualCard,
  /** The time before which an authorization needs to be made on the virtual card. */
  internal val cardValidUntilInternal: String?,
  /** The collection of tokens required to update the merchant reference or cancel the virtual card */
  val tokens: CheckoutV3Tokens,
) : Parcelable {
  constructor(parcel: Parcel) : this(
    cardDetails = parcel.readString()?.let { Json.decodeFromString(it) } ?: throw IllegalArgumentException("Missing Serialized value for `cardDetails`"),
    cardValidUntilInternal = parcel.readString(),
    tokens = parcel.readString()?.let { Json.decodeFromString(it) } ?: throw IllegalArgumentException("Missing Serialized value `tokens`"),
  )

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeString(Json.encodeToString(cardDetails))
    parcel.writeString(cardValidUntilInternal)
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

  val cardValidUntil: Instant?
    get() = cardValidUntilInternal?.let { Instant.parse(it) }
}

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
package com.afterpay.android

import android.os.Parcel
import android.os.Parcelable

data class AfterpayCheckoutV2Options(
  val pickup: Boolean? = null,
  val buyNow: Boolean? = null,
  val shippingOptionRequired: Boolean? = null,
  val enableSingleShippingOptionUpdate: Boolean? = null,
) : Parcelable {
  constructor(parcel: Parcel) : this(
    parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
    parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
    parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
    parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
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

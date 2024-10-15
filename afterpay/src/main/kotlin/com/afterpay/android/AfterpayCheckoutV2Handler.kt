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

import com.afterpay.android.model.ShippingAddress
import com.afterpay.android.model.ShippingOption
import com.afterpay.android.model.ShippingOptionUpdateResult
import com.afterpay.android.model.ShippingOptionsResult

interface AfterpayCheckoutV2Handler {
  fun didCommenceCheckout(onTokenLoaded: (Result<String>) -> Unit)

  fun shippingAddressDidChange(
    address: ShippingAddress,
    onProvideShippingOptions: (ShippingOptionsResult) -> Unit,
  )

  fun shippingOptionDidChange(
    shippingOption: ShippingOption,
    onProvideShippingOption: (ShippingOptionUpdateResult?) -> Unit,
  )
}

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
package com.afterpay.android.internal

import com.afterpay.android.Afterpay
import com.afterpay.android.AfterpayCheckoutV2Options
import com.afterpay.android.BuildConfig
import com.afterpay.android.model.Configuration
import kotlinx.serialization.Serializable

@Serializable
internal data class AfterpayCheckoutV2(
    val token: String,
    val locale: String,
    val environment: String,
    val version: String,
    val pickup: Boolean?,
    val buyNow: Boolean?,
    val shippingOptionRequired: Boolean?,
    val checkoutRedesignForced: Boolean?,
    val consumerLocale: String?,
) {
    constructor(
        token: String,
        configuration: Configuration,
        options: AfterpayCheckoutV2Options,
    ) : this(
        token = token,
        locale = configuration.locale.toString(),
        environment = configuration.environment.toString(),
        version = "${BuildConfig.AfterpayLibraryVersion}-android",
        pickup = options.pickup,
        buyNow = options.buyNow,
        shippingOptionRequired = options.shippingOptionRequired,
        checkoutRedesignForced = options.enableSingleShippingOptionUpdate,
        consumerLocale = Afterpay.language.toString(),
    )
}

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

import java.math.BigDecimal

/** The order total. Each property will be transformed to a `Money` object by
 * conforming the amount to ISO-4217 by:
 * - Rounding to 2 decimals using banker's rounding.
 * - Including the currency code as provided by [AfterpayRegion].
 */
data class OrderTotal(
  /** Amount to be charged to consumer, inclusive of [shipping] and [tax]. */
  val total: BigDecimal,
  /** The shipping amount, included for fraud detection purposes. */
  val shipping: BigDecimal,
  /** The tax amount, included for fraud detection purposes. */
  val tax: BigDecimal,
)

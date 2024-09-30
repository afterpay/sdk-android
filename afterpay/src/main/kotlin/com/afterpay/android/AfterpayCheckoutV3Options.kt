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

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.net.URL

@Parcelize
data class AfterpayCheckoutV3Options(
  val buyNow: Boolean? = null,
  val checkoutPayload: String? = null,
  val token: String? = null,
  val ppaConfirmToken: String? = null,
  val singleUseCardToken: String? = null,
  val checkoutUrl: URL? = null,
  val redirectUrl: URL? = null,
  val confirmUrl: URL? = null,
) : Parcelable

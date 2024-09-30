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

import android.net.Uri
import com.afterpay.android.AfterpayEnvironment
import java.net.URL

data class CheckoutV3Configuration(
  val shopDirectoryMerchantId: String,
  val region: AfterpayRegion,
  val environment: AfterpayEnvironment,
) {
  internal val shopDirectoryId: String
    get() = when (region) {
      AfterpayRegion.US -> when (environment) {
        AfterpayEnvironment.SANDBOX -> "cd6b7914412b407d80aaf81d855d1105"
        AfterpayEnvironment.PRODUCTION -> "e1e5632bebe64cee8e5daff8588e8f2f05ca4ed6ac524c76824c04e09033badc"
      }
      // Currently the same values as the US region
      AfterpayRegion.CA -> when (environment) {
        AfterpayEnvironment.SANDBOX -> "cd6b7914412b407d80aaf81d855d1105"
        AfterpayEnvironment.PRODUCTION -> "e1e5632bebe64cee8e5daff8588e8f2f05ca4ed6ac524c76824c04e09033badc"
      }
    }

  private val baseUrl: String
    get() = when (region) {
      AfterpayRegion.US -> when (environment) {
        AfterpayEnvironment.SANDBOX -> "https://api-plus.us-sandbox.afterpay.com/v3/button"
        AfterpayEnvironment.PRODUCTION -> "https://api-plus.us.afterpay.com/v3/button"
      }
      // Currently the same URLs as the US region
      AfterpayRegion.CA -> when (environment) {
        AfterpayEnvironment.SANDBOX -> "https://api-plus.us-sandbox.afterpay.com/v3/button"
        AfterpayEnvironment.PRODUCTION -> "https://api-plus.us.afterpay.com/v3/button"
      }
    }

  val v3CheckoutUrl: URL
    get() = URL(baseUrl)

  val v3CheckoutConfirmationUrl: URL
    get() = URL("$baseUrl/confirm")

  val v3ConfigurationUrl: URL
    get() {
      val url = "$baseUrl/merchant/config"
      val builder = Uri.parse(url)
        .buildUpon()
        .appendQueryParameter("shopDirectoryId", shopDirectoryId)
        .appendQueryParameter("shopDirectoryMerchantId", shopDirectoryMerchantId)
        .build()
      return URL(builder.toString())
    }
}

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

interface CheckoutV3Consumer {
  /** The consumer’s email address. Limited to 128 characters. **/
  val email: String

  /** The consumer’s first name and any middle names. Limited to 128 characters. **/
  val givenNames: String?

  /** The consumer’s last name. Limited to 128 characters. **/
  val surname: String?

  /** The consumer’s phone number. Limited to 32 characters. **/
  val phoneNumber: String?

  /** The consumer's shipping information. **/
  val shippingInformation: CheckoutV3Contact?

  /** The consumer's billing information. **/
  val billingInformation: CheckoutV3Contact?
}

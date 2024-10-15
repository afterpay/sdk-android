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

interface CheckoutV3Contact {
  /** Full name of contact. Limited to 255 characters */
  var name: String

  /** First line of the address. Limited to 128 characters */
  var line1: String

  /** Second line of the address. Limited to 128 characters. */
  var line2: String?

  /** Australian suburb, U.S. city, New Zealand town or city, U.K. Postal town.
   * Maximum length is 128 characters.
   */
  var area1: String?

  /** New Zealand suburb, U.K. village or local area. Maximum length is 128 characters. */
  var area2: String?

  /** U.S. state, Australian state, U.K. county, New Zealand region. Maximum length is 128 characters. */
  var region: String?

  /** The zip code or equivalent. Maximum length is 128 characters. */
  var postcode: String?

  /** The two-character ISO 3166-1 country code. */
  var countryCode: String

  /** The phone number, in E.123 format. Maximum length is 32 characters. */
  var phoneNumber: String?
}

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

import com.afterpay.android.cashapp.AfterpayCashAppJwt
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test

class AfterpayCashAppJwtTest {
  /**
   * JWT was created with the following payload
   *
   * {
   *   "amount": {
   *       "amount": "80.8",
   *       "currency": "USD",
   *       "symbol": "$"
   *   },
   *   "token": "123",
   *   "externalMerchantId": "merchant_abc123",
   *   "redirectUrl": "https://example.com"
   * }
   */
  @Suppress("ktlint:standard:max-line-length")
  private val validJwtValidPayload = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiIsImtpZCI6IjRjYmMwNzY4NGQxYzRlZmIzMGY2YjA1M2VhZjM1Zjc1In0.eyJhbW91bnQiOnsiYW1vdW50IjoiODAuOCIsImN1cnJlbmN5IjoiVVNEIiwic3ltYm9sIjoiJCJ9LCJ0b2tlbiI6IjEyMyIsImV4dGVybmFsTWVyY2hhbnRJZCI6Im1lcmNoYW50X2FiYzEyMyIsInJlZGlyZWN0VXJsIjoiaHR0cHM6Ly9leGFtcGxlLmNvbSJ9.OJo_w8zisdC5572lxfAP2TYf434G_MH9KqpO0nInTabhTvJXIrtfWJsW2Ic4YupN0BfiRKUMdxAAD9f3jtszHQ"

  /**
   * JWT was created randomly
   */
  @Suppress("ktlint:standard:max-line-length")
  private val invalidJwtRandomGenerated = "aw4j3kj32.2nkjgsjfkbr1kjbwekjbwejkbqjerbwrkb.ae4rargaggr"

  /**
   * JWT was created with the same payload as [validJwtValidPayload] and then
   * 2 characters were removed from the start and end of the body part
   */
  @Suppress("ktlint:standard:max-line-length")
  private val invalidJwtMissingChars = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiIsImtpZCI6IjRjYmMwNzY4NGQxYzRlZmIzMGY2YjA1M2VhZjM1Zjc1In0.JhbW91bnQiOnsiYW1vdW50IjoiODAuOCIsImN1cnJlbmN5IjoiVVNEIiwic3ltYm9sIjoiJCJ9LCJ0b2tlbiI6IjEyMyIsImV4dGVybmFsTWVyY2hhbnRJZCI6Im1lcmNoYW50X2FiYzEyMyIsInJlZGlyZWN0VXJsIjoiaHR0cHM6Ly9leGFtcGxlLmNvbS.OJo_w8zisdC5572lxfAP2TYf434G_MH9KqpO0nInTabhTvJXIrtfWJsW2Ic4YupN0BfiRKUMdxAAD9f3jtszHQ"

  @Test
  fun `Should decode valid jwt with valid payload`() {
    val jwtResult = AfterpayCashAppJwt.decode(validJwtValidPayload)
    val jwt = jwtResult.getOrNull()!!

    assertEquals(true, jwtResult.isSuccess)
    assertEquals("80.8", jwt.amount.amount)
    assertEquals("123", jwt.token)
    assertEquals("merchant_abc123", jwt.externalMerchantId)
    assertEquals("https://example.com", jwt.redirectUrl)
  }

  @Test
  fun `Should fail to decode randomly generated invalid JWT`() {
    val jwtResult = AfterpayCashAppJwt.decode(invalidJwtRandomGenerated)
    val jwt = jwtResult.exceptionOrNull()!!

    assertEquals(true, jwtResult.isFailure)
    assertThat(jwt.message, containsString("Expected start of the object '{', but had 'EOF' instead"))
  }

  @Test
  fun `Should fail to decode invalid JWT due to stripped chars`() {
    val jwtResult = AfterpayCashAppJwt.decode(invalidJwtMissingChars)
    val jwt = jwtResult.exceptionOrNull()!!

    assertEquals(true, jwtResult.isFailure)
    assertThat(jwt.message, containsString("Expected start of the object '{', but had 'EOF' instead"))
  }
}

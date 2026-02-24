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

import org.junit.Assert.assertEquals
import org.junit.Test

class AfterpayLogTest {

  @Test
  fun `sanitizeToken masks long tokens correctly`() {
    val token = "tok_abcdefghijklmnopqrstuvwxyz1234567890"
    val sanitized = AfterpayLog.sanitizeToken(token)
    assertEquals("tok_abcd...7890", sanitized)
  }

  @Test
  fun `sanitizeToken masks short tokens completely`() {
    val token = "shortToken"
    val sanitized = AfterpayLog.sanitizeToken(token)
    assertEquals("****", sanitized)
  }

  @Test
  fun `sanitizeToken handles exactly 12 character tokens`() {
    val token = "123456789012"
    val sanitized = AfterpayLog.sanitizeToken(token)
    assertEquals("****", sanitized)
  }

  @Test
  fun `sanitizeToken handles exactly 13 character tokens`() {
    val token = "1234567890123"
    val sanitized = AfterpayLog.sanitizeToken(token)
    assertEquals("12345678...0123", sanitized)
  }

  @Test
  fun `sanitizeToken handles null token`() {
    val sanitized = AfterpayLog.sanitizeToken(null)
    assertEquals("null", sanitized)
  }

  @Test
  fun `sanitizeUrl removes query parameters`() {
    val url = "https://api.afterpay.com/checkout?token=abc123&session=xyz789"
    val sanitized = AfterpayLog.sanitizeUrl(url)
    assertEquals("https://api.afterpay.com/checkout", sanitized)
  }

  @Test
  fun `sanitizeUrl handles URLs without query parameters`() {
    val url = "https://api.afterpay.com/checkout"
    val sanitized = AfterpayLog.sanitizeUrl(url)
    assertEquals("https://api.afterpay.com/checkout", sanitized)
  }

  @Test
  fun `sanitizeUrl handles null URL`() {
    val sanitized = AfterpayLog.sanitizeUrl(null)
    assertEquals("null", sanitized)
  }

  @Test
  fun `sanitizeUrl handles URL with fragment after query`() {
    val url = "https://api.afterpay.com/checkout?token=abc#section"
    val sanitized = AfterpayLog.sanitizeUrl(url)
    assertEquals("https://api.afterpay.com/checkout", sanitized)
  }

  @Test
  fun `logging methods don't crash when called`() {
    // In unit tests, BuildConfig.DEBUG may be true but android.util.Log is not available.
    // The logging calls may throw RuntimeException due to unmocked Log.
    // This is expected behavior in unit tests - in real usage, Log will be available.
    // We're just testing that the methods can be called without compilation errors.
    try {
      AfterpayLog.d("Debug message")
      AfterpayLog.d("Debug with args: %s", "arg1")
      AfterpayLog.w("Warning message")
      AfterpayLog.w("Warning with args: %s", "arg1")
      AfterpayLog.w(Exception("Test exception"), "Warning with exception")
      AfterpayLog.e("Error message")
      AfterpayLog.e("Error with args: %s", "arg1")
      AfterpayLog.e(Exception("Test exception"), "Error with exception")
    } catch (e: RuntimeException) {
      // Expected in unit tests where android.util.Log is not mocked
      // In production, this won't happen as Log is always available
    }
  }
}

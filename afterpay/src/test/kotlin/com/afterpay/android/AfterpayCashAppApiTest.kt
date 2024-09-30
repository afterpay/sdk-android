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
import com.afterpay.android.cashapp.AfterpayCashAppApi
import com.afterpay.android.cashapp.AfterpayCashAppApi.CashHttpVerb
import com.afterpay.android.cashapp.AfterpayCashAppSigningResponse
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InvalidObjectException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class AfterpayCashAppApiTest {
  companion object {
    private val connection = mockk<HttpsURLConnection>()

    @BeforeClass
    @JvmStatic
    fun setup() {
      every { connection.requestMethod = "POST" } returns Unit
      every { connection.doInput = true } returns Unit
      every { connection.doOutput = true } returns Unit
      every { connection.setRequestProperty(any(), any()) } returns Unit
      every { connection.outputStream } returns ByteArrayOutputStream()
    }
  }

  @Test
  fun `test cashRequest for 503 response`() {
    every { connection.responseCode } returns 503
    every { connection.errorStream } returns ByteArrayInputStream(byteArrayOf())

    val url = mockk<URL>()
    every { url.openConnection() } returns connection
    val requestBody = """{ "token": "123" }"""

    val result = AfterpayCashAppApi.cashRequest<String, String>(url, CashHttpVerb.POST, requestBody)

    assert(result.isFailure)
    assertEquals("Unexpected response code: 503.", result.exceptionOrNull()?.message)
    assert(result.exceptionOrNull() is InvalidObjectException)
  }

  @Test
  fun `test cashRequest for 400 response`() {
    every { connection.responseCode } returns 400
    every { connection.errorStream } returns ByteArrayInputStream(byteArrayOf())

    val url = mockk<URL>()
    every { url.openConnection() } returns connection
    val requestBody = """{ "token": "123" }"""

    val result = AfterpayCashAppApi.cashRequest<String, String>(url, CashHttpVerb.POST, requestBody)

    assert(result.isFailure)
    assertEquals("Unexpected response code: 400.", result.exceptionOrNull()?.message)
    assert(result.exceptionOrNull() is InvalidObjectException)
  }

  @OptIn(ExperimentalSerializationApi::class)
  @Test
  fun `test cashRequest for invalid json response`() {
    val responseBody = """
            {
              "invalid" : "json"
            }
    """.trimIndent()
    val inputStream = ByteArrayInputStream(responseBody.toByteArray())

    every { connection.responseCode } returns 200
    every { connection.inputStream } returns inputStream
    every { connection.errorStream } returns null

    val url = mockk<URL>()
    every { url.openConnection() } returns connection
    val requestBody = """{ "token": "123" }"""

    val result = AfterpayCashAppApi.cashRequest<AfterpayCashAppSigningResponse, String>(url, CashHttpVerb.POST, requestBody)
    val response = result.exceptionOrNull()!!

    assert(result.isFailure)
    assert(response is MissingFieldException)
  }

  @Test
  fun `test cashRequest for valid json response`() {
    val responseBody = """
            {
              "jwtToken" : "abc123",
              "redirectUrl" : "https://example.com/some/path/confirm",
              "externalBrandId" : "BRAND_ABC"
            }
    """.trimIndent()
    val inputStream = ByteArrayInputStream(responseBody.toByteArray())

    every { connection.responseCode } returns 200
    every { connection.inputStream } returns inputStream
    every { connection.errorStream } returns null

    val url = mockk<URL>()
    every { url.openConnection() } returns connection
    val requestBody = """{ "token": "123" }"""

    val result = AfterpayCashAppApi.cashRequest<AfterpayCashAppSigningResponse, String>(url, CashHttpVerb.POST, requestBody)
    val response = result.getOrNull()!!

    assert(result.isSuccess)
    assertEquals("BRAND_ABC", response.externalBrandId)
    assertEquals("abc123", response.jwtToken)
    assertEquals("https://example.com/some/path/confirm", response.redirectUrl)
  }
}

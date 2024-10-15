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
package com.afterpay.android.cashapp

import com.afterpay.android.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InvalidObjectException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

internal object AfterpayCashAppApi {
  private val json = Json { ignoreUnknownKeys = true }

  internal inline fun <reified T, reified B> cashRequest(url: URL, method: CashHttpVerb, body: B): Result<T> {
    val connection = url.openConnection() as HttpsURLConnection
    return try {
      configure(connection, method)
      val payload = (body as? String) ?: json.encodeToString(body)

      OutputStreamWriter(connection.outputStream).use { writer ->
        writer.write(payload)
        writer.flush()
      }

      if (connection.errorStream == null && connection.responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
        connection.inputStream.bufferedReader().use { reader ->
          val data = reader.readText()
          val result = json.decodeFromString<T>(data)
          Result.success(result)
        }
      } else {
        throw InvalidObjectException("Unexpected response code: ${connection.responseCode}.")
      }
    } catch (exception: Exception) {
      Result.failure(exception)
    }
  }

  private fun configure(connection: HttpsURLConnection, type: CashHttpVerb) {
    connection.requestMethod = type.name
    connection.setRequestProperty("${BuildConfig.AfterpayLibraryVersion}-android", "X-Afterpay-SDK")
    when (type) {
      CashHttpVerb.POST, CashHttpVerb.PUT -> {
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
      }
      else -> { }
    }
    when (type) {
      CashHttpVerb.GET -> {
        connection.doInput = true
        connection.doOutput = false
      }
      CashHttpVerb.PUT -> {
        connection.doInput = true
        connection.doOutput = false
      }
      CashHttpVerb.POST -> {
        connection.doInput = true
        connection.doOutput = true
      }
    }
  }

  internal enum class CashHttpVerb {
    POST,
    PUT,
    GET,
  }

  @Serializable
  internal data class ApiErrorCashApp(
    val errorCode: String,
    val errorId: String,
    val message: String,
    val httpStatusCode: Int,
  )
}

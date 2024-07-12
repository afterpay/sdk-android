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

import com.afterpay.android.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InvalidObjectException
import java.io.OutputStreamWriter
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.Exception

internal object ApiV3 {
    private val json = Json { ignoreUnknownKeys = true }

    internal inline fun <reified T, reified B> request(url: URL, method: HttpVerb, body: B): Result<T> {
        val connection = url.openConnection() as HttpsURLConnection
        return try {
            configure(connection, method)
            val payload = (body as? String) ?: json.encodeToString(body)

            val outputStreamWriter = OutputStreamWriter(connection.outputStream)
            outputStreamWriter.write(payload)
            outputStreamWriter.flush()

            // TODO: Status code checking, error object decoding, bypass if return type is Unit
            val data = connection.inputStream.bufferedReader().readText()
            connection.inputStream.close()
            val result = json.decodeFromString<T>(data)
            Result.success(result)
        } catch (exception: Exception) {
            try {
                val data = connection.errorStream.bufferedReader().readText()
                connection.errorStream.close()
                val result = json.decodeFromString<ApiErrorV3>(data)
                Result.failure(InvalidObjectException(result.message))
            } catch (_: Exception) {
                Result.failure(exception)
            }
        } finally {
            connection.disconnect()
        }
    }

    internal inline fun <reified B> requestUnit(url: URL, method: HttpVerb, body: B): Result<Unit> {
        val connection = url.openConnection() as HttpsURLConnection
        return try {
            configure(connection, method)
            val payload = (body as? String) ?: json.encodeToString(body)

            val outputStreamWriter = OutputStreamWriter(connection.outputStream)
            outputStreamWriter.write(payload)
            outputStreamWriter.flush()

            if (connection.errorStream == null && connection.responseCode < 400) {
                Result.success(Unit)
            } else {
                throw InvalidObjectException("Unexpected response code: ${connection.responseCode}")
            }
        } catch (exception: Exception) {
            try {
                val data = connection.errorStream.bufferedReader().readText()
                connection.errorStream.close()
                val result = json.decodeFromString<ApiErrorV3>(data)
                Result.failure(InvalidObjectException(result.message))
            } catch (_: Exception) {
                Result.failure(exception)
            }
        } finally {
            connection.disconnect()
        }
    }

    internal inline fun <reified T> get(url: URL): Result<T> {
        val connection = url.openConnection() as HttpsURLConnection
        return try {
            configure(connection, HttpVerb.GET)

            val data = connection.inputStream.bufferedReader().readText()
            connection.inputStream.close()
            val result = json.decodeFromString<T>(data)
            Result.success(result)
        } catch (exception: Exception) {
            try {
                val data = connection.errorStream.bufferedReader().readText()
                connection.errorStream.close()
                val result = json.decodeFromString<ApiErrorV3>(data)
                Result.failure(InvalidObjectException(result.message))
            } catch (_: Exception) {
                Result.failure(exception)
            }
        } finally {
            connection.disconnect()
        }
    }

    internal enum class HttpVerb {
        POST,
        PUT,
        GET,
    }

    private fun configure(connection: HttpsURLConnection, type: HttpVerb) {
        connection.requestMethod = type.name
        connection.setRequestProperty("${BuildConfig.AfterpayLibraryVersion}-android", "X-Afterpay-SDK")
        when (type) {
            HttpVerb.POST, HttpVerb.PUT -> {
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
            }
            else -> { }
        }
        when (type) {
            HttpVerb.GET -> {
                connection.doInput = true
                connection.doOutput = false
            }
            HttpVerb.PUT -> {
                connection.doInput = true
                connection.doOutput = false // TODO: What?
            }
            HttpVerb.POST -> {
                connection.doInput = true
                connection.doOutput = true
            }
        }
    }

    @Serializable
    internal data class ApiErrorV3(
        val errorCode: String,
        val errorId: String,
        val message: String,
        val httpStatusCode: Int,
    )
}

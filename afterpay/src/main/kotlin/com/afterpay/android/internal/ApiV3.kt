package com.afterpay.android.internal

import com.afterpay.android.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.io.InvalidObjectException
import java.io.OutputStreamWriter
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.Exception

internal object ApiV3 {

    @JvmStatic
    internal inline fun <reified T, reified B>request(url: URL, method: HttpVerb, body: B): Result<T> {
        val connection = url.openConnection() as HttpsURLConnection
        return try {
            configure(connection, method)
            val payload = (body as? String) ?: Json.encodeToString(body)

            val outputStreamWriter = OutputStreamWriter(connection.outputStream)
            outputStreamWriter.write(payload)
            outputStreamWriter.flush()

            // TODO: Status code checking, error object decoding, bypass if return type is Unit
            val data = connection.inputStream.bufferedReader().readText()
            val result = Json.decodeFromString<T>(data)
            Result.success(result)
        } catch (error: Exception) {
            try {
                val data = connection.errorStream.bufferedReader().readText()
                val result = Json.decodeFromString<ApiErrorV3>(data)
                Result.failure(InvalidObjectException(result.message))
            } catch (_: Exception) {
                Result.failure(error)
            }
        } finally {
            connection.disconnect()
        }
    }

    @JvmStatic
    internal inline fun <reified B>requestUnit(url: URL, method: HttpVerb, body: B): Result<Unit> {
        val connection = url.openConnection() as HttpsURLConnection
        return try {
            configure(connection, method)
            val payload = (body as? String) ?: Json.encodeToString(body)

            val outputStreamWriter = OutputStreamWriter(connection.outputStream)
            outputStreamWriter.write(payload)
            outputStreamWriter.flush()

            when (connection.responseCode) {
                200, 201, 204 -> Result.success(Unit)
                else -> {
                    Result.failure(InvalidObjectException("Unexpected response code ${connection.responseCode}"))
                }
            }
        } catch (error: Exception) {
            try {
                val data = connection.errorStream.bufferedReader().readText()
                val result = Json.decodeFromString<ApiErrorV3>(data)
                Result.failure(InvalidObjectException(result.message))
            } catch (_: Exception) {
                Result.failure(error)
            }
        } finally {
            connection.disconnect()
        }
    }

    @JvmStatic
    internal inline fun <reified T>get(url: URL): Result<T> {
        val connection = url.openConnection() as HttpsURLConnection
        return try {
            configure(connection, HttpVerb.GET)

            val inputStream = connection.inputStream.bufferedReader().readText()
            val result = Json.decodeFromString<T>(inputStream)
            Result.success(result)
        } catch (error: Exception) {
            Result.failure(error)
        } finally {
            connection.disconnect()
        }
    }

    internal enum class HttpVerb(name: String) {
        POST("POST"), PUT("PUT"), GET("GET")
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
}

@Serializable
data class ApiErrorV3(
    val errorCode: String,
    val errorId: String,
    val message: String,
    val httpStatusCode: Int
)

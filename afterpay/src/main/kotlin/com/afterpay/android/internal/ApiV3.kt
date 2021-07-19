package com.afterpay.android.internal

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.io.OutputStreamWriter
import java.lang.Exception
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object ApiV3 {
    // @JvmStatic
    // internal suspend inline fun <reified T, reified B>request(url: URL, method: HttpVerb, body: B, crossinline completion: (Result<T>) -> Unit) {
    //     val connection = url.openConnection() as HttpsURLConnection
    //     try {
    //         configure(connection, method)
    //
    //         val payload = Json.encodeToString(body)
    //         val outputStreamWriter = OutputStreamWriter(connection.outputStream)
    //         outputStreamWriter.write(payload)
    //         outputStreamWriter.flush()
    //
    //         // TODO: Status code checking, error object decoding
    //         val inputStream = connection.inputStream.bufferedReader().readText()
    //         val result = Json.decodeFromString<T>(inputStream)
    //         withContext(Dispatchers.Main) {
    //             completion(Result.success(result))
    //         }
    //     } catch (error: Exception) {
    //         withContext(Dispatchers.Main) {
    //             completion(Result.failure(error))
    //         }
    //     } finally {
    //         connection.disconnect()
    //     }
    // }

    @JvmStatic
    internal inline fun <reified T, reified B>request(url: URL, method: HttpVerb, body: B): Result<T> {
        val connection = url.openConnection() as HttpsURLConnection
        return try {
            configure(connection, method)

            val payload = Json.encodeToString(body)
            val outputStreamWriter = OutputStreamWriter(connection.outputStream)
            outputStreamWriter.write(payload)
            outputStreamWriter.flush()

            // TODO: Status code checking, error object decoding, bypass if return type is Unit
            val inputStream = connection.inputStream.bufferedReader().readText()
            val result = Json.decodeFromString<T>(inputStream)
            Result.success(result)
        } catch (error: Exception) {
            Result.failure(error)
        } finally {
            connection.disconnect()
        }
    }

    // @JvmStatic
    // internal suspend inline fun <reified T>get(url: URL, crossinline completion: (Result<T>) -> Unit) {
    //     val connection = url.openConnection() as HttpsURLConnection
    //     try {
    //         configure(connection, HttpVerb.GET)
    //         connection.setChunkedStreamingMode(0)
    //
    //         val inputStream = connection.inputStream.bufferedReader().readText()
    //         val result = Json.decodeFromString<T>(inputStream)
    //         withContext(Dispatchers.Main) {
    //             completion(Result.success(result))
    //         }
    //     } catch (error: Exception) {
    //         withContext(Dispatchers.Main) {
    //             completion(Result.failure(error))
    //         }
    //     } finally {
    //         connection.disconnect()
    //     }
    // }

    @JvmStatic
    internal inline fun <reified T>get(url: URL): Result<T> {
        val connection = url.openConnection() as HttpsURLConnection
        return try {
            configure(connection, HttpVerb.GET)
            connection.setChunkedStreamingMode(0)

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
        // TODO: SDK version like on iOS?
        connection.setRequestProperty("1.3", "X-Afterpay-SDK")
        when (type) {
            HttpVerb.POST, HttpVerb.PUT -> {
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
            }
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

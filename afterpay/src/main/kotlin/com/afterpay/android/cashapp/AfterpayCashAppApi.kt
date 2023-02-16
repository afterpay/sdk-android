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

            val outputStreamWriter = OutputStreamWriter(connection.outputStream)
            outputStreamWriter.write(payload)
            outputStreamWriter.flush()

            if (connection.errorStream == null && connection.responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
                val data = connection.inputStream.bufferedReader().readText()
                connection.inputStream.close()
                val result = json.decodeFromString<T>(data)
                Result.success(result)
            } else {
                throw InvalidObjectException("Unexpected response code: ${connection.responseCode}")
            }
        } catch (exception: Exception) {
            try {
                val data = connection.errorStream.bufferedReader().readText()
                connection.errorStream.close()
                val result = json.decodeFromString<ApiErrorCashApp>(data)
                Result.failure(InvalidObjectException(result.message))
            } catch (_: Exception) {
                Result.failure(exception)
            }
        } finally {
            connection.disconnect()
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
        POST, PUT, GET
    }

    @Serializable
    internal data class ApiErrorCashApp(
        val errorCode: String,
        val errorId: String,
        val message: String,
        val httpStatusCode: Int,
    )
}

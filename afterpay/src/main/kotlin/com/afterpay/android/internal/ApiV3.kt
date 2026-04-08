package com.afterpay.android.internal

import com.afterpay.android.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.OutputStreamWriter
import java.net.URL
import javax.net.ssl.HttpsURLConnection

internal object ApiV3 {

    private val json = Json { ignoreUnknownKeys = true }

    internal inline fun <reified T> get(url: URL): Result<T> =
        execute(url, HttpVerb.GET) { /* no body */ }

    internal inline fun <reified T, reified B> request(
        url: URL,
        method: HttpVerb,
        body: B
    ): Result<T> = execute(url, method) {
        val payload = (body as? String) ?: json.encodeToString(body)
        OutputStreamWriter(it.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(payload)
            writer.flush()
        }
    }

    // ── Shared execution backbone (eliminates duplication) ──────────────

    private inline fun <reified T> execute(
        url: URL,
        method: HttpVerb,
        crossinline writeBody: (HttpsURLConnection) -> Unit
    ): Result<T> {
        val connection = configureConnection(url, method)
        return try {
            writeBody(connection)
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            Result.success(json.decodeFromString<T>(response))
        } catch (exception: Exception) {
            parseError(connection, exception)
        } finally {
            connection.disconnect()
        }
    }

    // ── Error response parser (single source of truth) ─────────────────

    private inline fun <reified T> parseError(
        connection: HttpsURLConnection,
        fallback: Exception
    ): Result<T> = try {
        val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }
            ?: return Result.failure(fallback)
        val error = json.decodeFromString<ApiErrorV3>(errorBody)
        Result.failure(InvalidObjectException(error.message))
    } catch (_: Exception) {
        Result.failure(fallback)
    }

    // ── Connection setup ───────────────────────────────────────────────

    private fun configureConnection(url: URL, method: HttpVerb): HttpsURLConnection {
        val connection = url.openConnection() as HttpsURLConnection
        connection.requestMethod = method.name
        connection.setRequestProperty(
            "X-Afterpay-SDK",
            "${BuildConfig.AfterpayLibraryVersion}-android"
        )
        if (method == HttpVerb.POST || method == HttpVerb.PUT) {
            connection.doOutput = true
        }
        return connection
    }

    @Serializable
    internal data class ApiErrorV3(
        val errorCode: String,
        val errorId: String,
        val message: String,
        val httpStatusCode: Int,
    )
}

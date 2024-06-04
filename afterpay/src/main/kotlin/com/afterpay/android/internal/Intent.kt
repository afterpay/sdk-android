package com.afterpay.android.internal

import android.content.Intent
import com.afterpay.android.AfterpayCheckoutV2Options
import com.afterpay.android.AfterpayCheckoutV3Options
import com.afterpay.android.CancellationStatus
import com.afterpay.android.CancellationStatusV3
import com.afterpay.android.model.CheckoutV3Data
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.lang.Exception

private object AfterpayIntent {
    const val CHECKOUT_URL = "AFTERPAY_CHECKOUT_URL"
    const val CHECKOUT_OPTIONS = "AFTERPAY_CHECKOUT_OPTIONS"
    const val INFO_URL = "AFTERPAY_INFO_URL"
    const val ORDER_TOKEN = "AFTERPAY_ORDER_TOKEN"
    const val CANCELLATION_STATUS = "AFTERPAY_CANCELLATION_STATUS"
    const val CANCELLATION_ERROR = "AFTERPAY_CANCELLATION_ERROR"
    const val RESULT_DATA_V3 = "AFTERPAY_RESULT_DATA_V3"
    const val SHOULD_LOAD_REDIRECT_URLS = "AFTERPAY_SHOULD_LOAD_REDIRECT_URLS"
}

internal fun Intent.putCheckoutUrlExtra(url: String): Intent =
    putExtra(AfterpayIntent.CHECKOUT_URL, url)

internal fun Intent.getCheckoutUrlExtra(): String? =
    getStringExtra(AfterpayIntent.CHECKOUT_URL)

internal fun Intent.putCheckoutShouldLoadRedirectUrls(bool: Boolean): Intent =
    putExtra(AfterpayIntent.SHOULD_LOAD_REDIRECT_URLS, bool)

internal fun Intent.getCheckoutShouldLoadRedirectUrls(): Boolean =
    getBooleanExtra(AfterpayIntent.SHOULD_LOAD_REDIRECT_URLS, false)

internal fun Intent.putCheckoutV2OptionsExtra(options: AfterpayCheckoutV2Options): Intent =
    putExtra(AfterpayIntent.CHECKOUT_OPTIONS, options)

internal fun Intent.getCheckoutV2OptionsExtra(): AfterpayCheckoutV2Options? =
    getParcelableExtra(AfterpayIntent.CHECKOUT_OPTIONS)

internal fun Intent.putCheckoutV3OptionsExtra(options: AfterpayCheckoutV3Options): Intent =
    putExtra(AfterpayIntent.CHECKOUT_OPTIONS, options)

internal fun Intent.getCheckoutV3OptionsExtra(): AfterpayCheckoutV3Options? =
    getParcelableExtra(AfterpayIntent.CHECKOUT_OPTIONS)

internal fun Intent.putOrderTokenExtra(token: String): Intent =
    putExtra(AfterpayIntent.ORDER_TOKEN, token)

internal fun Intent.getOrderTokenExtra(): String? =
    getStringExtra(AfterpayIntent.ORDER_TOKEN)

internal fun Intent.putResultDataV3(resultData: CheckoutV3Data): Intent {
    val json = Json.encodeToString(resultData)
    putExtra(AfterpayIntent.RESULT_DATA_V3, json)
    return this
}

internal fun Intent.getResultDataExtra(): CheckoutV3Data? {
    val data = getStringExtra(AfterpayIntent.RESULT_DATA_V3) ?: return null
    val json = Json { ignoreUnknownKeys = true }
    return json.decodeFromString(data)
}

internal fun Intent.putCancellationStatusExtra(status: CancellationStatus): Intent =
    putExtra(AfterpayIntent.CANCELLATION_STATUS, status.name)

internal fun Intent.getCancellationStatusExtra(): CancellationStatus? = try {
    getStringExtra(AfterpayIntent.CANCELLATION_STATUS)?.let { enumValueOf<CancellationStatus>(it) }
} catch (_: Exception) {
    null
}

internal fun Intent.putCancellationStatusExtraV3(status: CancellationStatusV3): Intent =
    putExtra(AfterpayIntent.CANCELLATION_STATUS, status.name)

internal fun Intent.getCancellationStatusExtraV3(): CancellationStatusV3? = try {
    getStringExtra(AfterpayIntent.CANCELLATION_STATUS)?.let { enumValueOf<CancellationStatusV3>(it) }
} catch (_: Exception) {
    null
}

internal fun Intent.putCancellationStatusExtraErrorV3(error: Exception): Intent =
    putExtra(AfterpayIntent.CANCELLATION_ERROR, error)

internal fun Intent.getCancellationStatusExtraErrorV3(): Exception? =
    getSerializableExtra(AfterpayIntent.CANCELLATION_ERROR) as? Exception

internal fun Intent.putInfoUrlExtra(url: String): Intent =
    putExtra(AfterpayIntent.INFO_URL, url)

internal fun Intent.getInfoUrlExtra(): String? =
    getStringExtra(AfterpayIntent.INFO_URL)

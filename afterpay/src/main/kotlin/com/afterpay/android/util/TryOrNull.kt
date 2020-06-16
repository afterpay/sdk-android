package com.afterpay.android.util

internal inline fun <T> tryOrNull(block: () -> T): T? = try {
    block()
} catch (_: Exception) {
    null
}

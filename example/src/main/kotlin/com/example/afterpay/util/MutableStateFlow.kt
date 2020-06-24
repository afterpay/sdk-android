package com.example.afterpay.util

import kotlinx.coroutines.flow.MutableStateFlow

inline fun <T> MutableStateFlow<T>.update(block: T.() -> T) {
    value = block(value)
}

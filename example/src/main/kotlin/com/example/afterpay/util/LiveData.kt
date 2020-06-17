package com.example.afterpay.util

import androidx.lifecycle.MutableLiveData

inline fun <T> MutableLiveData<T>.update(block: T.() -> T) {
    value?.let {
        postValue(block(it))
    }
}

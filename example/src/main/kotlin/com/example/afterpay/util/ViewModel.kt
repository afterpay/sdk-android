package com.example.afterpay.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

inline fun <reified T : ViewModel> viewModelFactory(
    viewModelClass: Class<T> = T::class.java,
    crossinline factory: () -> T,
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        if (modelClass.isAssignableFrom(viewModelClass)) {
            factory() as T
        } else {
            throw IllegalArgumentException("$viewModelClass Not Found")
        }
}

package com.example.afterpay.data

sealed class Result<out T> {
    object Idle : Result<Nothing>()
    object Loading : Result<Nothing>()
    data class Success<T>(val data: T) : Result<T>()
    data class Failure(val message: String) : Result<Nothing>()
}

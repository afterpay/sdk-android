package com.example

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun showToastFromBackground(
    context: Context,
    message: String,
) {
    withContext(Dispatchers.Main) {
        showToast(context, message)
    }
}

fun showToast(
    context: Context,
    message: String,
) {
    Toast.makeText(
        context,
        message,
        Toast.LENGTH_SHORT,
    ).show()
}

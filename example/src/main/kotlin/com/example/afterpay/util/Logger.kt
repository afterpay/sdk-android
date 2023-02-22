package com.example.afterpay.util

import android.util.Log

class Logger {
    var isEnabled = true

    fun error(tag: String = "ExampleApp", message: String? = null, tr: Throwable? = null) {
        Log.e(tag, message, tr)
    }

    fun debug( tag: String = "ExampleApp", message: String? = null, tr: Throwable? = null) {
        Log.d(tag, message, tr)
    }

    fun info(tag: String = "ExampleApp", message: String? = null, tr: Throwable? = null) {
        Log.i(tag, message, tr)
    }

    fun verbose(tag: String = "ExampleApp", message: String? = null, tr: Throwable? = null) {
        Log.v(tag, message, tr)
    }

    fun warn(tag: String = "ExampleApp", message: String? = null, tr: Throwable? = null) {
        Log.w(tag, message, tr)
    }
}

object LoggerFactory {
    fun getLogger(): Logger {
        return Logger()
    }
}


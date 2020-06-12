package com.afterpay.android

import android.content.Context

object Afterpay {
    fun createClient(context: Context): AfterpayClient = RealAfterpayClient(context)
}

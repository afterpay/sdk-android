package com.afterpay.android.internal

import android.webkit.WebView
import com.afterpay.android.BuildConfig

internal fun WebView.setAfterpayUserAgentString() = apply {
    settings.userAgentString += " Afterpay-Android-SDK/${BuildConfig.AfterpayLibraryVersion}"
}

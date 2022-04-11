package com.afterpay.android.view

enum class AfterpayModalTheme(val slug: String) {
    MINT(""),
    WHITE("-theme-white");

    internal companion object {

        @JvmField
        val DEFAULT = AfterpayModalTheme.MINT
    }
}

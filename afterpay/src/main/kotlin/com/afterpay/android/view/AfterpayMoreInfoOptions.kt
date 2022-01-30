package com.afterpay.android.view

import com.afterpay.android.Afterpay

class AfterpayMoreInfoOptions(
    var modalTheme: AfterpayModalTheme = AfterpayModalTheme.MINT,
    var modalId: String? = null,
    var isCbtEnabled: Boolean = false
) {
    internal fun modalFile(): String {
        modalId?.let {
            return "$it.html"
        }

        val locale = "${Afterpay.locale.language}_${Afterpay.locale.country}"
        val cbt = if (isCbtEnabled) "-cbt" else ""
        val theme = modalTheme.slug

        return "$locale$theme$cbt.html"
    }
}

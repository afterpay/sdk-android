package com.afterpay.android.view

import com.afterpay.android.Afterpay

class AfterpayMoreInfoOptions(
    var modalTheme: AfterpayModalTheme = AfterpayModalTheme.DEFAULT,
    var modalId: String? = null
) {

    fun modalFile() : String {
        modalId?.let {
            return "$it.html"
        }

        val locale = "${Afterpay.locale.language}_${Afterpay.locale.country}"
        return "$locale${modalTheme.slug}.html"
    }
}

package com.afterpay.android.model

import com.afterpay.android.internal.Locales
import java.util.Locale

enum class AfterpayRegion(val locale: Locale, val currencyCode: String) {
    US(Locales.EN_US, "USD"),
    CA(Locales.EN_CA, "CAD")
}

package com.afterpay.android.internal

import java.util.Locale

internal object Locales {

    val AUSTRALIA = Locale("en", "AU")
    val CANADA: Locale = Locale.CANADA
    val NEW_ZEALAND = Locale("en", "NZ")
    val UK: Locale = Locale.UK
    val US: Locale = Locale.US

    val validSet = setOf(AUSTRALIA, CANADA, UK, NEW_ZEALAND, US)
}

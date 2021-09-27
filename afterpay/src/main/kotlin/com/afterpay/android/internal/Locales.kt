package com.afterpay.android.internal

import java.util.Locale

internal object Locales {

    val AUSTRALIA = Locale("en", "AU")
    val CANADA: Locale = Locale.CANADA
    val NEW_ZEALAND = Locale("en", "NZ")
    val UK: Locale = Locale.UK
    val US: Locale = Locale.US

    val validSet = setOf(AUSTRALIA, CANADA, UK, NEW_ZEALAND, US)

    internal val brandLocales = mapOf(
        setOf(AUSTRALIA, CANADA, NEW_ZEALAND, US) to Brand.AFTERPAY,
        setOf(UK) to Brand.CLEARPAY,
    )
}

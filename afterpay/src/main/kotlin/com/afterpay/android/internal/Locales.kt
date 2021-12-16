package com.afterpay.android.internal

import java.util.Locale

internal object Locales {
    val EN_AU = Locale("en", "AU")
    val EN_CA: Locale = Locale.CANADA
    val FR_CA: Locale = Locale.CANADA_FRENCH
    val EN_NZ = Locale("en", "NZ")
    val EN_GB: Locale = Locale.UK
    val EN_US: Locale = Locale.US
    val IT_IT: Locale = Locale.ITALY
    val FR_FR: Locale = Locale.FRANCE
    val ES_ES: Locale = Locale("es", "ES")

    val validSet = setOf(
        EN_AU,
        EN_CA,
        FR_CA,
        EN_GB,
        EN_NZ,
        EN_US,
        IT_IT,
        FR_FR,
        ES_ES
    )
}

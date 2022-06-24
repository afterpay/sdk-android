package com.afterpay.android.internal

import java.util.Locale

private val validRegionLanguages = mapOf(
    Locales.EN_AU.country to setOf(Locales.EN_AU),
    Locales.EN_GB.country to setOf(Locales.EN_GB),
    Locales.EN_NZ.country to setOf(Locales.EN_NZ),
    Locales.EN_US.country to setOf(Locales.EN_US),
    Locales.EN_CA.country to setOf(Locales.EN_CA, Locales.FR_CA),
    Locales.FR_FR.country to setOf(Locales.FR_FR, Locales.EN_GB),
    Locales.IT_IT.country to setOf(Locales.IT_IT, Locales.EN_GB),
    Locales.ES_ES.country to setOf(Locales.ES_ES, Locales.EN_GB),
)

internal object Locales {
    val EN_AU: Locale = Locale("en", "AU")
    val EN_CA: Locale = Locale.CANADA
    val FR_CA: Locale = Locale.CANADA_FRENCH
    val EN_NZ: Locale = Locale("en", "NZ")
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

internal fun getRegionLanguage(merchantLocale: Locale, clientLocale: Locale): Locale? {
    return validRegionLanguages[merchantLocale.country]?.find {
        clientLocale.language == it.language
    }
}

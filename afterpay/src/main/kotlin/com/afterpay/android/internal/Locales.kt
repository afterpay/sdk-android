/*
 * Copyright (C) 2024 Afterpay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afterpay.android.internal

import java.util.Locale

private val validRegionLanguages = mapOf(
    Locales.EN_AU.country to setOf(Locales.EN_AU),
    Locales.EN_GB.country to setOf(Locales.EN_GB),
    Locales.EN_NZ.country to setOf(Locales.EN_NZ),
    Locales.EN_US.country to setOf(Locales.EN_US),
    Locales.EN_CA.country to setOf(Locales.EN_CA, Locales.FR_CA),
)

internal object Locales {
    val EN_AU: Locale = Locale("en", "AU")
    val EN_CA: Locale = Locale.CANADA
    val FR_CA: Locale = Locale.CANADA_FRENCH
    val EN_NZ: Locale = Locale("en", "NZ")
    val EN_GB: Locale = Locale.UK
    val EN_US: Locale = Locale.US

    val validSet = setOf(
        EN_AU,
        EN_CA,
        FR_CA,
        EN_GB,
        EN_NZ,
        EN_US,
    )
}

internal fun getRegionLanguage(merchantLocale: Locale, consumerLocale: Locale): Locale? {
    return validRegionLanguages[merchantLocale.country]?.find {
        consumerLocale.language == it.language
    }
}

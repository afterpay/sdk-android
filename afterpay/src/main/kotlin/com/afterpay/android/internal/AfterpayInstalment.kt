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

import com.afterpay.android.model.Configuration
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

internal sealed class AfterpayInstalment {
  data class Available(
    val instalmentAmount: String,
  ) : AfterpayInstalment()

  data class NotAvailable(
    val minimumAmount: String?,
    val maximumAmount: String,
  ) : AfterpayInstalment()

  object NoConfiguration : AfterpayInstalment()

  companion object {
    fun of(totalCost: BigDecimal, configuration: Configuration?, clientLocale: Locale): AfterpayInstalment {
      if (configuration == null) {
        return NoConfiguration
      }

      val currencyLocales = Locales.validSet.filterTo(HashSet()) {
        Currency.getInstance(it) == configuration.currency
      }

      val currencyLocale: Locale = when {
        currencyLocales.isEmpty() -> {
          return NotAvailable(
            minimumAmount = null,
            maximumAmount = "0",
          )
        }
        currencyLocales.count() == 1 -> currencyLocales.first()
        currencyLocales.contains(configuration.locale) -> configuration.locale
        else -> Locales.validSet.first { Currency.getInstance(it) == configuration.currency }
      }

      val localCurrency = Currency.getInstance(clientLocale)
      val currencySymbol = configuration.currency.getSymbol(currencyLocale)

      val usCurrencySymbol = Currency.getInstance(Locales.EN_US).getSymbol(Locales.EN_US)
      val gbCurrencySymbol = Currency.getInstance(Locales.EN_GB).getSymbol(Locales.EN_GB)

      val currencyFormatter = (NumberFormat.getCurrencyInstance(clientLocale) as DecimalFormat).apply {
        this.currency = configuration.currency
      }

      if (clientLocale == Locales.EN_US) {
        currencyFormatter.apply {
          decimalFormatSymbols = decimalFormatSymbols.apply {
            this.currencySymbol = when (configuration.currency) {
              Currency.getInstance(Locales.EN_AU) -> "A$"
              Currency.getInstance(Locales.EN_NZ) -> "NZ$"
              Currency.getInstance(Locales.EN_CA) -> "CA$"
              Currency.getInstance(Locales.FR_CA) -> "CA$"
              else -> currencySymbol
            }
          }
        }
      } else if (configuration.currency != localCurrency) {
        currencyFormatter.apply {
          decimalFormatSymbols = decimalFormatSymbols.apply {
            this.currencySymbol = currencySymbol
          }

          when (currencySymbol) {
            usCurrencySymbol -> this.applyPattern("¤#,##0.00 ¤¤")
            gbCurrencySymbol -> this.applyPattern("¤#,##0.00")
          }
        }
      }

      val minimumAmount = configuration.minimumAmount ?: BigDecimal.ZERO
      if (totalCost < minimumAmount || totalCost > configuration.maximumAmount) {
        val currencyFormatterNoDecimals = currencyFormatter.clone() as DecimalFormat
        currencyFormatterNoDecimals.maximumFractionDigits = 0

        return NotAvailable(
          minimumAmount = configuration.minimumAmount?.let(currencyFormatterNoDecimals::format),
          maximumAmount = currencyFormatterNoDecimals.format(configuration.maximumAmount),
        )
      }

      val numberOfInstalments = numberOfInstalments(configuration.currency).toBigDecimal()
      val instalment = totalCost.divide(numberOfInstalments, 2, RoundingMode.HALF_EVEN)
      return Available(instalmentAmount = currencyFormatter.format(instalment))
    }

    fun numberOfInstalments(currency: Currency): Int {
      return 4
    }
  }
}

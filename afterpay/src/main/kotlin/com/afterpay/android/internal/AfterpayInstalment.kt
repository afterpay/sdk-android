package com.afterpay.android.internal

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency

internal sealed class AfterpayInstalment {
    data class Available(
        val instalmentAmount: String
    ) : AfterpayInstalment()

    data class NotAvailable(
        val minimumAmount: String?,
        val maximumAmount: String
    ) : AfterpayInstalment()

    object NoConfiguration : AfterpayInstalment()

    companion object {
        fun of(totalCost: BigDecimal, configuration: Configuration?): AfterpayInstalment {
            if (configuration == null) {
                return NoConfiguration
            }

            val currencyLocale = Locales.validSet.first { Currency.getInstance(it) == configuration.currency }
            val currencySymbol = configuration.currency.getSymbol(currencyLocale)
            val usCurrencySymbol = Currency.getInstance(Locales.US).getSymbol(Locales.US)
            val localCurrency = Currency.getInstance(configuration.locale)

            val currencyFormatter = (NumberFormat.getCurrencyInstance(currencyLocale) as DecimalFormat).apply {
                this.currency = configuration.currency
            }

            if (configuration.locale == Locales.US) {
                currencyFormatter.apply {
                    decimalFormatSymbols = decimalFormatSymbols.apply {
                        this.currencySymbol = when (configuration.currency) {
                            Currency.getInstance(Locales.AUSTRALIA) -> "A$"
                            Currency.getInstance(Locales.NEW_ZEALAND) -> "NZ$"
                            Currency.getInstance(Locales.CANADA) -> "CA$"
                            else -> currencySymbol
                        }
                    }
                }
            } else if (currencySymbol == usCurrencySymbol && configuration.currency != localCurrency) {
                currencyFormatter.apply {
                    this.applyPattern("¤#,##0.00 ¤¤")
                }
            }

            val minimumAmount = configuration.minimumAmount ?: BigDecimal.ZERO
            if (totalCost < minimumAmount || totalCost > configuration.maximumAmount) {
                return NotAvailable(
                    minimumAmount = configuration.minimumAmount?.let(currencyFormatter::format),
                    maximumAmount = currencyFormatter.format(configuration.maximumAmount)
                )
            }

            val instalment = (totalCost / 4.toBigDecimal()).setScale(2, RoundingMode.HALF_EVEN)
            return Available(instalmentAmount = currencyFormatter.format(instalment))
        }
    }
}

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

            val locale = configuration.locale
            val currency = configuration.currency
            val currencyFormatter: NumberFormat
            val isLocalCurrency = currency == Currency.getInstance(locale)

            if (locale == Locales.US || isLocalCurrency) {
                currencyFormatter = NumberFormat.getCurrencyInstance(locale)

                (currencyFormatter as DecimalFormat).apply {
                    this.currency = currency

                    if (locale == Locales.US) {
                        decimalFormatSymbols = decimalFormatSymbols.apply {
                            currencySymbol = when (currency.currencyCode) {
                                "AUD" -> "A$"
                                "NZD" -> "NZ$"
                                "CAD" -> "CA$"
                                "GBP" -> "£"
                                else -> currency.getSymbol(locale)
                            }
                        }
                    }
                }
            } else {
                val currencyLocale = Locales.validSet.first { Currency.getInstance(it) == currency }
                currencyFormatter = NumberFormat.getCurrencyInstance(currencyLocale)

                (currencyFormatter as DecimalFormat).apply {
                    this.currency = currency

                    if (!isLocalCurrency && currency.getSymbol(currencyLocale) == Currency.getInstance(Locales.US).getSymbol(Locales.US)) {
                        this.applyPattern("¤#,##0.00 ¤¤")
                    }
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

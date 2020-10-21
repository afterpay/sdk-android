package com.afterpay.android.internal

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat

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

            val currencyFormatter = (NumberFormat.getCurrencyInstance() as DecimalFormat).apply {
                currency = configuration.currency
                decimalFormatSymbols = decimalFormatSymbols.apply {
                    currencySymbol = when (configuration.currency.currencyCode) {
                        "AUD" -> "A$"
                        "NZD" -> "NZ$"
                        "CAD" -> "CA$"
                        else -> "$"
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

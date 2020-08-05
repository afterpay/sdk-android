package com.afterpay.android.internal

import com.afterpay.android.Afterpay
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat

internal sealed class AfterpayInstalment {
    data class Available(val instalmentCost: String) : AfterpayInstalment()
    object NoConfiguration : AfterpayInstalment()

    companion object {
        fun of(totalCost: BigDecimal): AfterpayInstalment {
            val currency = Afterpay.configuration?.currency ?: return NoConfiguration

            val currencyFormatter = NumberFormat.getCurrencyInstance().apply {
                this.currency = currency
            }
            val instalment = (totalCost / 4.toBigDecimal()).setScale(2, RoundingMode.HALF_EVEN)
            return Available(instalmentCost = currencyFormatter.format(instalment))
        }
    }
}

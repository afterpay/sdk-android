package com.afterpay.android

import com.afterpay.android.internal.AfterpayInstalment
import com.afterpay.android.internal.Locales
import com.afterpay.android.model.Configuration
import org.junit.Assert
import org.junit.Test
import java.math.BigDecimal
import java.util.Currency
import java.util.Locale

class AfterpayInstalmentPriceTest {
    private val oneHundredAndTwentyOne = 121.toBigDecimal()

    private val priceCasesDouble = mapOf(
        40.0 to "$10.00",
        40.2 to "$10.05",
        40.00 to "$10.00",
        40.01 to "$10.00",
        40.02 to "$10.00",
        40.03 to "$10.01",
        40.04 to "$10.01",
        40.009 to "$10.00",
        40.019 to "$10.00",
        40.3934567 to "$10.10",
    )

    private val priceCasesInt = mapOf(
        40 to "$10.00",
        41 to "$10.25",
        100 to "$25.00",
        103 to "$25.75",
    )

    @Test
    fun `available instalment double test cases display correctly`() {
        priceCasesDouble.forEach { (amount, instalmentAmount) ->
            val instalments = availableInstalment(
                amount.toBigDecimal(),
                Currency.getInstance("AUD"),
                Locales.EN_AU,
            )
            Assert.assertEquals(instalmentAmount, instalments.instalmentAmount)
        }
    }

    @Test
    fun `available instalment int test cases display correctly`() {
        priceCasesInt.forEach { (amount, instalmentAmount) ->
            val instalments = availableInstalment(
                amount.toBigDecimal(),
                Currency.getInstance("AUD"),
                Locales.EN_AU,
            )
            Assert.assertEquals(instalmentAmount, instalments.instalmentAmount)
        }
    }

    /**
     * This test was added because when using the / character to divide a BigDecimal
     * it uses the BigDecimal.div extension
     *
     * see here: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/java.math.-big-decimal/div.html
     *
     * Relevant section is: The scale of the result is the same as the scale of this (divident)
     */
    @Test
    fun `available instalment when amount is round and odd`() {
        val instalments = availableInstalment(
            oneHundredAndTwentyOne,
            Currency.getInstance("AUD"),
            Locales.EN_AU,
        )

        Assert.assertEquals("$30.25", instalments.instalmentAmount)
    }

    private fun availableInstalment(
        amount: BigDecimal,
        currency: Currency,
        locale: Locale,
    ): AfterpayInstalment.Available {
        val configuration = Configuration(
            2.toBigDecimal(),
            1000.toBigDecimal(),
            currency,
            locale,
            AfterpayEnvironment.SANDBOX,
        )
        return AfterpayInstalment.of(amount, configuration, locale) as AfterpayInstalment.Available
    }
}

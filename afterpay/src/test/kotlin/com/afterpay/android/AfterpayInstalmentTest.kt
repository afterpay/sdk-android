package com.afterpay.android

import com.afterpay.android.internal.AfterpayInstalment
import com.afterpay.android.internal.Configuration
import com.afterpay.android.internal.Locales
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.util.Currency
import java.util.Locale

class AfterpayInstalmentTest {
    private val australianDollar: Currency = Currency.getInstance("AUD")
    private val canadianDollar: Currency = Currency.getInstance("CAD")
    private val poundSterling: Currency = Currency.getInstance("GBP")
    private val newZealandDollar: Currency = Currency.getInstance("NZD")
    private val unitedStatesDollar: Currency = Currency.getInstance("USD")

    private val oneHundredAndTwenty = 120.toBigDecimal()
    private val oneHundredAndTwentyOne = 121.toBigDecimal()

    @Test
    fun `available instalment in Australia locale`() {
        val locale = Locales.EN_AU

        val audInstalment = availableInstalment(oneHundredAndTwenty, australianDollar, locale)
        val cadInstalment = availableInstalment(oneHundredAndTwenty, canadianDollar, locale)
        val gbpInstalment = availableInstalment(oneHundredAndTwenty, poundSterling, locale)
        val nzdInstalment = availableInstalment(oneHundredAndTwenty, newZealandDollar, locale)
        val usdInstalment = availableInstalment(oneHundredAndTwenty, unitedStatesDollar, locale)

        assertEquals("$30.00", audInstalment.instalmentAmount)
        assertEquals("$30.00 CAD", cadInstalment.instalmentAmount)
        assertEquals("£30.00", gbpInstalment.instalmentAmount)
        assertEquals("$30.00 NZD", nzdInstalment.instalmentAmount)
        assertEquals("$30.00 USD", usdInstalment.instalmentAmount)
    }

    @Test
    fun `available instalment in Canada locale`() {
        val locale = Locales.EN_CA

        val audInstalment = availableInstalment(oneHundredAndTwenty, australianDollar, locale)
        val cadInstalment = availableInstalment(oneHundredAndTwenty, canadianDollar, locale)
        val gbpInstalment = availableInstalment(oneHundredAndTwenty, poundSterling, locale)
        val nzdInstalment = availableInstalment(oneHundredAndTwenty, newZealandDollar, locale)
        val usdInstalment = availableInstalment(oneHundredAndTwenty, unitedStatesDollar, locale)

        assertEquals("$30.00 AUD", audInstalment.instalmentAmount)
        assertEquals("$30.00", cadInstalment.instalmentAmount)
        assertEquals("£30.00", gbpInstalment.instalmentAmount)
        assertEquals("$30.00 NZD", nzdInstalment.instalmentAmount)
        assertEquals("$30.00 USD", usdInstalment.instalmentAmount)
    }

    @Test
    fun `available instalment in UK locale`() {
        val locale = Locales.EN_GB

        val audInstalment = availableInstalment(oneHundredAndTwenty, australianDollar, locale)
        val cadInstalment = availableInstalment(oneHundredAndTwenty, canadianDollar, locale)
        val gbpInstalment = availableInstalment(oneHundredAndTwenty, poundSterling, locale)
        val nzdInstalment = availableInstalment(oneHundredAndTwenty, newZealandDollar, locale)
        val usdInstalment = availableInstalment(oneHundredAndTwenty, unitedStatesDollar, locale)

        assertEquals("$30.00 AUD", audInstalment.instalmentAmount)
        assertEquals("$30.00 CAD", cadInstalment.instalmentAmount)
        assertEquals("£30.00", gbpInstalment.instalmentAmount)
        assertEquals("$30.00 NZD", nzdInstalment.instalmentAmount)
        assertEquals("$30.00 USD", usdInstalment.instalmentAmount)
    }

    @Test
    fun `available instalment in New Zealand locale`() {
        val locale = Locales.EN_NZ

        val audInstalment = availableInstalment(oneHundredAndTwenty, australianDollar, locale)
        val cadInstalment = availableInstalment(oneHundredAndTwenty, canadianDollar, locale)
        val gbpInstalment = availableInstalment(oneHundredAndTwenty, poundSterling, locale)
        val nzdInstalment = availableInstalment(oneHundredAndTwenty, newZealandDollar, locale)
        val usdInstalment = availableInstalment(oneHundredAndTwenty, unitedStatesDollar, locale)

        assertEquals("$30.00 AUD", audInstalment.instalmentAmount)
        assertEquals("$30.00 CAD", cadInstalment.instalmentAmount)
        assertEquals("£30.00", gbpInstalment.instalmentAmount)
        assertEquals("$30.00", nzdInstalment.instalmentAmount)
        assertEquals("$30.00 USD", usdInstalment.instalmentAmount)
    }

    @Test
    fun `available instalment in US locale`() {
        val locale = Locales.EN_US

        val audInstalment = availableInstalment(oneHundredAndTwenty, australianDollar, locale)
        val cadInstalment = availableInstalment(oneHundredAndTwenty, canadianDollar, locale)
        val gbpInstalment = availableInstalment(oneHundredAndTwenty, poundSterling, locale)
        val nzdInstalment = availableInstalment(oneHundredAndTwenty, newZealandDollar, locale)
        val usdInstalment = availableInstalment(oneHundredAndTwenty, unitedStatesDollar, locale)

        assertEquals("A$30.00", audInstalment.instalmentAmount)
        assertEquals("CA$30.00", cadInstalment.instalmentAmount)
        assertEquals("£30.00", gbpInstalment.instalmentAmount)
        assertEquals("NZ$30.00", nzdInstalment.instalmentAmount)
        assertEquals("$30.00", usdInstalment.instalmentAmount)
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
        val locale = Locales.EN_AU

        val audInstalment = availableInstalment(oneHundredAndTwentyOne, australianDollar, locale)
        val cadInstalment = availableInstalment(oneHundredAndTwentyOne, canadianDollar, locale)
        val gbpInstalment = availableInstalment(oneHundredAndTwentyOne, poundSterling, locale)
        val nzdInstalment = availableInstalment(oneHundredAndTwentyOne, newZealandDollar, locale)
        val usdInstalment = availableInstalment(oneHundredAndTwentyOne, unitedStatesDollar, locale)

        assertEquals("$30.25", audInstalment.instalmentAmount)
        assertEquals("$30.25 CAD", cadInstalment.instalmentAmount)
        assertEquals("£30.25", gbpInstalment.instalmentAmount)
        assertEquals("$30.25 NZD", nzdInstalment.instalmentAmount)
        assertEquals("$30.25 USD", usdInstalment.instalmentAmount)
    }

    private fun availableInstalment(
        amount: BigDecimal,
        currency: Currency,
        locale: Locale
    ): AfterpayInstalment.Available {
        val configuration = Configuration(
            50.toBigDecimal(),
            1000.toBigDecimal(),
            currency,
            locale,
            AfterpayEnvironment.SANDBOX
        )
        return AfterpayInstalment.of(amount, configuration) as AfterpayInstalment.Available
    }
}

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

    @Test
    fun `available instalment in Australia locale`() {
        val locale = Locales.AUSTRALIA

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
        val locale = Locales.CANADA

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
        val locale = Locales.UK

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
        val locale = Locales.NEW_ZEALAND

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
        val locale = Locales.US

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

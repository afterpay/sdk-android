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
    private val euro: Currency = Currency.getInstance("EUR")

    private val oneHundredAndTwenty = 120.toBigDecimal()
    private val oneHundredAndTwentyOne = 121.toBigDecimal()

    @Test
    fun `available instalment in en-AU locale`() {
        val instalments = createAllInstalments(oneHundredAndTwenty, Locales.EN_AU)

        assertEquals("$30.00", instalments.aud.instalmentAmount)
        assertEquals("$30.00 CAD", instalments.cad.instalmentAmount)
        assertEquals("£30.00", instalments.gbp.instalmentAmount)
        assertEquals("$30.00 NZD", instalments.nzd.instalmentAmount)
        assertEquals("$30.00 USD", instalments.usd.instalmentAmount)
        assertEquals("30.00€", instalments.eur.instalmentAmount)
    }

    @Test
    fun `available instalment in en-CA locale`() {
        val instalments = createAllInstalments(oneHundredAndTwenty, Locales.EN_CA)

        assertEquals("$30.00 AUD", instalments.aud.instalmentAmount)
        assertEquals("$30.00", instalments.cad.instalmentAmount)
        assertEquals("£30.00", instalments.gbp.instalmentAmount)
        assertEquals("$30.00 NZD", instalments.nzd.instalmentAmount)
        assertEquals("$30.00 USD", instalments.usd.instalmentAmount)
        assertEquals("30.00€", instalments.eur.instalmentAmount)
    }

    @Test
    fun `available instalment in fr-CA locale`() {
        val instalments = createAllInstalments(oneHundredAndTwenty, Locales.FR_CA)

        assertEquals("$30,00 AUD", instalments.aud.instalmentAmount)
        assertEquals("30,00 $", instalments.cad.instalmentAmount)
        assertEquals("£30,00", instalments.gbp.instalmentAmount)
        assertEquals("$30,00 NZD", instalments.nzd.instalmentAmount)
        assertEquals("$30,00 USD", instalments.usd.instalmentAmount)
        assertEquals("30,00€", instalments.eur.instalmentAmount)
    }

    @Test
    fun `available instalment in en-GB locale`() {
        val instalments = createAllInstalments(oneHundredAndTwenty, Locales.EN_GB)

        assertEquals("$30.00 AUD", instalments.aud.instalmentAmount)
        assertEquals("$30.00 CAD", instalments.cad.instalmentAmount)
        assertEquals("£30.00", instalments.gbp.instalmentAmount)
        assertEquals("$30.00 NZD", instalments.nzd.instalmentAmount)
        assertEquals("$30.00 USD", instalments.usd.instalmentAmount)
        assertEquals("30.00€", instalments.eur.instalmentAmount)
    }

    @Test
    fun `available instalment in en-NZ locale`() {
        val instalments = createAllInstalments(oneHundredAndTwenty, Locales.EN_NZ)

        assertEquals("$30.00 AUD", instalments.aud.instalmentAmount)
        assertEquals("$30.00 CAD", instalments.cad.instalmentAmount)
        assertEquals("£30.00", instalments.gbp.instalmentAmount)
        assertEquals("$30.00", instalments.nzd.instalmentAmount)
        assertEquals("$30.00 USD", instalments.usd.instalmentAmount)
        assertEquals("30.00€", instalments.eur.instalmentAmount)
    }

    @Test
    fun `available instalment in en-US locale`() {
        val instalments = createAllInstalments(oneHundredAndTwenty, Locales.EN_US)

        assertEquals("A$30.00", instalments.aud.instalmentAmount)
        assertEquals("CA$30.00", instalments.cad.instalmentAmount)
        assertEquals("£30.00", instalments.gbp.instalmentAmount)
        assertEquals("NZ$30.00", instalments.nzd.instalmentAmount)
        assertEquals("$30.00", instalments.usd.instalmentAmount)
        assertEquals("30.00€", instalments.eur.instalmentAmount)
    }

    @Test
    fun `available instalment in it-IT locale`() {
        val instalments = createAllInstalments(oneHundredAndTwenty, Locales.IT_IT)

        assertEquals("$30,00 AUD", instalments.aud.instalmentAmount)
        assertEquals("$30,00 CAD", instalments.cad.instalmentAmount)
        assertEquals("£30,00", instalments.gbp.instalmentAmount)
        assertEquals("$30,00 NZD", instalments.nzd.instalmentAmount)
        assertEquals("$30,00 USD", instalments.usd.instalmentAmount)
        assertEquals("30,00 €", instalments.eur.instalmentAmount)
    }

    @Test
    fun `available instalment in fr-FR locale`() {
        val instalments = createAllInstalments(oneHundredAndTwenty, Locales.FR_FR)

        assertEquals("$30,00 AUD", instalments.aud.instalmentAmount)
        assertEquals("$30,00 CAD", instalments.cad.instalmentAmount)
        assertEquals("£30,00", instalments.gbp.instalmentAmount)
        assertEquals("$30,00 NZD", instalments.nzd.instalmentAmount)
        assertEquals("$30,00 USD", instalments.usd.instalmentAmount)
        assertEquals("30,00 €", instalments.eur.instalmentAmount)
    }

    @Test
    fun `available instalment in es-ES locale`() {
        val instalments = createAllInstalments(oneHundredAndTwenty, Locales.ES_ES)

        assertEquals("$30,00 AUD", instalments.aud.instalmentAmount)
        assertEquals("$30,00 CAD", instalments.cad.instalmentAmount)
        assertEquals("£30,00", instalments.gbp.instalmentAmount)
        assertEquals("$30,00 NZD", instalments.nzd.instalmentAmount)
        assertEquals("$30,00 USD", instalments.usd.instalmentAmount)
        assertEquals("30,00 €", instalments.eur.instalmentAmount)
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
        val instalments = createAllInstalments(oneHundredAndTwentyOne, Locales.EN_AU)

        assertEquals("$30.25", instalments.aud.instalmentAmount)
        assertEquals("$30.25 CAD", instalments.cad.instalmentAmount)
        assertEquals("£30.25", instalments.gbp.instalmentAmount)
        assertEquals("$30.25 NZD", instalments.nzd.instalmentAmount)
        assertEquals("$30.25 USD", instalments.usd.instalmentAmount)
        assertEquals("30.25€", instalments.eur.instalmentAmount)
    }

    private data class AllInstallments(
        val aud: AfterpayInstalment.Available,
        val cad: AfterpayInstalment.Available,
        val gbp: AfterpayInstalment.Available,
        val nzd: AfterpayInstalment.Available,
        val usd: AfterpayInstalment.Available,
        val eur: AfterpayInstalment.Available
    )

    private fun createAllInstalments(amount: BigDecimal, locale: Locale): AllInstallments {
        return AllInstallments(
            aud = availableInstalment(amount, australianDollar, locale),
            cad = availableInstalment(amount, canadianDollar, locale),
            gbp = availableInstalment(amount, poundSterling, locale),
            nzd = availableInstalment(amount, newZealandDollar, locale),
            usd = availableInstalment(amount, unitedStatesDollar, locale),
            eur = availableInstalment(amount, euro, locale)
        )
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
        return AfterpayInstalment.of(amount, configuration, locale) as AfterpayInstalment.Available
    }
}

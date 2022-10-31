package com.afterpay.android

import com.afterpay.android.internal.AfterpayInstalment
import com.afterpay.android.internal.Configuration
import com.afterpay.android.internal.Locales
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.util.Currency
import java.util.Locale

class AfterpayInstalmentLocaleTest {
    private val australianDollar: Currency = Currency.getInstance("AUD")
    private val canadianDollar: Currency = Currency.getInstance("CAD")
    private val poundSterling: Currency = Currency.getInstance("GBP")
    private val newZealandDollar: Currency = Currency.getInstance("NZD")
    private val unitedStatesDollar: Currency = Currency.getInstance("USD")
    private val euro: Currency = Currency.getInstance("EUR")

    private val oneHundredAndTwenty = 120.toBigDecimal()

    @Test
    fun `available instalment in en-AU locale`() {
        val instalments = createAllAvailableInstalments(oneHundredAndTwenty, Locales.EN_AU)

        assertEquals("$30.00", instalments.aud.instalmentAmount)
        assertEquals("$30.00 CAD", instalments.cad.instalmentAmount)
        assertEquals("£30.00", instalments.gbp.instalmentAmount)
        assertEquals("$30.00 NZD", instalments.nzd.instalmentAmount)
        assertEquals("$30.00 USD", instalments.usd.instalmentAmount)
        assertEquals("40.00€", instalments.eur.instalmentAmount)
    }

    @Test
    fun `unavailable instalment in en-AU locale`() {
        val instalments = createAllUnavailableInstalments(oneHundredAndTwenty, Locales.EN_AU)

        assertEquals("$10", instalments.aud.minimumAmount)
        assertEquals("$10 CAD", instalments.cad.minimumAmount)
        assertEquals("£10", instalments.gbp.minimumAmount)
        assertEquals("$10 NZD", instalments.nzd.minimumAmount)
        assertEquals("$10 USD", instalments.usd.minimumAmount)
        assertEquals("10€", instalments.eur.minimumAmount)
    }

    @Test
    fun `available instalment in en-CA locale`() {
        val instalments = createAllAvailableInstalments(oneHundredAndTwenty, Locales.EN_CA)

        assertEquals("$30.00 AUD", instalments.aud.instalmentAmount)
        assertEquals("$30.00", instalments.cad.instalmentAmount)
        assertEquals("£30.00", instalments.gbp.instalmentAmount)
        assertEquals("$30.00 NZD", instalments.nzd.instalmentAmount)
        assertEquals("$30.00 USD", instalments.usd.instalmentAmount)
        assertEquals("40.00€", instalments.eur.instalmentAmount)
    }

    @Test
    fun `unavailable instalment in en-CA locale`() {
        val instalments = createAllUnavailableInstalments(oneHundredAndTwenty, Locales.EN_CA)

        assertEquals("$10 AUD", instalments.aud.minimumAmount)
        assertEquals("$10", instalments.cad.minimumAmount)
        assertEquals("£10", instalments.gbp.minimumAmount)
        assertEquals("$10 NZD", instalments.nzd.minimumAmount)
        assertEquals("$10 USD", instalments.usd.minimumAmount)
        assertEquals("10€", instalments.eur.minimumAmount)
    }

    @Test
    fun `available instalment in fr-CA locale`() {
        val instalments = createAllAvailableInstalments(oneHundredAndTwenty, Locales.FR_CA)

        assertEquals("$30,00 AUD", instalments.aud.instalmentAmount)
        assertEquals("30,00 $", instalments.cad.instalmentAmount)
        assertEquals("£30,00", instalments.gbp.instalmentAmount)
        assertEquals("$30,00 NZD", instalments.nzd.instalmentAmount)
        assertEquals("$30,00 USD", instalments.usd.instalmentAmount)
        assertEquals("40,00€", instalments.eur.instalmentAmount)
    }

    @Test
    fun `unavailable instalment in fr-CA locale`() {
        val instalments = createAllUnavailableInstalments(oneHundredAndTwenty, Locales.FR_CA)

        assertEquals("$10 AUD", instalments.aud.minimumAmount)
        assertEquals("10 $", instalments.cad.minimumAmount)
        assertEquals("£10", instalments.gbp.minimumAmount)
        assertEquals("$10 NZD", instalments.nzd.minimumAmount)
        assertEquals("$10 USD", instalments.usd.minimumAmount)
        assertEquals("10€", instalments.eur.minimumAmount)
    }

    @Test
    fun `available instalment in en-GB locale`() {
        val instalments = createAllAvailableInstalments(oneHundredAndTwenty, Locales.EN_GB)

        assertEquals("$30.00 AUD", instalments.aud.instalmentAmount)
        assertEquals("$30.00 CAD", instalments.cad.instalmentAmount)
        assertEquals("£30.00", instalments.gbp.instalmentAmount)
        assertEquals("$30.00 NZD", instalments.nzd.instalmentAmount)
        assertEquals("$30.00 USD", instalments.usd.instalmentAmount)
        assertEquals("40.00€", instalments.eur.instalmentAmount)
    }

    @Test
    fun `unavailable instalment in en-GB locale`() {
        val instalments = createAllUnavailableInstalments(oneHundredAndTwenty, Locales.EN_GB)

        assertEquals("$10 AUD", instalments.aud.minimumAmount)
        assertEquals("$10 CAD", instalments.cad.minimumAmount)
        assertEquals("£10", instalments.gbp.minimumAmount)
        assertEquals("$10 NZD", instalments.nzd.minimumAmount)
        assertEquals("$10 USD", instalments.usd.minimumAmount)
        assertEquals("10€", instalments.eur.minimumAmount)
    }

    @Test
    fun `available instalment in en-NZ locale`() {
        val instalments = createAllAvailableInstalments(oneHundredAndTwenty, Locales.EN_NZ)

        assertEquals("$30.00 AUD", instalments.aud.instalmentAmount)
        assertEquals("$30.00 CAD", instalments.cad.instalmentAmount)
        assertEquals("£30.00", instalments.gbp.instalmentAmount)
        assertEquals("$30.00", instalments.nzd.instalmentAmount)
        assertEquals("$30.00 USD", instalments.usd.instalmentAmount)
        assertEquals("40.00€", instalments.eur.instalmentAmount)
    }

    @Test
    fun `unavailable instalment in en-NZ locale`() {
        val instalments = createAllUnavailableInstalments(oneHundredAndTwenty, Locales.EN_NZ)

        assertEquals("$10 AUD", instalments.aud.minimumAmount)
        assertEquals("$10 CAD", instalments.cad.minimumAmount)
        assertEquals("£10", instalments.gbp.minimumAmount)
        assertEquals("$10", instalments.nzd.minimumAmount)
        assertEquals("$10 USD", instalments.usd.minimumAmount)
        assertEquals("10€", instalments.eur.minimumAmount)
    }

    @Test
    fun `available instalment in en-US locale`() {
        val instalments = createAllAvailableInstalments(oneHundredAndTwenty, Locales.EN_US)

        assertEquals("A$30.00", instalments.aud.instalmentAmount)
        assertEquals("CA$30.00", instalments.cad.instalmentAmount)
        assertEquals("£30.00", instalments.gbp.instalmentAmount)
        assertEquals("NZ$30.00", instalments.nzd.instalmentAmount)
        assertEquals("$30.00", instalments.usd.instalmentAmount)
        assertEquals("40.00€", instalments.eur.instalmentAmount)
    }

    @Test
    fun `unavailable instalment in en-US locale`() {
        val instalments = createAllUnavailableInstalments(oneHundredAndTwenty, Locales.EN_US)

        assertEquals("A$10", instalments.aud.minimumAmount)
        assertEquals("CA$10", instalments.cad.minimumAmount)
        assertEquals("£10", instalments.gbp.minimumAmount)
        assertEquals("NZ$10", instalments.nzd.minimumAmount)
        assertEquals("$10", instalments.usd.minimumAmount)
        assertEquals("10€", instalments.eur.minimumAmount)
    }

    @Test
    fun `available instalment in it-IT locale`() {
        val instalments = createAllAvailableInstalments(oneHundredAndTwenty, Locales.IT_IT)

        assertEquals("$30,00 AUD", instalments.aud.instalmentAmount)
        assertEquals("$30,00 CAD", instalments.cad.instalmentAmount)
        assertEquals("£30,00", instalments.gbp.instalmentAmount)
        assertEquals("$30,00 NZD", instalments.nzd.instalmentAmount)
        assertEquals("$30,00 USD", instalments.usd.instalmentAmount)
        assertEquals("40,00 €", instalments.eur.instalmentAmount)
    }

    @Test
    fun `unavailable instalment in it-IT locale`() {
        val instalments = createAllUnavailableInstalments(oneHundredAndTwenty, Locales.IT_IT)

        assertEquals("$10 AUD", instalments.aud.minimumAmount)
        assertEquals("$10 CAD", instalments.cad.minimumAmount)
        assertEquals("£10", instalments.gbp.minimumAmount)
        assertEquals("$10 NZD", instalments.nzd.minimumAmount)
        assertEquals("$10 USD", instalments.usd.minimumAmount)
        assertEquals("10 €", instalments.eur.minimumAmount)
    }

    @Test
    fun `available instalment in fr-FR locale`() {
        val instalments = createAllAvailableInstalments(oneHundredAndTwenty, Locales.FR_FR)

        assertEquals("$30,00 AUD", instalments.aud.instalmentAmount)
        assertEquals("$30,00 CAD", instalments.cad.instalmentAmount)
        assertEquals("£30,00", instalments.gbp.instalmentAmount)
        assertEquals("$30,00 NZD", instalments.nzd.instalmentAmount)
        assertEquals("$30,00 USD", instalments.usd.instalmentAmount)
        assertEquals("40,00 €", instalments.eur.instalmentAmount)
    }

    @Test
    fun `unavailable instalment in fr-FR locale`() {
        val instalments = createAllUnavailableInstalments(oneHundredAndTwenty, Locales.FR_FR)

        assertEquals("$10 AUD", instalments.aud.minimumAmount)
        assertEquals("$10 CAD", instalments.cad.minimumAmount)
        assertEquals("£10", instalments.gbp.minimumAmount)
        assertEquals("$10 NZD", instalments.nzd.minimumAmount)
        assertEquals("$10 USD", instalments.usd.minimumAmount)
        assertEquals("10 €", instalments.eur.minimumAmount)
    }

    @Test
    fun `available instalment in es-ES locale`() {
        val instalments = createAllAvailableInstalments(oneHundredAndTwenty, Locales.ES_ES)

        assertEquals("$30,00 AUD", instalments.aud.instalmentAmount)
        assertEquals("$30,00 CAD", instalments.cad.instalmentAmount)
        assertEquals("£30,00", instalments.gbp.instalmentAmount)
        assertEquals("$30,00 NZD", instalments.nzd.instalmentAmount)
        assertEquals("$30,00 USD", instalments.usd.instalmentAmount)
        assertEquals("40,00 €", instalments.eur.instalmentAmount)
    }

    @Test
    fun `unavailable instalment in es-ES locale`() {
        val instalments = createAllUnavailableInstalments(oneHundredAndTwenty, Locales.ES_ES)

        assertEquals("$10 AUD", instalments.aud.minimumAmount)
        assertEquals("$10 CAD", instalments.cad.minimumAmount)
        assertEquals("£10", instalments.gbp.minimumAmount)
        assertEquals("$10 NZD", instalments.nzd.minimumAmount)
        assertEquals("$10 USD", instalments.usd.minimumAmount)
        assertEquals("10 €", instalments.eur.minimumAmount)
    }

    private data class AllAvailableInstallments(
        val aud: AfterpayInstalment.Available,
        val cad: AfterpayInstalment.Available,
        val gbp: AfterpayInstalment.Available,
        val nzd: AfterpayInstalment.Available,
        val usd: AfterpayInstalment.Available,
        val eur: AfterpayInstalment.Available,
    )

    private data class AllUnavailableInstallments(
        val aud: AfterpayInstalment.NotAvailable,
        val cad: AfterpayInstalment.NotAvailable,
        val gbp: AfterpayInstalment.NotAvailable,
        val nzd: AfterpayInstalment.NotAvailable,
        val usd: AfterpayInstalment.NotAvailable,
        val eur: AfterpayInstalment.NotAvailable,
    )

    private fun createAllAvailableInstalments(amount: BigDecimal, locale: Locale): AllAvailableInstallments {
        return AllAvailableInstallments(
            aud = availableInstalment(amount, australianDollar, locale),
            cad = availableInstalment(amount, canadianDollar, locale),
            gbp = availableInstalment(amount, poundSterling, locale),
            nzd = availableInstalment(amount, newZealandDollar, locale),
            usd = availableInstalment(amount, unitedStatesDollar, locale),
            eur = availableInstalment(amount, euro, locale),
        )
    }

    private fun createAllUnavailableInstalments(amount: BigDecimal, locale: Locale): AllUnavailableInstallments {
        return AllUnavailableInstallments(
            aud = unavailableInstalment(amount, australianDollar, locale),
            cad = unavailableInstalment(amount, canadianDollar, locale),
            gbp = unavailableInstalment(amount, poundSterling, locale),
            nzd = unavailableInstalment(amount, newZealandDollar, locale),
            usd = unavailableInstalment(amount, unitedStatesDollar, locale),
            eur = unavailableInstalment(amount, euro, locale),
        )
    }

    private fun availableInstalment(
        amount: BigDecimal,
        currency: Currency,
        locale: Locale,
    ): AfterpayInstalment.Available {
        val configuration = Configuration(
            50.toBigDecimal(),
            1000.toBigDecimal(),
            currency,
            locale,
            AfterpayEnvironment.SANDBOX,
        )
        return AfterpayInstalment.of(amount, configuration, locale) as AfterpayInstalment.Available
    }

    private fun unavailableInstalment(
        amount: BigDecimal,
        currency: Currency,
        locale: Locale,
    ): AfterpayInstalment.NotAvailable {
        val configuration = Configuration(
            10.toBigDecimal(),
            20.toBigDecimal(),
            currency,
            locale,
            AfterpayEnvironment.SANDBOX,
        )
        return AfterpayInstalment.of(amount, configuration, locale) as AfterpayInstalment.NotAvailable
    }
}

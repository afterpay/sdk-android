package com.afterpay.android

import com.afterpay.android.internal.Locales
import org.junit.Assert
import org.junit.Test
import java.util.Locale

class AfterpayEnabled {
    private val environment = AfterpayEnvironment.SANDBOX

    private val validMerchantLocales: Array<Locale> = arrayOf(
        Locales.EN_AU,
        Locales.EN_CA,
        Locales.EN_GB,
        Locales.EN_US,
        Locales.EN_NZ,
        Locales.FR_CA,
    )

    @Test
    fun `Afterpay is enabled for basic config and locale is English`() {
        Afterpay.setConfiguration(
            minimumAmount = "10.00",
            maximumAmount = "100.00",
            currencyCode = "AUD",
            locale = Locale.US,
            environment = environment,
            consumerLocale = Locale.ENGLISH,
        )

        Assert.assertEquals(true, Afterpay.enabled)
    }

    @Test
    fun `Afterpay is not enabled for basic config and language is not available for merchant country`() {
        Afterpay.setConfiguration(
            minimumAmount = "10.00",
            maximumAmount = "100.00",
            currencyCode = "AUD",
            locale = Locale.US,
            environment = environment,
            consumerLocale = Locale.FRANCE,
        )

        Assert.assertEquals(false, Afterpay.enabled)
    }

    @Test
    fun `Afterpay is enabled for merchant locales`() {
        for (locale in validMerchantLocales) {
            Afterpay.setConfiguration(
                minimumAmount = "10.00",
                maximumAmount = "1000.00",
                currencyCode = "USD",
                locale = locale,
                environment = environment,
                consumerLocale = Locale.US,
            )

            Assert.assertEquals(true, Afterpay.enabled)
        }
    }
}

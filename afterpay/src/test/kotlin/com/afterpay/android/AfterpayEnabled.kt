package com.afterpay.android

import org.junit.Assert
import org.junit.Test
import java.util.Locale

class AfterpayEnabled {
    private val environment = AfterpayEnvironment.SANDBOX

    @Test
    fun `Afterpay is enabled for basic config and locale is English`() {
        Afterpay.setConfiguration(
            minimumAmount = "10.00",
            maximumAmount = "100.00",
            currencyCode = "AUD",
            locale = Locale.US,
            environment = environment,
            consumerLocale = Locale.ENGLISH
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
            consumerLocale = Locale.FRANCE
        )

        Assert.assertEquals(false, Afterpay.enabled)
    }
}

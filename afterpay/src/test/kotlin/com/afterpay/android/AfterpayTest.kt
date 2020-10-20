package com.afterpay.android

import org.junit.Assert.assertThrows
import org.junit.Test
import java.lang.IllegalArgumentException
import java.lang.NumberFormatException
import java.util.Locale

class AfterpayTest {
    @Test
    fun `setConfiguration does not throw for valid configuration`() {
        Afterpay.setConfiguration(
            minimumAmount = "10.00",
            maximumAmount = "100.00",
            currencyCode = "AUD",
            locale = Locale.US
        )
    }

    @Test
    fun `setConfiguration does not throw for valid configuration with no minimum amount`() {
        Afterpay.setConfiguration(
            minimumAmount = null,
            maximumAmount = "100.00",
            currencyCode = "AUD",
            locale = Locale.US
        )
    }

    @Test
    fun `setConfiguration throws for invalid currency code`() {
        assertThrows(IllegalArgumentException::class.java) {
            Afterpay.setConfiguration(
                minimumAmount = "10.00",
                maximumAmount = "100.00",
                currencyCode = "foo",
                locale = Locale.US
            )
        }
    }

    @Test
    fun `setConfiguration throws for invalid minimum order amount`() {
        assertThrows(NumberFormatException::class.java) {
            Afterpay.setConfiguration(
                minimumAmount = "foo",
                maximumAmount = "100.00",
                currencyCode = "AUD",
                locale = Locale.US
            )
        }
    }

    @Test
    fun `setConfiguration throws for invalid maximum order amount`() {
        assertThrows(NumberFormatException::class.java) {
            Afterpay.setConfiguration(
                minimumAmount = "10.00",
                maximumAmount = "foo",
                currencyCode = "AUD",
                locale = Locale.US
            )
        }
    }

    @Test
    fun `setConfiguration throws for minimum order amount less than zero`() {
        assertThrows(IllegalArgumentException::class.java) {
            Afterpay.setConfiguration(
                minimumAmount = "-10.00",
                maximumAmount = "100.00",
                currencyCode = "AUD",
                locale = Locale.US
            )
        }
    }

    @Test
    fun `setConfiguration throws for minimum order amount greater than maximum amount`() {
        assertThrows(IllegalArgumentException::class.java) {
            Afterpay.setConfiguration(
                minimumAmount = "110.00",
                maximumAmount = "100.00",
                currencyCode = "AUD",
                locale = Locale.US
            )
        }
    }

    @Test
    fun `setConfiguration throws for maximum order amount less than zero`() {
        assertThrows(IllegalArgumentException::class.java) {
            Afterpay.setConfiguration(
                minimumAmount = null,
                maximumAmount = "-2.00",
                currencyCode = "AUD",
                locale = Locale.US
            )
        }
    }
}

package com.afterpay.android

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AfterpayCashAppExceptionTests {
    @Test
    fun `should throw on signCashAppOrder if handler not setup`() {
        val exception = assertThrows(Exception::class.java) {
            runTest {
                Afterpay.signCashAppOrderToken("123")
            }
        }

        assertEquals("cashAppHandler or the handler parameter must be set and not null before attempting to sign a Cash App order", exception.message)
    }
}

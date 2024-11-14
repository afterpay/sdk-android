/*
 * Copyright (C) 2024 Afterpay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afterpay.android

import org.junit.Assert
import org.junit.Assert.assertThrows
import org.junit.Test
import java.util.Locale

class AfterpayTest {

  private val environment = AfterpayEnvironment.SANDBOX

  private val invalidMerchantLocales: Array<Locale> = arrayOf(
    Locale.ITALY,
    Locale.FRANCE,
    Locale("es", "ES"),
    Locale.JAPAN,
  )

  @Test
  fun `setConfiguration does not throw for valid configuration`() {
    Afterpay.setConfiguration(
      minimumAmount = "10.00",
      maximumAmount = "100.00",
      currencyCode = "AUD",
      locale = Locale.US,
      environment = environment,
    )
  }

  @Test
  fun `setConfiguration does not throw for valid configuration with no minimum amount`() {
    Afterpay.setConfiguration(
      minimumAmount = null,
      maximumAmount = "100.00",
      currencyCode = "AUD",
      locale = Locale.US,
      environment = environment,
    )
  }

  @Test
  fun `setConfiguration throws for invalid currency code`() {
    assertThrows(IllegalArgumentException::class.java) {
      Afterpay.setConfiguration(
        minimumAmount = "10.00",
        maximumAmount = "100.00",
        currencyCode = "foo",
        locale = Locale.US,
        environment = environment,
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
        locale = Locale.US,
        environment = environment,
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
        locale = Locale.US,
        environment = environment,
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
        locale = Locale.US,
        environment = environment,
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
        locale = Locale.US,
        environment = environment,
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
        locale = Locale.US,
        environment = environment,
      )
    }
  }

  @Test
  fun `setConfiguration throws for a locale not in the valid set`() {
    assertThrows(IllegalArgumentException::class.java) {
      for (locale in invalidMerchantLocales) {
        Afterpay.setConfiguration(
          minimumAmount = "10.00",
          maximumAmount = "1000.00",
          currencyCode = "USD",
          locale = locale,
          environment = environment,
        )

        Assert.assertEquals(false, Afterpay.enabled)
      }
    }
  }
}

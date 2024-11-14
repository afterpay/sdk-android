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
package com.afterpay.android.internal

import com.afterpay.android.model.AfterpayRegion
import com.afterpay.android.model.CheckoutV3Configuration
import com.afterpay.android.model.CheckoutV3Consumer
import com.afterpay.android.model.CheckoutV3Contact
import com.afterpay.android.model.CheckoutV3Item
import com.afterpay.android.model.Money
import com.afterpay.android.model.OrderTotal
import com.afterpay.android.model.VirtualCard
import kotlinx.serialization.Serializable
import java.util.Currency

internal object CheckoutV3 {
  @Serializable
  data class MerchantReferenceUpdate(
    val merchantReference: String,
    val token: String,
    val singleUseCardToken: String,
    val ppaConfirmToken: String,
  )

  @Serializable
  data class Response(
    val token: String,
    val confirmMustBeCalledBefore: String?,
    val redirectCheckoutUrl: String,
    val singleUseCardToken: String,
  )

  @Serializable
  data class Request(
    val shopDirectoryId: String,
    val shopDirectoryMerchantId: String,

    val amount: Money,
    val shippingAmount: Money?,
    val taxAmount: Money?,

    val items: List<Item>,
    val consumer: Consumer,
    val merchant: Merchant,
    val shipping: Contact?,
    val billing: Contact?,
    val isCashAppPay: Boolean?,
  ) {
    companion object {
      @JvmStatic
      fun create(
        consumer: CheckoutV3Consumer,
        isCashAppPay: Boolean?,
        orderTotal: OrderTotal,
        items: Array<CheckoutV3Item>,
        configuration: CheckoutV3Configuration,
      ): Request {
        val currency = Currency.getInstance(configuration.region.currencyCode)

        return Request(
          shopDirectoryId = configuration.shopDirectoryId,
          shopDirectoryMerchantId = configuration.shopDirectoryMerchantId,
          amount = Money(
            orderTotal.total,
            currency,
          ),
          shippingAmount = Money(orderTotal.shipping, currency),
          taxAmount = Money(orderTotal.tax, currency),
          items = items.map { Item.create(it, configuration.region) },
          consumer = Consumer(
            email = consumer.email,
            givenNames = consumer.givenNames,
            surname = consumer.surname,
            phoneNumber = consumer.phoneNumber,
          ),
          merchant = Merchant(
            redirectConfirmUrl = "https://static.afterpay.com",
            redirectCancelUrl = "https://static.afterpay.com",
          ),
          shipping = Contact.create(consumer.shippingInformation),
          billing = Contact.create(consumer.billingInformation),
          // server only handles true or null
          isCashAppPay = isCashAppPay?.let { if (!it) null else true },
        )
      }
    }
  }

  @Serializable
  data class Item(
    val name: String,
    val quantity: UInt,
    val price: Money,
    val sku: String?,
    val pageUrl: String?,
    val imageUrl: String?,
    val categories: List<List<String>>?,
    val estimatedShipmentDate: String?,
  ) {
    companion object {
      @JvmStatic
      fun create(item: CheckoutV3Item, region: AfterpayRegion): Item {
        val currency = Currency.getInstance(region.currencyCode)
        return Item(
          name = item.name,
          quantity = item.quantity,
          price = Money(item.price, currency),
          sku = item.sku,
          pageUrl = item.pageUrl.toString(),
          imageUrl = item.imageUrl.toString(),
          categories = item.categories,
          estimatedShipmentDate = item.estimatedShipmentDate,
        )
      }
    }
  }

  @Serializable
  data class Merchant(
    val redirectConfirmUrl: String,
    val redirectCancelUrl: String,
  )

  @Serializable
  data class Consumer(
    val email: String,
    val givenNames: String?,
    val surname: String?,
    val phoneNumber: String?,
  )

  @Serializable
  data class Contact(
    val name: String,
    val line1: String,
    val line2: String?,
    val area1: String?,
    val area2: String?,
    val region: String?,
    val postcode: String?,
    val countryCode: String,
    val phoneNumber: String?,
  ) {
    companion object {
      @JvmStatic
      fun create(contact: CheckoutV3Contact?): Contact? {
        contact ?: return null
        return Contact(
          name = contact.name,
          line1 = contact.line1,
          line2 = contact.line2,
          area1 = contact.area1,
          area2 = contact.area2,
          region = contact.region,
          postcode = contact.postcode,
          countryCode = contact.countryCode,
          phoneNumber = contact.phoneNumber,
        )
      }
    }
  }

  object Confirmation {
    @Serializable
    data class CashAppPayRequest(
      val token: String,
      val singleUseCardToken: String,
      val cashAppPspInfo: CashAppPspInfo,
    ) {
      @Serializable
      data class CashAppPspInfo(
        val externalCustomerId: String,
        val externalGrantId: String,
        val jwt: String,
      )
    }

    @Serializable
    data class CashAppPayResponse(
      val paymentDetails: PaymentDetails,
      val cardValidUntil: String?,
    )

    @Serializable
    data class Response(
      val paymentDetails: PaymentDetails,
      val cardValidUntil: String?,
      val authToken: String,
    )

    @Serializable
    data class PaymentDetails(
      val virtualCard: VirtualCard.Card? = null,
      val virtualCardToken: VirtualCard.TokenizedCard? = null,
    )
  }
}

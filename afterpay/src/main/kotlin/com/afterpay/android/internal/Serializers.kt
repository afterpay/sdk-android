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

import com.afterpay.android.model.VirtualCard
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency

internal object MoneyBigDecimalSerializer : KSerializer<BigDecimal> {

  override val descriptor = PrimitiveSerialDescriptor(
    serialName = "BigDecimal",
    kind = PrimitiveKind.STRING,
  )

  override fun deserialize(decoder: Decoder) = decoder.decodeString().toBigDecimal()

  // Round to two decimals, as per ISO-4217, using banker's rounding
  override fun serialize(encoder: Encoder, value: BigDecimal) {
    return encoder.encodeString(
      value.setScale(2, RoundingMode.HALF_EVEN).toPlainString(),
    )
  }
}

internal object CurrencySerializer : KSerializer<Currency> {

  override val descriptor = PrimitiveSerialDescriptor(
    serialName = "Currency",
    kind = PrimitiveKind.STRING,
  )

  override fun deserialize(decoder: Decoder): Currency =
    Currency.getInstance(decoder.decodeString())

  override fun serialize(encoder: Encoder, value: Currency) =
    encoder.encodeString(value.currencyCode)
}

internal object VirtualCardSerializer : JsonContentPolymorphicSerializer<VirtualCard>(VirtualCard::class) {

  override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out VirtualCard> {
    if (element.jsonObject.containsKey("cardToken")) {
      return VirtualCard.TokenizedCard.serializer()
    }
    if (element.jsonObject.containsKey("cardNumber")) {
      return VirtualCard.Card.serializer()
    }
    throw IllegalArgumentException("Unknown VirtualCard: JSON does not match any response type")
  }
}

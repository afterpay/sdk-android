package com.afterpay.android.internal

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigDecimal
import java.util.Currency

internal object BigDecimalSerializer : KSerializer<BigDecimal> {

    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "BigDecimal",
        kind = PrimitiveKind.STRING,
    )

    override fun deserialize(decoder: Decoder) = decoder.decodeString().toBigDecimal()

    override fun serialize(encoder: Encoder, value: BigDecimal) =
        encoder.encodeString(value.toPlainString())
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

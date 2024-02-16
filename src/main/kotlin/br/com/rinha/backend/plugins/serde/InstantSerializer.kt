package br.com.rinha.backend.plugins.serde

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.reflect.typeOf

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = serialDescriptor(typeOf<String>())

    override fun deserialize(decoder: Decoder): Instant {
       return Instant.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Instant) {
       encoder.encodeString(
           DateTimeFormatter.ISO_INSTANT.format(value)
       )
    }

}
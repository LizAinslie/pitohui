package dev.lizainslie.pitohui.platforms.discord.config

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object SnowflakeAsStringSerializer : KSerializer<Snowflake> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("dev.kord.common.entity.Snowflake", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Snowflake) {
        encoder.encodeString(value.value.toString())
    }

    override fun deserialize(decoder: Decoder): Snowflake {
        val value = decoder.decodeString()
        return Snowflake(value.toLong())
    }
}
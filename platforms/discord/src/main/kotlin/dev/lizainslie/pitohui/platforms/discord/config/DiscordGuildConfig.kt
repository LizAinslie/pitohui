@file:OptIn(ExperimentalSerializationApi::class)

package dev.lizainslie.pitohui.platforms.discord.config

import dev.kord.common.entity.Snowflake
import dev.lizainslie.pitohui.core.config.ConfigBase
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
data class DiscordGuildConfig(
    @Serializable(SnowflakeAsStringSerializer::class) val id: Snowflake,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val admin: Boolean = false,
) : ConfigBase {
    override fun validate(): Boolean {
        return true
    }
}

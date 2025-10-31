package dev.lizainslie.pitohui.platforms.discord.config

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
data class DiscordPlatformConfig(
    val token: String,
    val guilds: List<@Serializable(with = SnowflakeAsStringSerializer::class) Snowflake>,
)

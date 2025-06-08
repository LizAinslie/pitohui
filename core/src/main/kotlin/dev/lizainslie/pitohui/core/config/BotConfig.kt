package dev.lizainslie.pitohui.core.config

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String,
)

@Serializable
data class BotConfig(
    val token: String,
    val guilds: List<@Serializable(with = SnowflakeAsStringSerializer::class) Snowflake>,
    val db: DatabaseConfig,
) {
    companion object {
        fun load(config: String) = Json.decodeFromString<BotConfig>( config)

        fun load(config: File): BotConfig {
            val configJson = config.readText()
                .trimStart('\uFEFF') // BOM character

            return load(configJson)
        }
    }
}
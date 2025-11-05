package dev.lizainslie.pitohui.core.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class DatabaseConfig(
    val url: String = "",
    val user: String = "",
    val password: String = "",
) : ConfigBase {
    override fun validate(): Boolean {
        // todo:  check that url is actually psql url
        return (url.trim().isNotBlank()
                && user.trim().isNotBlank()
                && password.trim().isNotBlank())
    }
}

@Serializable
data class BotConfig(
    val db: DatabaseConfig = DatabaseConfig(),
) : ConfigBase {
    override fun validate(): Boolean {
        return db.validate()
    }

    companion object {
        fun load(config: String) = Json.decodeFromString<BotConfig>( config)

        fun load(config: File): BotConfig {
            val configJson = config.readText()
                .trimStart('\uFEFF') // BOM character

            return load(configJson)
        }

        fun load() = load(Configs.getBotConfigFile())
    }
}

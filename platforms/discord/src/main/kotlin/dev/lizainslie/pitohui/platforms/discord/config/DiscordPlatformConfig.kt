package dev.lizainslie.pitohui.platforms.discord.config

import dev.lizainslie.pitohui.core.config.ConfigBase
import kotlinx.serialization.Serializable

@Serializable
data class DiscordPlatformConfig(
    val token: String = "",
    val guilds: List<DiscordGuildConfig> = emptyList(),
) : ConfigBase {
    override fun validate(): Boolean {
        return (token.trim().isNotBlank() && guilds.isNotEmpty() && guilds.all { it.validate() })
    }
}

package dev.lizainslie.pitohui.platforms.discord.config

import dev.lizainslie.pitohui.core.config.ConfigBase
import dev.lizainslie.pitohui.core.config.ConfigFile
import dev.lizainslie.pitohui.core.config.ConfigType
import kotlinx.serialization.Serializable

@ConfigFile("discord.json", "platforms:discord", ConfigType.PLATFORM)
@Serializable
data class DiscordPlatformConfig(
    val token: String = "",
    val guilds: List<DiscordGuildConfig> = emptyList(),
) : ConfigBase {
    override fun validate(): Boolean {
        return (token.trim().isNotBlank() && guilds.isNotEmpty() && guilds.all { it.validate() })
    }
}

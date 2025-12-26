package dev.lizainslie.pitohui.platforms.discord.config

import dev.lizainslie.pitohui.core.config.ConfigBase
import kotlinx.serialization.Serializable

@Serializable
data class DiscordCommandParsingConfig(
    val defaultPrefix: String = ";",
    val allowMentionPrefix: Boolean = true,
    val caseInsensitive: Boolean = true,
    val bannedPrefixes: List<String> = emptyList()
) : ConfigBase {
    override fun validate(): Boolean {
        return defaultPrefix.isNotBlank() && bannedPrefixes.all { it.isNotBlank() }
    }
}

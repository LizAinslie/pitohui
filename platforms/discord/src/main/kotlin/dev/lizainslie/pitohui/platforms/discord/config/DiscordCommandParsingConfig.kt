package dev.lizainslie.pitohui.platforms.discord.config

import dev.lizainslie.pitohui.core.config.ConfigBase
import kotlinx.serialization.Serializable

@Serializable
data class DiscordCommandParsingConfig(
    val allowMentionPrefix: Boolean = true,
    val allowCustomGuildPrefixes: Boolean = true,
    val allowCustomUserPrefixes: Boolean = true,
) : ConfigBase {
    override fun validate() = true
}

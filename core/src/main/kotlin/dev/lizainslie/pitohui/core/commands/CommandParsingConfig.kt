package dev.lizainslie.pitohui.core.commands

import dev.lizainslie.pitohui.core.config.ConfigBase
import dev.lizainslie.pitohui.core.config.ConfigFile
import dev.lizainslie.pitohui.core.config.ConfigType
import kotlinx.serialization.Serializable

@Serializable
@ConfigFile("command_parsing.json", "core:command_parsing", ConfigType.ROOT)
data class CommandParsingConfig(
    val defaultPrefix: String = ";",
    val allowedArgumentDelimiters: List<String> = listOf(
        ":",
        "=",
    ),
    val requireArgumentNames: Boolean = false,
    val caseInsensitive: Boolean = true,
    val bannedPrefixes: List<String> = emptyList()
) : ConfigBase {
    override fun validate(): Boolean {
        return defaultPrefix.isNotBlank()
                && allowedArgumentDelimiters.all { it.isNotBlank() }
                && bannedPrefixes.all { it.isNotBlank() }
    }
}

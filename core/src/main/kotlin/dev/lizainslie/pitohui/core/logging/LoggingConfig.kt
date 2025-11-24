package dev.lizainslie.pitohui.core.logging

import dev.lizainslie.pitohui.core.config.ConfigBase
import dev.lizainslie.pitohui.core.config.ConfigFile
import dev.lizainslie.pitohui.core.config.ConfigType
import kotlinx.serialization.Serializable

@ConfigFile("logging.json", "core:logging", ConfigType.ROOT)
@Serializable
data class LoggingConfig(
    val level: String = "INFO",
) : ConfigBase {
    override fun validate(): Boolean {
        return level.uppercase() in validLevels
    }

    override fun onLoad() {
        Logging.syncLevel(level)
    }

    companion object {
        val validLevels = setOf("TRACE", "DEBUG", "INFO", "WARN", "ERROR", "OFF")
    }
}

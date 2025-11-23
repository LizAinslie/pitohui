package dev.lizainslie.pitohui.core.data

import dev.lizainslie.pitohui.core.config.ConfigBase
import dev.lizainslie.pitohui.core.config.ConfigFile
import dev.lizainslie.pitohui.core.config.ConfigType
import kotlinx.serialization.Serializable

@ConfigFile("database.json", "core:database", ConfigType.ROOT)
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
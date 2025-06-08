package dev.lizainslie.pitohui.core.platforms

import dev.kord.common.entity.Snowflake

data class PlatformId(
    val platform: Platforms,
    val id: String
) {
    companion object {
        fun fromSnowflake(snowflake: Snowflake) = PlatformId(
            platform = Platforms.DISCORD,
            id = snowflake.value.toString()
        )
    }
}

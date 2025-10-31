package dev.lizainslie.pitohui.platforms.discord.extensions

import dev.kord.common.entity.Snowflake
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.platforms.discord.Discord

fun Snowflake.platform() = PlatformId(
    platform = Discord.key,
    id = value.toString()
)

infix fun Snowflake.eq(other: PlatformId) = this.value == other.id.toULong()
package dev.lizainslie.pitohui.platforms.discord.extensions

import dev.kord.common.entity.Snowflake
import dev.lizainslie.pitohui.core.platforms.PlatformId

val PlatformId.snowflake get() = Snowflake(this.id)
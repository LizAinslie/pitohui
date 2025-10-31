package dev.lizainslie.pitohui.platforms.discord.extensions

import dev.lizainslie.pitohui.core.platforms.PlatformAdapterFactory
import dev.lizainslie.pitohui.core.platforms.Platforms
import dev.lizainslie.pitohui.platforms.discord.Discord
import dev.lizainslie.pitohui.platforms.discord.config.DiscordPlatformConfig


val Platforms.DISCORD: PlatformAdapterFactory<DiscordPlatformConfig, Discord>
    get() = Discord
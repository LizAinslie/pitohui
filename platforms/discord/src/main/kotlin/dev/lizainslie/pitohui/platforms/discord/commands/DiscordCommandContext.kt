package dev.lizainslie.pitohui.platforms.discord.commands

import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.commands.CommandContext
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.platforms.PlatformAdapterFactory
import dev.lizainslie.pitohui.core.platforms.PlatformId

abstract class DiscordCommandContext(
    bot: Bot,
    module: AbstractModule,
    platform: PlatformAdapterFactory<*, *>,
) : CommandContext(bot, module, platform) {
    abstract val channelId: PlatformId
    abstract val userId: PlatformId
    abstract val guildId: PlatformId?
}

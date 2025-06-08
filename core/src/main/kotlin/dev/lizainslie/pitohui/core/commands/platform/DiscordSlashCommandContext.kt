package dev.lizainslie.pitohui.core.commands.platform

import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.commands.CommandContext
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.platforms.Platforms

class DiscordSlashCommandContext(
    bot: Bot,
    module: AbstractModule,
    platform: Platforms,

    val interaction: ChatInputCommandInteraction
) : CommandContext(bot, module, platform) {
    override suspend fun respond(text: String) {
        interaction.respondPublic {
            content = text
        }
    }
}
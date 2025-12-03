package dev.lizainslie.pitohui.modules.starboard

import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.event.message.ReactionRemoveEvent
import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.modules.ModuleVisibility
import dev.lizainslie.pitohui.core.platforms.SupportPlatforms
import dev.lizainslie.pitohui.platforms.discord.Discord

@SupportPlatforms(Discord::class)
object StarboardModule : AbstractModule(
    name = "starboard",
    description = "Highlight popular messages in a dedicated channel.",
    visibility = ModuleVisibility.MODERATOR,
    commands = setOf(),
    tables = setOf(),
) {
    override fun onInit(bot: Bot) {
        super.onInit(bot)

        Discord.addEventListener<ReactionAddEvent> {
            // todo: handle
        }

        Discord.addEventListener<ReactionRemoveEvent> {
            // todo
        }
    }
}
package dev.lizainslie.pitohui.platforms.discord.commands

import dev.lizainslie.pitohui.core.commands.CommandContext
import dev.lizainslie.pitohui.core.platforms.UnsupportedPlatformException
import dev.lizainslie.pitohui.platforms.discord.Discord

suspend inline fun <T: Any, reified TContext : DiscordCommandContext> CommandContext.evalDiscord(block: suspend TContext.() -> T): T? {
    return if (this is TContext) this.block()
    else null
}

suspend inline fun <reified TContext : DiscordCommandContext> CommandContext.enforceDiscord(
    block: suspend TContext.() -> Unit
) {
    if (this is TContext) this.block()
    else
        throw UnsupportedPlatformException(platform, Discord)
}
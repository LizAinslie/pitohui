package dev.lizainslie.pitohui.core.commands

import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.Platforms

abstract class CommandContext(
    val bot: Bot,
    val module: AbstractModule,
    val platform: Platforms,
) {
    abstract suspend fun respond(text: String)
}

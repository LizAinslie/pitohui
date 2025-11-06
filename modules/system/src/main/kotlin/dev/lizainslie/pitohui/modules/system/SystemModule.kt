package dev.lizainslie.pitohui.modules.system

import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.SupportPlatforms
import dev.lizainslie.pitohui.modules.system.commands.AboutCommand
import dev.lizainslie.pitohui.modules.system.commands.ModuleCommand
import dev.lizainslie.pitohui.platforms.discord.Discord

@SupportPlatforms(Discord::class)
object SystemModule : AbstractModule(
    "system",
    description = "Core functionality required for Pitohui to function",
    optional = false,
    commands = setOf(
        AboutCommand,
        ModuleCommand,
    )
) {
    override fun isEnabledForCommunity(communityId: PlatformId) = true
}

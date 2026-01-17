package dev.lizainslie.pitohui.modules.system

import dev.lizainslie.moeka.core.modules.AbstractModule
import dev.lizainslie.moeka.core.platforms.PlatformId
import dev.lizainslie.moeka.core.platforms.SupportPlatforms
import dev.lizainslie.moeka.platforms.discord.Discord
import dev.lizainslie.pitohui.modules.system.commands.AboutCommand
import dev.lizainslie.pitohui.modules.system.commands.ModuleCommand

@SupportPlatforms(Discord::class)
object SystemModule : AbstractModule(
    "system",
    description = "Core functionality required for Pitohui to function",
    optional = false,
    commands =
        setOf(
            AboutCommand,
            ModuleCommand,
        ),
) {
    override fun isEnabledForCommunity(communityId: PlatformId) = true
}

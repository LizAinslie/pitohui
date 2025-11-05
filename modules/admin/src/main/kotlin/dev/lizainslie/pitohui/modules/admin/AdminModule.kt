package dev.lizainslie.pitohui.modules.admin

import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.modules.ModuleVisibility
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.SupportPlatforms
import dev.lizainslie.pitohui.modules.admin.commands.DumpContextCommand
import dev.lizainslie.pitohui.modules.admin.commands.MigrateCommand
import dev.lizainslie.pitohui.modules.admin.commands.ToggleStealthCommand
import dev.lizainslie.pitohui.modules.system.SystemModule
import dev.lizainslie.pitohui.platforms.discord.Discord

@SupportPlatforms(Discord::class)
object AdminModule : AbstractModule() {
    override val name = "admin"
    override val optional = false
    override val visibility = ModuleVisibility.DEVELOPER
    override val description = "Administrative commands for Pitohui"
    override val commands = setOf(
        MigrateCommand,
        ToggleStealthCommand,
        DumpContextCommand
    )
    override val dependencies = setOf(SystemModule.name)

    override fun isEnabledForCommunity(communityId: PlatformId) = true
}

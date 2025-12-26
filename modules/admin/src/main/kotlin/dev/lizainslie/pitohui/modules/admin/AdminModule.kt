package dev.lizainslie.pitohui.modules.admin

import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.modules.ModuleVisibility
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.SupportPlatforms
import dev.lizainslie.pitohui.modules.admin.commands.AdminModuleCommand
import dev.lizainslie.pitohui.modules.admin.commands.DumpContextCommand
import dev.lizainslie.pitohui.modules.admin.commands.MigrateCommand
import dev.lizainslie.pitohui.modules.admin.commands.ShutdownCommand
import dev.lizainslie.pitohui.modules.admin.commands.ToggleStealthCommand
import dev.lizainslie.pitohui.modules.system.SystemModule
import dev.lizainslie.pitohui.platforms.discord.Discord

@SupportPlatforms(Discord::class)
object AdminModule : AbstractModule(
    "admin",
    description = "Administrative commands for Pitohui",
    visibility = ModuleVisibility.DEVELOPER,
    optional = false,
    commands = setOf(
        MigrateCommand,
        ToggleStealthCommand,
        DumpContextCommand,
        AdminModuleCommand,
        ShutdownCommand,
    ),
    dependencies = setOf(SystemModule.name),
) {
    override fun isEnabledForCommunity(communityId: PlatformId) = true

    suspend fun shutdownBot() {
        bot.stop()
    }
}

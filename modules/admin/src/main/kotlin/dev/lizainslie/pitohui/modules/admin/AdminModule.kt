package dev.lizainslie.pitohui.modules.admin

import dev.lizainslie.moeka.core.modules.AbstractModule
import dev.lizainslie.moeka.core.modules.ModuleVisibility
import dev.lizainslie.moeka.core.platforms.PlatformId
import dev.lizainslie.moeka.core.platforms.SupportPlatforms
import dev.lizainslie.moeka.platforms.discord.Discord
import dev.lizainslie.pitohui.modules.admin.commands.AdminModuleCommand
import dev.lizainslie.pitohui.modules.admin.commands.ConfigCommand
import dev.lizainslie.pitohui.modules.admin.commands.DumpContextCommand
import dev.lizainslie.pitohui.modules.admin.commands.MigrateCommand
import dev.lizainslie.pitohui.modules.admin.commands.ShutdownCommand
import dev.lizainslie.pitohui.modules.admin.commands.ToggleStealthCommand
import dev.lizainslie.pitohui.modules.system.SystemModule

@SupportPlatforms(Discord::class)
object AdminModule : AbstractModule(
    "admin",
    description = "Administrative commands for Pitohui",
    visibility = ModuleVisibility.DEVELOPER,
    optional = false,
    commands =
        setOf(
            MigrateCommand,
            ToggleStealthCommand,
            DumpContextCommand,
            AdminModuleCommand,
            ShutdownCommand,
            ConfigCommand,
        ),
    dependencies = setOf(SystemModule.name),
) {
    override fun isEnabledForCommunity(communityId: PlatformId) = true

    suspend fun shutdownBot() {
        bot.stop()
    }
}

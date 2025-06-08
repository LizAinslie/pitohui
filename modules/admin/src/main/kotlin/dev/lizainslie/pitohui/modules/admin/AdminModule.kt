package dev.lizainslie.pitohui.modules.admin

import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.modules.ModuleVisibility
import dev.lizainslie.pitohui.core.platforms.Platforms
import dev.lizainslie.pitohui.modules.admin.commands.MigrateCommand
import dev.lizainslie.pitohui.modules.system.SystemModule

object AdminModule : AbstractModule() {
    override val name = "admin"
    override val optional = false
    override val visibility = ModuleVisibility.DEVELOPER
    override val description = "Administrative commands for Pitohui"
    override val commands = setOf(
        MigrateCommand,
    )
    override val dependencies = setOf(SystemModule.name)

    override fun isEnabledForCommunity(platform: Platforms, platformId: String) = true
}
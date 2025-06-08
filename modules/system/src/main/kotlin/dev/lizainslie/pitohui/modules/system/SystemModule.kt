package dev.lizainslie.pitohui.modules.system

import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.platforms.Platforms
import dev.lizainslie.pitohui.modules.system.commands.AboutCommand
import dev.lizainslie.pitohui.modules.system.commands.ModuleCommand
import dev.lizainslie.pitohui.modules.system.commands.SettingsCommand

object SystemModule : AbstractModule() {
    override val optional = false
    override val name = "system"
    override val description = "Core functionality required for Pitohui to function"
    override val commands = setOf(
        SettingsCommand,
        AboutCommand,
        ModuleCommand,
    )

    override fun isEnabledForCommunity(platform: Platforms, platformId: String) = true
}
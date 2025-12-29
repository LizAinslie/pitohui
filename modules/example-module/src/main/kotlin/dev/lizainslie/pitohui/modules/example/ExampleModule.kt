package dev.lizainslie.pitohui.modules.example

import dev.lizainslie.pitohui.core.config.Configs
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.modules.ModuleVisibility
import dev.lizainslie.pitohui.core.platforms.SupportPlatforms
import dev.lizainslie.pitohui.modules.example.commands.ExampleCommand
import dev.lizainslie.pitohui.modules.example.config.ExampleConfig
import dev.lizainslie.pitohui.modules.system.SystemModule
import dev.lizainslie.pitohui.platforms.discord.Discord

@Suppress("unused")
@SupportPlatforms(Discord::class)
object ExampleModule : AbstractModule(
    name = "example",
    optional = false,
    visibility = ModuleVisibility.DEVELOPER,
    description = "An example module to show off pitohui's engine features",
    commands = setOf(
        ExampleCommand
    ),
    tables = setOf(),
    dependencies = setOf(SystemModule.name)
) {
    val config by Configs.config<ExampleConfig>()
}
package dev.lizainslie.pitohui.core.modules

import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.commands.RootCommand
import dev.lizainslie.pitohui.core.data.ModuleSwitch
import dev.lizainslie.pitohui.core.platforms.Platforms
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

abstract class AbstractModule() {
    abstract val name: String
    open val optional = true
    open val visibility = ModuleVisibility.PUBLIC
    open val description: String = "No description provided"
    open val commands: Set<RootCommand> = emptySet()
    open val tables: Set<Table> = emptySet()
    open val dependencies: Set<String> = emptySet()

    open fun onLoad() {}
    open fun onInit(bot: Bot) {}

    open fun isEnabledForCommunity(platform: Platforms, platformId: String) = transaction {
        ModuleSwitch.isModuleEnabled(platformId, platform, name)
    }
}
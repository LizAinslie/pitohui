package dev.lizainslie.pitohui.core.modules

import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.commands.RootCommand
import dev.lizainslie.pitohui.core.data.ModuleSwitch
import dev.lizainslie.pitohui.core.platforms.PlatformAdapterFactory
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.PlatformKey
import dev.lizainslie.pitohui.core.platforms.SupportPlatforms
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
    protected lateinit var bot: Bot

    open fun onLoad() {}
    open fun onInit(bot: Bot) {
        this.bot = bot
    }

    open fun isEnabledForCommunity(communityId: PlatformId) = transaction {
        supportsPlatform(communityId.platform) && ModuleSwitch.isModuleEnabled(communityId, name)
    }

    fun supportsPlatform(platform: PlatformAdapterFactory<*, *>) =
        this::class.annotations.filterIsInstance<SupportPlatforms>().any {
            it.platforms.contains(platform.platformClass)
        }

    fun supportsPlatform(platform: PlatformKey) =
        bot.platformAdapters.firstOrNull { it.key == platform }
            ?.let { supportsPlatform(it) } ?: false
}

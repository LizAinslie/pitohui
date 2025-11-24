package dev.lizainslie.pitohui.core.modules

import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.commands.RootCommand
import dev.lizainslie.pitohui.core.config.ConfigBase
import dev.lizainslie.pitohui.core.config.Configs
import dev.lizainslie.pitohui.core.data.ModuleSwitch
import dev.lizainslie.pitohui.core.platforms.PlatformAdapter
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.PlatformKey
import dev.lizainslie.pitohui.core.platforms.SupportPlatforms
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class AbstractModule(
    val name: String,
    val optional: Boolean = true,
    val visibility: ModuleVisibility = ModuleVisibility.PUBLIC,
    val description: String = "No description provided",
    val commands: Set<RootCommand> = emptySet(),
    val tables: Set<Table> = emptySet(),
    val dependencies: Set<String> = emptySet(),
) {
    protected lateinit var bot: Bot
    protected val log: Logger = LoggerFactory.getLogger(this::class.java)

    open fun onLoad() {}
    open fun onUnload() {}
    open fun onInit(bot: Bot) {
        this.bot = bot
    }

    inline fun <reified TConfig : ConfigBase> config() =
        Configs.moduleConfig<TConfig>(this.name)

    open fun isEnabledForCommunity(communityId: PlatformId) = transaction {
        supportsPlatform(communityId.platform) && ModuleSwitch.isModuleEnabled(communityId, name)
    }

    fun supportsPlatform(platform: PlatformAdapter) =
        this::class.annotations.filterIsInstance<SupportPlatforms>().any {
            it.platforms.contains(platform::class)
        }

    fun supportsPlatform(platform: PlatformKey) =
        bot.platformAdapters.firstOrNull { it.key == platform }
            ?.let { supportsPlatform(it) } ?: false
}

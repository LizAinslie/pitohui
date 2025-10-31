package dev.lizainslie.pitohui.core.platforms

import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.commands.RootCommand
import dev.lizainslie.pitohui.core.commands.PlatformArgumentParseFn
import dev.lizainslie.pitohui.core.modules.AbstractModule

abstract class PlatformAdapter<TConfig: Any>(
    val config: TConfig,
    val key: PlatformKey,
    val displayName: String
) {
    open val channelArgumentParser: PlatformArgumentParseFn<PlatformId>? = null
    open val roleArgumentParser: PlatformArgumentParseFn<PlatformId>? = null

    lateinit var bot: Bot
    open suspend fun initialize(bot: Bot) {
        this.bot = bot
    }

    abstract suspend fun start(bot: Bot)
    abstract suspend fun registerCommand(command: RootCommand, module: AbstractModule)
}
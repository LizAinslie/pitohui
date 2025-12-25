package dev.lizainslie.pitohui.core.platforms

import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.commands.RootCommand
import dev.lizainslie.pitohui.core.commands.PlatformArgumentParseFn
import dev.lizainslie.pitohui.core.logging.suspendLogPlatform
import dev.lizainslie.pitohui.core.modules.AbstractModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class PlatformAdapter(
    val key: PlatformKey,
    val displayName: String
) {
    protected val log: Logger get() = LoggerFactory.getLogger(this::class.java)

    open val channelArgumentParser: PlatformArgumentParseFn<PlatformId>? = null
    open val roleArgumentParser: PlatformArgumentParseFn<PlatformId>? = null
    open val userArgumentParser: PlatformArgumentParseFn<PlatformId>? = null

    lateinit var bot: Bot

    open suspend fun initialize(bot: Bot) {
        this.bot = bot
    }

    suspend fun <T> suspendLogPlatform(block: suspend () -> T) =
        suspendLogPlatform(key, block)

    abstract suspend fun start(bot: Bot)
    abstract suspend fun stop()
    abstract suspend fun registerCommand(command: RootCommand, module: AbstractModule)
    abstract suspend fun unregisterCommand(command: RootCommand, module: AbstractModule)
}
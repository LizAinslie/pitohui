package dev.lizainslie.pitohui.core.commands

import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.platforms.PlatformAdapterFactory

abstract class CommandContext(
    val bot: Bot,
    val module: AbstractModule,
    val platform: PlatformAdapterFactory<*, *>,
) {
    abstract suspend fun respond(text: String)
    abstract suspend fun respondError(text: String)

    abstract fun <T> resolveRawArgumentValue(arg: ArgumentDescriptor<T>): T?

    val args = ArgumentMap(this)

    class ArgumentMap(private val context: CommandContext) {
        suspend operator fun <T> get(key: ArgumentDescriptor<T>): T? = key.resolve(context)
    }
}

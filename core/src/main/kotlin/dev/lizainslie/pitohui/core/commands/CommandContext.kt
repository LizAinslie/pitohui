package dev.lizainslie.pitohui.core.commands

import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.data.DeveloperOptions
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.platforms.PlatformAdapterFactory
import dev.lizainslie.pitohui.core.platforms.PlatformId
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

abstract class CommandContext(
    val bot: Bot,
    val module: AbstractModule,
    val platform: PlatformAdapterFactory<*, *>,
) {
    abstract suspend fun respond(text: String)
    abstract suspend fun respondPrivate(text: String)
    abstract suspend fun respondError(text: String)

    abstract fun <T> resolveRawArgumentValue(arg: ArgumentDescriptor<T>): T?

    abstract val callerId: PlatformId

    suspend fun callerIsDeveloper() = newSuspendedTransaction {
        DeveloperOptions.isUserDeveloper(callerId)
    }

    suspend fun callerIsStealth() = newSuspendedTransaction {
        DeveloperOptions.isUserStealth(callerId)
    }

    val args = ArgumentMap(this)

    class ArgumentMap(private val context: CommandContext) {
        suspend operator fun <T> get(key: ArgumentDescriptor<T>): T? = key.resolve(context)
    }
}

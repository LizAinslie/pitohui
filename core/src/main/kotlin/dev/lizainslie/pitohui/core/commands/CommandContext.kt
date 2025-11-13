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
    open suspend fun respondException(e: Exception) {
        respondStealth("""An exception has occurred while running this command: ${e.message}
            |```kt
            |${e.stackTraceToString()}
            |```
        """.trimMargin())
    }

    suspend fun respondStealth(text: String) {
        if (callerIsStealth()) respondPrivate(text)
        else respond(text)
    }

    abstract fun <T> resolveRawArgumentValue(arg: ArgumentDescriptor<T>): T?

    abstract val callerId: PlatformId
    abstract val communityId: PlatformId?

    // we use `get()` here because with a simple assignment, the expression
    // will always evaluate to `false` even if the value of `communityId` is not
    // `null`
    val isInCommunity get() = communityId != null

    suspend fun callerIsDeveloper() = newSuspendedTransaction {
        DeveloperOptions.isUserDeveloper(callerId)
    }

    suspend fun callerIsStealth() = newSuspendedTransaction {
        DeveloperOptions.getDeveloperOptions(callerId)?.stealth ?: false
    }

    val args = ArgumentMap(this)

    abstract suspend fun dump()

    class ArgumentMap(private val context: CommandContext) {
        suspend operator fun <T> get(key: ArgumentDescriptor<T>): T? = key.resolve(context)
    }
}

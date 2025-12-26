package dev.lizainslie.pitohui.core.commands

import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.commands.argument.ArgumentDescriptor
import dev.lizainslie.pitohui.core.data.entities.DeveloperOptions
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.platforms.AnyPlatformAdapter
import dev.lizainslie.pitohui.core.platforms.PlatformAdapter
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.entities.PlatformResponse
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

abstract class CommandContext(
    val bot: Bot,
    val module: AbstractModule,
    val platform: AnyPlatformAdapter,
) {
    open var response: PlatformResponse? = null
    abstract suspend fun respond(text: String): PlatformResponse
    abstract suspend fun respondPrivate(text: String): PlatformResponse
    abstract suspend fun respondError(text: String): PlatformResponse

    open suspend fun respondException(e: Exception) =
        response?.createStealthFollowup(
            """An exception has occurred while running this command: ${e.message}
                    |```kt
                    |${e.stackTraceToString()}
                    |```
                """.trimMargin()
        ) ?: respondStealth(
            """An exception has occurred while running this command: ${e.message}
                    |```kt
                    |${e.stackTraceToString()}
                    |```
                """.trimMargin()
        )

    suspend fun respondStealth(text: String) =
        if (callerIsStealth()) respondPrivate(text)
        else respond(text)

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

package dev.lizainslie.pitohui.core.commands

import com.sun.jdi.connect.Connector
import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.commands.argument.ArgumentDescriptor
import dev.lizainslie.pitohui.core.commands.argument.ResolvedArguments
import dev.lizainslie.pitohui.core.data.entities.DeveloperOptions
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.platforms.AnyPlatformAdapter
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.entities.PlatformResponse
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KProperty

abstract class CommandContext(
    val bot: Bot,
    val module: AbstractModule,
    val platform: AnyPlatformAdapter,
    val args: ResolvedArguments,
) {

    protected val log: Logger = LoggerFactory.getLogger(javaClass)
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

    abstract suspend fun dump()

    inline operator fun <reified T : Any> ArgumentDescriptor<T>.getValue(thisRef: Nothing?, property: KProperty<*>): T? = this@CommandContext.args[this].value

    inline fun <reified T : Any> ArgumentDescriptor<T>.require(
        errorMessage: String = "Argument $name is required but not provided"
    ) = RequiredArgumentDelegate(
        argument = this,
        value = this@CommandContext.args[this].value,
        errorMessage = errorMessage,
    )

    class RequiredArgumentDelegate<T : Any>(
        private val argument: ArgumentDescriptor<T>,
        private val value: T?,
        private val errorMessage: String = "Argument ${argument.name} is required but not provided",
    ) {
        operator fun getValue(thisRef: Nothing?, property: KProperty<*>): T {
            if (value == null) throw Exception(errorMessage)
            return value
        }
    }
}

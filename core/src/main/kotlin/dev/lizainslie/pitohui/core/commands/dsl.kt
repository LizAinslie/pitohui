@file:OptIn(ExperimentalContracts::class)
package dev.lizainslie.pitohui.core.commands

import dev.lizainslie.pitohui.core.commands.argument.ArgumentDescriptor
import dev.lizainslie.pitohui.core.commands.argument.ArgumentType
import dev.lizainslie.pitohui.core.platforms.PlatformAdapter
import dev.lizainslie.pitohui.core.platforms.PlatformKey
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

open class BaseCommandDsl(
    val name: String,
    val description: String,
) {
    lateinit var handler: CommandHandler

    protected val arguments = mutableListOf<ArgumentDescriptor<*>>()

    fun handle(block: CommandHandler) {
        handler = block
    }

    fun <T> argument(
        name: String,
        description: String,
        type: ArgumentType<T>,
        defaultValue: T? = null,
        required: Boolean = false,
        autoComplete: () -> List<String> = { emptyList() },
    ): ArgumentDescriptor<T> {
        val descriptor = ArgumentDescriptor(name, description, type, defaultValue, required)
        arguments.add(descriptor)
        return descriptor
    }
}

class RootCommandDsl(
    name: String,
    description: String,
) : BaseCommandDsl(name, description) {
    private val platforms = mutableMapOf<PlatformKey, PlatformCommandConfig>()
    private val subCommands = mutableListOf<SubCommandDsl>()
    var communityOnly: Boolean = false

    fun subCommand(
        name: String,
        description: String,
        block: BaseCommandDsl.() -> Unit,
    ) {
        val command = SubCommandDsl(name, description).apply(block)
        subCommands.add(command)
    }

    fun <TCommandConfig : PlatformCommandConfig> platform(platform: PlatformAdapter<TCommandConfig>, configure: (TCommandConfig.() -> Unit) = {}) {
        contract { callsInPlace(configure, InvocationKind.EXACTLY_ONCE) }
        val commandConfig = platform.createEmptyCommandConfig().apply(configure)
        this.platforms[platform.key] = commandConfig
    }

    fun buildRoot(): RootCommand {
        // Capture properties to pass into the anonymous class
        val subCommands = subCommands
        val platforms = platforms
        val arguments = arguments
        val communityOnly = communityOnly

        return object : RootCommand(name, description) {
            override val subCommands: List<SubCommand> = subCommands.map { it.buildSubCommand(this) }
            override val platforms: Map<PlatformKey, PlatformCommandConfig> = platforms
            override val arguments: List<ArgumentDescriptor<*>> = arguments
            override val communityOnly: Boolean = communityOnly

            override suspend fun handle(context: CommandContext) {
                handler(context)
            }
        }
    }
}

class SubCommandDsl(
    name: String,
    description: String,
) : BaseCommandDsl(name, description) {
    fun buildSubCommand(parent: BaseCommand): SubCommand {
        val arguments = arguments

        return object : SubCommand(name, description, parent) {
            override val arguments: List<ArgumentDescriptor<*>> = arguments

            override suspend fun handle(context: CommandContext) {
                handler(context)
            }
        }
    }
}

fun defineCommand(
    name: String,
    description: String,
    define: RootCommandDsl.() -> Unit,
): RootCommand {
    contract { callsInPlace(define, InvocationKind.EXACTLY_ONCE) }
    return RootCommandDsl(name, description).apply(define).buildRoot()
}

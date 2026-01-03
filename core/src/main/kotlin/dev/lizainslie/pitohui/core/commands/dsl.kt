@file:OptIn(ExperimentalContracts::class)
package dev.lizainslie.pitohui.core.commands

import dev.lizainslie.pitohui.core.annotations.PitohuiDsl
import dev.lizainslie.pitohui.core.commands.argument.ArgumentDescriptor
import dev.lizainslie.pitohui.core.commands.argument.ArgumentType
import dev.lizainslie.pitohui.core.platforms.PlatformAdapter
import dev.lizainslie.pitohui.core.platforms.PlatformKey
import dev.lizainslie.pitohui.core.validation.ValidationDsl
import dev.lizainslie.pitohui.core.validation.Validator
import dev.lizainslie.pitohui.core.validation.buildValidator
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass

@PitohuiDsl
class ArgumentDsl<T : Any>(
    val tClass: KClass<T>,
    val name: String,
    val description: String,
    val type: ArgumentType<T>,
) {
    var defaultValue: T? = null
    var required: Boolean = false
    var validator: Validator<T>? = null
    var autoCompleter: (() -> List<String>)? = null

    fun validate(builder: ValidationDsl<T>.() -> Unit) {
        contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }
        validator = buildValidator(builder)
    }

    fun complete(completer: () -> List<String>) {
        autoCompleter = completer
    }

    fun buildDescriptor() = ArgumentDescriptor(
        tClass = tClass,
        name = name,
        description = description,
        argumentType = type,
        defaultValue = defaultValue,
        required = required,
        validator = validator,
        autoComplete = autoCompleter,
    )
}

@PitohuiDsl
open class BaseCommandDsl(
    val name: String,
    val description: String,
) {
    lateinit var handler: CommandHandler
    val arguments = mutableListOf<ArgumentDescriptor<*>>()

    fun handle(block: CommandHandler) {
        handler = block
    }

    inline fun <reified T : Any> argument(
        name: String,
        description: String,
        type: ArgumentType<T>,
        builder: ArgumentDsl<T>.() -> Unit = {}
    ): ArgumentDescriptor<T> {
        contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }
        val dsl = ArgumentDsl( T::class, name, description, type).apply(builder)
        val descriptor = dsl.buildDescriptor()
        arguments.add(descriptor)
        return descriptor
    }
}

@PitohuiDsl
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

@PitohuiDsl
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

package dev.lizainslie.pitohui.core.commands

import dev.lizainslie.pitohui.core.platforms.Platforms

open class BaseCommandDsl(
    val name: String,
    val description: String,
) {
    lateinit var handler: CommandHandler

    protected val platforms = mutableSetOf<Platforms>()
    protected val arguments = mutableListOf<ArgumentDescriptor<*>>()

    fun platforms(vararg platforms: Platforms) {
        this.platforms.addAll(platforms)
    }

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
    private val subCommands = mutableListOf<SubCommandDsl>()

    fun subCommand(
        name: String,
        description: String,
        block: BaseCommandDsl.() -> Unit,
    ) {
        val command = SubCommandDsl(name, description).apply(block)
        subCommands.add(command)
    }

    fun buildRoot(): RootCommand {
        val subCommands = subCommands
        val platforms = if (platforms.isEmpty()) Platforms.entries.toSet() else platforms
        val arguments = arguments

        return object : RootCommand(name, description) {
            override val subCommands: List<SubCommand> = subCommands.map { it.buildSubCommand(this) }
            override val platforms: Set<Platforms> = platforms
            override val arguments: List<ArgumentDescriptor<*>> = arguments

            override suspend fun handle(context: CommandContext) {
                handler.handle(context)
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
            override val platforms: Set<Platforms> = parent.platforms
            override val arguments: List<ArgumentDescriptor<*>> = arguments

            override suspend fun handle(context: CommandContext) {
                handler.handle(context)
            }
        }
    }
}

fun defineCommand(
    name: String,
    description: String,
    block: RootCommandDsl.() -> Unit,
): RootCommand {
    return RootCommandDsl(name, description).apply(block).buildRoot()
}
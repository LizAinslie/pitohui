package dev.lizainslie.pitohui.core.commands

import dev.lizainslie.pitohui.core.commands.argument.ArgumentDescriptor
import dev.lizainslie.pitohui.core.platforms.AnyPlatformAdapter
import dev.lizainslie.pitohui.core.platforms.PlatformKey

abstract class BaseCommand(
    val name: String,
    val description: String,
) {
    abstract val rootCommand: RootCommand

    open val arguments: List<ArgumentDescriptor<*>> = emptyList()

    abstract suspend fun handle(context: CommandContext)

    fun supportsPlatform(key: PlatformKey) =
        rootCommand.platforms.containsKey(key)

    fun supportsPlatform(platform: AnyPlatformAdapter) =
        supportsPlatform(platform.key)
}

abstract class RootCommand(
    name: String,
    description: String,
) : BaseCommand(name, description) {
    abstract val platforms: Map<PlatformKey, PlatformCommandConfig>
    override val rootCommand = this
    open val communityOnly: Boolean = false

    open val subCommands: List<SubCommand> = emptyList()
}

abstract class SubCommand(
    name: String,
    description: String,
    parent: BaseCommand,
) : BaseCommand(name, description) {
    override val rootCommand = parent.rootCommand
}

package dev.lizainslie.pitohui.core.commands

import dev.lizainslie.pitohui.core.platforms.PlatformAdapter
import dev.lizainslie.pitohui.core.platforms.PlatformKey

abstract class BaseCommand(
    val name: String,
    val description: String,
) {
    abstract val rootName: String

    open val platforms: Set<PlatformKey> = emptySet()
    open val arguments: List<ArgumentDescriptor<*>> = emptyList()

    abstract suspend fun handle(context: CommandContext)

    fun supportsPlatform(key: PlatformKey) =
        if (platforms.isEmpty()) true
        else platforms.contains(key)

    fun supportsPlatform(platform: PlatformAdapter) =
        supportsPlatform(platform.key)
}

abstract class RootCommand(
    name: String,
    description: String,
) : BaseCommand(name, description) {
    override val rootName = name

    open val subCommands: List<SubCommand> = emptyList()
}

abstract class SubCommand(
    name: String,
    description: String,
    val parent: BaseCommand,
) : BaseCommand(name, description) {
    override val rootName = parent.rootName
}

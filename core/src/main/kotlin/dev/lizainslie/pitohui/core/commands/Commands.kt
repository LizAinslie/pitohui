package dev.lizainslie.pitohui.core.commands

import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.modules.AbstractModule

class Commands(
    private val bot: Bot
) {
    val commands = mutableListOf<CommandRegistration>()

    suspend fun registerCommand(command: RootCommand, module: AbstractModule) {
        commands.add(CommandRegistration(command, module))
        bot.eachPlatform { it.registerCommand(command, module) }
    }

    fun getRegistration(commandName: String): CommandRegistration? =
        commands.find { it.command.rootName == commandName }

    suspend fun dispatchCommand(handlingCommand: BaseCommand, context: CommandContext) {
        if (!context.module.supportsPlatform(context.platform)) {
            context.respondError("The ${handlingCommand.rootName} command is not supported on ${context.platform.displayName}.")
            return
        }

        handlingCommand.handle(context)
    }
}

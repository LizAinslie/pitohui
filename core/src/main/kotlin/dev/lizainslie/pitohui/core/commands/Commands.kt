package dev.lizainslie.pitohui.core.commands

import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.data.DeveloperOptions
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.modules.ModuleVisibility
import dev.lizainslie.pitohui.core.platforms.UnsupportedPlatformException

import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

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

    private suspend fun respondUnsupportedPlatform(
        handlingCommand: BaseCommand,
        context: CommandContext
    ) {
        context.respondError("The ${handlingCommand.rootName} command is not supported on ${context.platform.displayName}.")
    }

    private suspend fun respondUnsupportedPlatform(
        handlingCommand: BaseCommand,
        context: CommandContext,
        exception: UnsupportedPlatformException,
    ) {
        context.respondError("The ${handlingCommand.rootName} command is not supported on ${exception.currentPlatform.displayName}. It can be used on: ${
            exception.allowedPlatforms.joinToString(
                ", "
            ) { it.displayName }
        }")
    }

    suspend fun dispatchCommand(handlingCommand: BaseCommand, context: CommandContext) {
        if (!context.module.supportsPlatform(context.platform)) {
            respondUnsupportedPlatform(handlingCommand, context)
            return
        }

        if (context.module.visibility == ModuleVisibility.DEVELOPER && !context.callerIsDeveloper()) {
            return // exit silently.
        }

        val devOpts = newSuspendedTransaction {
            DeveloperOptions.getDeveloperOptions(context.callerId)
        }

        try {
            handlingCommand.handle(context)
        } catch (exc: UnsupportedPlatformException) {
            respondUnsupportedPlatform(handlingCommand, context, exc)
        } catch (exc: Exception) {
            if (devOpts != null) context.respondException(exc)
        }

        if (devOpts != null && devOpts.contextDebug) context.dump()
    }
}

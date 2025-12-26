package dev.lizainslie.pitohui.core.commands

import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.config.Configs
import dev.lizainslie.pitohui.core.data.entities.DeveloperOptions
import dev.lizainslie.pitohui.core.logging.suspendLogModule
import dev.lizainslie.pitohui.core.logging.suspendLogPlatform
import dev.lizainslie.pitohui.core.logging.suspendLogTag
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.modules.ModuleVisibility
import dev.lizainslie.pitohui.core.platforms.UnsupportedPlatformException

import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory

class Commands(
    private val bot: Bot
) {
    val parsingConfig by Configs.config<CommandParsingConfig>()
    val commands = mutableListOf<CommandRegistration>()
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun registerCommand(command: RootCommand, module: AbstractModule) {
        suspendLogModule(module) {
            log.info("Registering command: '${command.name}'.")
            commands.add(CommandRegistration(command, module))

            bot.eachPlatform {
                suspendLogPlatform(it) {
                    if (module.supportsPlatform(it)
                        && command.supportsPlatform(it)
                    )
                        it.registerCommand(command, module)
                }
            }
        }
    }

    suspend fun unregisterModuleCommands(module: AbstractModule) {
        suspendLogModule(module) {
            log.info("Unregistering commands for module '${module.name}'.")
            val toRemove = commands.filter { it.module == module }
            commands.removeAll(toRemove)

            bot.eachPlatform {
                suspendLogPlatform(it) {
                    for (reg in toRemove) {
                        if (module.supportsPlatform(it) && reg.command.supportsPlatform(it)) {
                            it.unregisterCommand(reg.command, module)
                        }
                    }
                }
            }
        }
    }

    suspend fun registerModuleCommands(module: AbstractModule) {
        suspendLogModule(module) {
            log.info("Registering commands for module '${module.name}'.")
            for (command in module.commands) {
                registerCommand(command, module)
            }
        }
    }


    fun getRegistration(commandName: String): CommandRegistration? =
        commands.find { it.command.rootCommand.name == commandName }

    private suspend fun respondUnsupportedPlatform(
        handlingCommand: BaseCommand,
        context: CommandContext
    ) {
        context.respondError("The ${handlingCommand.rootCommand.name} command is not supported on ${context.platform.displayName}.")
    }

    private suspend fun respondUnsupportedPlatform(
        handlingCommand: BaseCommand,
        context: CommandContext,
        exception: UnsupportedPlatformException,
    ) {
        context.respondError("The ${handlingCommand.rootCommand.name} command is not supported on ${exception.currentPlatform.displayName}. It can be used on: ${
            exception.allowedPlatforms.joinToString(
                ", "
            ) { it.displayName }
        }")
    }

    suspend fun dispatchCommand(handlingCommand: BaseCommand, context: CommandContext) {
        suspendLogModule(context.module) {
            suspendLogPlatform(context.platform) {
                if (!context.module.supportsPlatform(context.platform)) {
                    respondUnsupportedPlatform(handlingCommand, context)
                    return@suspendLogPlatform
                }

                if (context.module.visibility == ModuleVisibility.DEVELOPER && !context.callerIsDeveloper()) {
                    return@suspendLogPlatform // exit silently.
                }

                if (handlingCommand.rootCommand.communityOnly && !context.isInCommunity) {
                    context.respondError("The ${handlingCommand.rootCommand.name} command can only be used in communities.")
                    return@suspendLogPlatform
                }

                val devOpts = newSuspendedTransaction {
                    DeveloperOptions.getDeveloperOptions(context.callerId)
                }

                try {
                    log.info("Handling command: '${handlingCommand.rootCommand.name}' on platform '${context.platform.displayName}'.")
                    suspendLogTag("command: ${handlingCommand.rootCommand.name}") {
                        handlingCommand.handle(context)
                    }
                } catch (exc: UnsupportedPlatformException) {
                    respondUnsupportedPlatform(handlingCommand, context, exc)
                } catch (exc: Exception) {
                    log.error("Handling command '${handlingCommand.rootCommand.name}' failed with ${exc.message}: ", exc)
                    if (devOpts != null) context.respondException(exc)
                }

                if (devOpts != null && devOpts.contextDebug) {
                    log.debug("Dumping context.")
                    context.dump()
                }
            }
        }
    }
}

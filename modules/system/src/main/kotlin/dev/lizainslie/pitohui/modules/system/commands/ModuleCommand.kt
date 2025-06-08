package dev.lizainslie.pitohui.modules.system.commands

import dev.kord.common.entity.Permission
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.lizainslie.pitohui.core.commands.ArgumentTypes
import dev.lizainslie.pitohui.core.commands.CommandContext
import dev.lizainslie.pitohui.core.commands.defineCommand
import dev.lizainslie.pitohui.core.commands.platform.DiscordSlashCommandContext
import dev.lizainslie.pitohui.core.data.ModuleSwitch
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.Platforms
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

fun checkManagementPermission(context: CommandContext): Boolean {
    return when (context) {
        is DiscordSlashCommandContext -> {
            when (val interaction = context.interaction) {
                is GuildChatInputCommandInteraction -> {
                    // Check if the user has the "Manage Server" permission
                    interaction.user.permissions?.contains(Permission.ManageGuild) ?: false
                }
                else -> false
            }
        }
        else -> false
    }
}

val ModuleCommand = defineCommand("module", "Manage modules") {
    platforms(*Platforms.entries.toTypedArray())

    subCommand("enable", "Enable a module") {
        val moduleNameArg = argument("module_name", "The name of the module to enable", ArgumentTypes.STRING, required = true)

        handle { context ->
            val moduleName = moduleNameArg.resolve(context)
            if (moduleName != null) {
                val module = context.bot.modules.firstOrNull { it.name == moduleName } ?: run {
                    context.respond("Module '$moduleName' does not exist.")
                    return@handle
                }

                if (!module.optional && !checkManagementPermission(context)) {
                    context.respond("You do not have permission to manage this module.")
                    return@handle
                }

                val platformId = when (context) {
                    is DiscordSlashCommandContext -> {
                        when (context.interaction) {
                            is GuildChatInputCommandInteraction -> PlatformId.fromSnowflake((context.interaction as GuildChatInputCommandInteraction).guildId)
                            else -> {
                                context.respond("This command can only be used in a server.")
                                return@handle
                            }
                        }
                    }
                    else -> {
                        context.respond("This command is not supported on this platform.")
                        return@handle
                    }
                }

                newSuspendedTransaction {
                    val moduleSwitch = ModuleSwitch.getSwitch(platformId, module.name)

                    println("hawk")
                    if (moduleSwitch != null) {
                        if (moduleSwitch.enabled) {
                            context.respond("Module '${module.name}' is already enabled.")
                            return@newSuspendedTransaction
                        } else {
                            println("tuah")
                            moduleSwitch.enabled = true
                        }
                    } else {
                        println("frot on that thang")
                        ModuleSwitch.createSwitch(platformId, module.name, enabled = true)
                    }

                    context.respond("Module '${module.name}' has been enabled.")
                }
            } else {
                context.respond("Please provide a valid module name to enable.")
            }
        }
    }

    subCommand("disable", "Disable a module") {
        val moduleNameArg = argument("module_name", "The name of the module to disable", ArgumentTypes.STRING, required = true)

        handle { context ->
            val moduleName = moduleNameArg.resolve(context)
            if (moduleName != null) {

                // todo: abstract this into a function

                val module = context.bot.modules.firstOrNull { it.name == moduleName } ?: run {
                    context.respond("Module '$moduleName' does not exist.")
                    return@handle
                }

                if (!module.optional && !checkManagementPermission(context)) {
                    context.respond("You do not have permission to manage this module.")
                    return@handle
                }

                val platformId = when (context) {
                    is DiscordSlashCommandContext -> {
                        when (context.interaction) {
                            is GuildChatInputCommandInteraction -> PlatformId.fromSnowflake((context.interaction as GuildChatInputCommandInteraction).guildId)
                            else -> {
                                context.respond("This command can only be used in a server.")
                                return@handle
                            }
                        }
                    }
                    else -> {
                        context.respond("This command is not supported on this platform.")
                        return@handle
                    }
                }

                newSuspendedTransaction {
                    val moduleSwitch = ModuleSwitch.getSwitch(platformId, module.name)
                    if (moduleSwitch != null) {
                        if (!moduleSwitch.enabled) {
                            context.respond("Module '${module.name}' is already disabled.")
                            return@newSuspendedTransaction
                        } else {
                            moduleSwitch.enabled = false
                        }
                    } else {
                        ModuleSwitch.createSwitch(platformId, module.name, enabled = false)
                    }

                    context.respond("Module '${module.name}' has been disabled.")
                }
            } else {
                context.respond("Please provide a valid module name to disable.")
            }
        }
    }
}
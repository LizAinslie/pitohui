package dev.lizainslie.pitohui.modules.system.commands

import dev.kord.common.entity.Permission
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.lizainslie.pitohui.core.commands.ArgumentTypes
import dev.lizainslie.pitohui.core.commands.CommandContext
import dev.lizainslie.pitohui.core.commands.defineCommand
import dev.lizainslie.pitohui.platforms.discord.commands.DiscordSlashCommandContext
import dev.lizainslie.pitohui.core.data.ModuleSwitch
import dev.lizainslie.pitohui.core.platforms.Platforms
import dev.lizainslie.pitohui.platforms.discord.extensions.DISCORD
import dev.lizainslie.pitohui.platforms.discord.extensions.platform
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

fun CommandContext.checkManagementPermission(): Boolean {
    return when (this) {
        is DiscordSlashCommandContext -> {
            when (val typedInteraction = interaction) {
                is GuildChatInputCommandInteraction -> {
                    // Check if the user has the "Manage Server" permission
                    typedInteraction.user.permissions?.contains(Permission.ManageGuild) ?: false
                }

                else -> false
            }
        }

        else -> false
    }
}

val ModuleCommand = defineCommand("module", "Manage modules") {
    platforms(Platforms.DISCORD)

    subCommand("enable", "Enable a module") {
        val moduleNameArg =
            argument("module_name", "The name of the module to enable", ArgumentTypes.STRING, required = true)

        handle {
            val moduleName = args[moduleNameArg]
            if (moduleName != null) {
                val module = bot.modules.firstOrNull { it.name == moduleName } ?: run {
                    respond("Module '$moduleName' does not exist.")
                    return@handle
                }

                if (!module.optional && !checkManagementPermission()) {
                    respond("You do not have permission to manage this module.")
                    return@handle
                }

                val platformId = when (this) {
                    is DiscordSlashCommandContext -> {
                        when (interaction) {
                            is GuildChatInputCommandInteraction -> guildId!!
                            else -> {
                                respond("This command can only be used in a server.")
                                return@handle
                            }
                        }
                    }

                    else -> {
                        respond("This command is not supported on this platform.")
                        return@handle
                    }
                }

                newSuspendedTransaction {
                    val moduleSwitch = ModuleSwitch.getSwitch(platformId, module.name)

                    println("hawk")
                    if (moduleSwitch != null) {
                        if (moduleSwitch.enabled) {
                            respond("Module '${module.name}' is already enabled.")
                            return@newSuspendedTransaction
                        } else {
                            println("tuah")
                            moduleSwitch.enabled = true
                        }
                    } else {
                        println("frot on that thang")
                        ModuleSwitch.createSwitch(platformId, module.name, enabled = true)
                    }

                    respond("Module '${module.name}' has been enabled.")
                }
            } else {
                respond("Please provide a valid module name to enable.")
            }
        }
    }

    subCommand("disable", "Disable a module") {
        val moduleNameArg =
            argument("module_name", "The name of the module to disable", ArgumentTypes.STRING, required = true)

        handle {
            val moduleName = args[moduleNameArg]
            if (moduleName != null) {

                // todo: abstract this into a function

                val module = bot.modules.firstOrNull { it.name == moduleName } ?: run {
                    respond("Module '$moduleName' does not exist.")
                    return@handle
                }

                if (!module.optional && !checkManagementPermission()) {
                    respond("You do not have permission to manage this module.")
                    return@handle
                }

                val platformId = when (this) {
                    is DiscordSlashCommandContext -> {
                        when (interaction) {
                            is GuildChatInputCommandInteraction -> (interaction as GuildChatInputCommandInteraction).guildId.platform()
                            else -> {
                                respond("This command can only be used in a server.")
                                return@handle
                            }
                        }
                    }

                    else -> {
                        respond("This command is not supported on this platform.")
                        return@handle
                    }
                }

                newSuspendedTransaction {
                    val moduleSwitch = ModuleSwitch.getSwitch(platformId, module.name)
                    if (moduleSwitch != null) {
                        if (!moduleSwitch.enabled) {
                            respond("Module '${module.name}' is already disabled.")
                            return@newSuspendedTransaction
                        } else {
                            moduleSwitch.enabled = false
                        }
                    } else {
                        ModuleSwitch.createSwitch(platformId, module.name, enabled = false)
                    }

                    respond("Module '${module.name}' has been disabled.")
                }
            } else {
                respond("Please provide a valid module name to disable.")
            }
        }
    }
}

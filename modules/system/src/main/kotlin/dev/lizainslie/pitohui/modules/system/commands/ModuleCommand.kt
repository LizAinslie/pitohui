package dev.lizainslie.pitohui.modules.system.commands

import dev.kord.common.entity.Permission
import dev.lizainslie.pitohui.core.commands.ArgumentTypes
import dev.lizainslie.pitohui.core.commands.CommandContext
import dev.lizainslie.pitohui.core.commands.defineCommand
import dev.lizainslie.pitohui.core.data.ModuleSwitch
import dev.lizainslie.pitohui.platforms.discord.Discord
import dev.lizainslie.pitohui.platforms.discord.commands.DiscordCommandContext
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun CommandContext.checkManagementPermission(): Boolean {
    return when (this) {
        is DiscordCommandContext -> checkCallerPermission(Permission.ManageGuild)

        else -> false
    }
}

val ModuleCommand = defineCommand("module", "Manage modules") {
    platforms(Discord)

    subCommand("enable", "Enable a module") {
        val moduleNameArg =
            argument("module_name", "The name of the module to enable", ArgumentTypes.STRING, required = true)

        handle {
            val moduleName = args[moduleNameArg]
            if (moduleName != null) {
                val module = bot.modules.firstOrNull { it.name == moduleName } ?: run {
                    respondError("Module **$moduleName** does not exist.")
                    return@handle
                }

                if (!checkManagementPermission()) {
                    respondError("You do not have permission to manage modules in this community.")
                    return@handle
                }

                if (!module.optional) {
                    respondError("Module **${module.name}** cannot be toggled.")
                    return@handle
                }

                val communityId = when (this) {
                    is DiscordCommandContext -> {
                        if (isInGuild) guildId!!
                        else {
                            respondError("This command must be used in a community.")
                            return@handle
                        }
                    }

                    else -> {
                        respondError("This command is not supported on this platform.")
                        return@handle
                    }
                }

                newSuspendedTransaction {
                    val moduleSwitch = ModuleSwitch.getSwitch(communityId, module.name)

                    if (moduleSwitch != null) {
                        if (moduleSwitch.enabled) {
                            respondPrivate("Module **${module.name}** is already enabled.")
                            return@newSuspendedTransaction
                        } else {
                            moduleSwitch.enabled = true
                        }
                    } else
                        ModuleSwitch.createSwitch(communityId, module.name, enabled = true)

                    respondPrivate("Module **${module.name}** has been enabled.")
                }
            } else {
                respondError("Please provide a valid module name to enable.")
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
                    respondError("Module **$moduleName** does not exist.")
                    return@handle
                }

                if (!checkManagementPermission()) {
                    respondError("You do not have permission to manage modules in this community.")
                    return@handle
                }

                if (!module.optional) {
                    respondError("Module **${module.name}** cannot be toggled.")
                    return@handle
                }

                val communityId = when (this) {
                    is DiscordCommandContext -> {
                        if (isInGuild) guildId!!
                        else {
                            respondError("This command can only be used in a server.")
                            return@handle
                        }
                    }

                    else -> {
                        respondError("This command is not supported on this platform.")
                        return@handle
                    }
                }

                newSuspendedTransaction {
                    val moduleSwitch = ModuleSwitch.getSwitch(communityId, module.name)
                    if (moduleSwitch != null) {
                        if (!moduleSwitch.enabled) {
                            respondPrivate("Module **${module.name}** is already disabled.")
                            return@newSuspendedTransaction
                        } else {
                            moduleSwitch.enabled = false
                        }
                    } else {
                        ModuleSwitch.createSwitch(communityId, module.name, enabled = false)
                    }

                    respondPrivate("Module **${module.name}** has been disabled.")
                }
            } else {
                respondError("Please provide a valid module name to disable.")
            }
        }
    }
}

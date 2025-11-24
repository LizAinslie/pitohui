package dev.lizainslie.pitohui.modules.system.commands

import dev.kord.common.entity.Permission
import dev.lizainslie.pitohui.core.commands.ArgumentTypes
import dev.lizainslie.pitohui.core.commands.CommandContext
import dev.lizainslie.pitohui.core.commands.defineCommand
import dev.lizainslie.pitohui.core.data.ModuleSwitch
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.modules.ModuleVisibility
import dev.lizainslie.pitohui.platforms.discord.Discord
import dev.lizainslie.pitohui.platforms.discord.commands.DiscordCommandContext
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun CommandContext.checkManagementPermission(): Boolean {
    return when (this) {
        is DiscordCommandContext -> checkCallerPermission(Permission.ManageGuild)

        else -> false
    }
}

fun buildModuleDescription(module: AbstractModule, context: CommandContext, supportsMarkdown: Boolean = true): String {
    var description = module.description

    if (!module.optional) description += "\n-# *This module is not optional and enabled in every community.*"

    if (context.isInCommunity) {
        description += "\n${if (supportsMarkdown) "**" else ""}Enabled:${if (supportsMarkdown) "**" else ""} ${if (module.isEnabledForCommunity(context.communityId!!)) "yes" else "no"}"
    }

    return description
}

suspend fun shouldListModule(module: AbstractModule, context: CommandContext) = when(module.visibility) {
    ModuleVisibility.DEVELOPER -> false
    ModuleVisibility.PUBLIC -> true
    ModuleVisibility.MODERATOR -> {
        when (context) {
            is DiscordCommandContext -> context.checkCallerPermission(Permission.ManageGuild) // todo: change this to a more robust role-based system
            else -> false
        }
    }
}

val ModuleCommand = defineCommand("module", "Manage modules") {
    platforms(Discord)

    subCommand("list", "List all modules") {
        handle {
            val modules = bot.modules.loadedModules.filter {
                shouldListModule(it.instance, this@handle)
            }

            if (this is DiscordCommandContext) {
                respond {
                    title = "Pitohui Bot Modules"

                    description = "Below are modules that are available on this bot, their descriptions, and whether or not they're enabled."

                    modules.forEach { module ->
                        field(module.name, inline = false) {
                            buildModuleDescription(module.instance, this@handle)
                        }
                    }
                }
            }
        }
    }

    subCommand("enable", "Enable a module") {
        val moduleNameArg =
            argument("module_name", "The name of the module to enable", ArgumentTypes.STRING, required = true)

        handle {
            val moduleName = args[moduleNameArg]!!.lowercase()

            val module = bot.modules.loadedModules.firstOrNull { it.name == moduleName } ?: run {
                respondError("Module **$moduleName** does not exist.")
                return@handle
            }

            if (!checkManagementPermission()) {
                respondError("You do not have permission to manage modules in this community.")
                return@handle
            }

            if (!module.instance.optional) {
                respondError("Module **${module.name}** cannot be toggled.")
                return@handle
            }

            if (!isInCommunity) {
                respondError("This command must be used in a community.")
                return@handle
            }

            newSuspendedTransaction {
                ModuleSwitch.getSwitch(
                    communityId!!,
                    module.name
                )?.also {
                    it.enabled = true
                } ?: ModuleSwitch.createSwitch(
                    communityId!!,
                    module.name,
                    true
                )
            }

            respondPrivate("Module **${module.name}** has been enabled.")
        }
    }

    subCommand("disable", "Disable a module") {
        val moduleNameArg =
            argument("module_name", "The name of the module to disable", ArgumentTypes.STRING, required = true)

        handle {
            val moduleName = args[moduleNameArg]!!.lowercase()

            val module = bot.modules.loadedModules.firstOrNull { it.name == moduleName } ?: run {
                respondError("Module **$moduleName** does not exist.")
                return@handle
            }

            if (!checkManagementPermission()) {
                respondError("You do not have permission to manage modules in this community.")
                return@handle
            }

            if (!module.instance.optional) {
                respondError("Module **${module.name}** cannot be toggled.")
                return@handle
            }

            if (!isInCommunity) {
                respondError("This command must be used in a community.")
                return@handle
            }

            newSuspendedTransaction {
                ModuleSwitch.getSwitch(
                    communityId!!,
                    module.name
                )?.also {
                    it.enabled = false
                } ?: ModuleSwitch.createSwitch(
                    communityId!!,
                    module.name,
                    false
                )
            }

            respondPrivate("Module **${module.name}** has been disabled.")
        }
    }
}

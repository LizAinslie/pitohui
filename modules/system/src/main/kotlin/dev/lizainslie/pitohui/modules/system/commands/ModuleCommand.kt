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
            val moduleName = args[moduleNameArg]!!.lowercase()

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

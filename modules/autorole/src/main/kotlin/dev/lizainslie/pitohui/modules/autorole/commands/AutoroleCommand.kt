package dev.lizainslie.pitohui.modules.autorole.commands

import dev.kord.common.entity.Permission
import dev.lizainslie.pitohui.core.commands.ArgumentTypes
import dev.lizainslie.pitohui.core.commands.defineCommand
import dev.lizainslie.pitohui.modules.autorole.data.AutoroleSettings
import dev.lizainslie.pitohui.platforms.discord.Discord
import dev.lizainslie.pitohui.platforms.discord.commands.DiscordCommandContext
import dev.lizainslie.pitohui.platforms.discord.commands.enforceDiscord
import dev.lizainslie.pitohui.platforms.discord.extensions.platform
import dev.lizainslie.pitohui.platforms.discord.extensions.snowflake
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

val AutoroleCommand = defineCommand(
    "autorole",
    "Manage community autorole command",
) {
    platforms(Discord)

    subCommand(
        "setMemberRole",
        "Set the role to be assigned to new users",
    ) {
        val roleArg = argument("role", "The role to apply to new users", ArgumentTypes.ROLE, required = true)

        handle {
            enforceDiscord<DiscordCommandContext> {
                enforceGuild { guild ->
                    val roleId = args[roleArg]!!

                    if (!guild.getMember(callerId.snowflake).getPermissions()
                            .contains(Permission.ManageGuild)
                    ) {
                        return@enforceGuild respondError("This command can only be executed by someone who has permission to manage the server")
                    }

                    newSuspendedTransaction {
                        val communityId = guild.id.platform

                        var autoroleSettings = AutoroleSettings.getAutoroleSettings(communityId)

                        if (autoroleSettings == null)
                            autoroleSettings = AutoroleSettings.create(communityId, memberRole = roleId.id)
                        else
                            autoroleSettings.memberRoleId = roleId.id

                        val role = guild.getRole(roleId.snowflake)

                        respondPrivate("Member autorole set to ${role.mention}.")
                    }
                }
            }
        }
    }
}
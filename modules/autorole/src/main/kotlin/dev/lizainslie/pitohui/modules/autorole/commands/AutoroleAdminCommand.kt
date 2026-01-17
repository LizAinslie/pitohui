package dev.lizainslie.pitohui.modules.autorole.commands

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.lizainslie.moeka.core.commands.argument.ArgumentTypes
import dev.lizainslie.moeka.core.commands.defineCommand
import dev.lizainslie.moeka.platforms.discord.Discord
import dev.lizainslie.moeka.platforms.discord.commands.DiscordCommandContext
import dev.lizainslie.moeka.platforms.discord.commands.enforceDiscordType
import dev.lizainslie.moeka.platforms.discord.extensions.snowflake
import dev.lizainslie.pitohui.modules.autorole.data.entities.AutoroleSettings
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

val AutoroleAdminCommand =
    defineCommand(
        "autorole_admin",
        "Manage community autorole settings",
    ) {
        platform(Discord) {
            defaultMemberPermissions = Permissions(Permission.ManageGuild)
            dmPermission = false
        }

        communityOnly = true

        subCommand(
            "set_member_role",
            "Set the role to be assigned to new users",
        ) {
            val roleArg =
                argument("role", "The role to apply to new users", ArgumentTypes.ROLE) {
                    required = true
                }

            handle {
                val roleId by roleArg.require()

                enforceDiscordType<DiscordCommandContext> {
                    enforceGuild { guild ->

                        if (!checkCallerPermission(Permission.ManageGuild)) {
                            respondError("This command can only be executed by someone who has permission to manage the server")
                            return@enforceGuild
                        }

                        newSuspendedTransaction {
                            var autoroleSettings = AutoroleSettings.getAutoroleSettings(communityId!!)

                            if (autoroleSettings == null) {
                                autoroleSettings = AutoroleSettings.create(communityId!!, memberRole = roleId.id)
                            } else {
                                autoroleSettings.memberRoleId = roleId.id
                            }

                            val role = guild.getRole(roleId.snowflake)

                            respondPrivate("Member autorole set to ${role.mention}.")
                        }
                    }
                }
            }
        }

        subCommand(
            "set_bot_role",
            "Set the role to be assigned to new bots",
        ) {
            val roleArg =
                argument("role", "The role to apply to new bot", ArgumentTypes.ROLE) {
                    required = true
                }

            handle {
                val roleId by roleArg.require()

                enforceDiscordType<DiscordCommandContext> {
                    enforceGuild { guild ->
                        if (!checkCallerPermission(Permission.ManageGuild)) {
                            respondError("This command can only be executed by someone who has permission to manage the server")
                            return@enforceGuild
                        }

                        newSuspendedTransaction {
                            var autoroleSettings = AutoroleSettings.getAutoroleSettings(communityId!!)

                            if (autoroleSettings == null) {
                                autoroleSettings = AutoroleSettings.create(communityId!!, botRole = roleId.id)
                            } else {
                                autoroleSettings.botRoleId = roleId.id
                            }

                            val role = guild.getRole(roleId.snowflake)

                            respondPrivate("Bot autorole set to ${role.mention}.")
                        }
                    }
                }
            }
        }
    }

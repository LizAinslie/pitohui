package dev.lizainslie.pitohui.modules.vcnotify.commands

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.lizainslie.pitohui.core.commands.argument.ArgumentTypes
import dev.lizainslie.pitohui.core.commands.defineCommand
import dev.lizainslie.pitohui.modules.vcnotify.data.entities.VcNotifySettings
import dev.lizainslie.pitohui.platforms.discord.Discord
import dev.lizainslie.pitohui.platforms.discord.commands.DiscordCommandContext
import dev.lizainslie.pitohui.platforms.discord.commands.enforceDiscordType
import dev.lizainslie.pitohui.platforms.discord.extensions.platform
import dev.lizainslie.pitohui.platforms.discord.extensions.snowflake
import dev.lizainslie.pitohui.util.time.format
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlin.time.Duration.Companion.minutes

val VcNotifyAdminCommand = defineCommand("vcnotify_admin", "Admin commands for the VcNotify module") {
    platform(Discord) {
        defaultMemberPermissions = Permissions(Permission.ManageGuild)
        dmPermission = false
    }

    communityOnly = true

    subCommand("info", "Documentation for VcNotify") {
        handle {
            enforceDiscordType<DiscordCommandContext> {
                respondPrivate {
                    title = "VcNotify Documentation"
                    description = """
VcNotify is a module that allows users to notify others when they are in a voice channel.

To set up VcNotify, you need to use the `/vcnotify_admin setrole` command to specify a role that will be notified when a user runs the main command.

If you want to customize the message format, you can run `/vcnotify_admin setformat <format>`, where `<format>` is the message you want to send. You can use `{role}` to mention the notification role, `{user}` to mention the user who ran the command, and `{channel}` to mention the voice channel they are in, or {channelLink} for a link to the channel instead (provides a nice embed). The default format is `{role} {user} is now in {channelLink}! Join them!`.

To customize the cooldown, you can run `/vcnotify_admin setcooldown <duration>`, where `<duration>` is the cooldown duration in minutes. The default cooldown is 30 minutes.
                    """.trimIndent()
                }
            }
        }
    }

    subCommand("setrole", "Set the role to be notified when a user runs the main command") {
        val roleArgument = argument("role", "The role to set for notifications", ArgumentTypes.ROLE) {
            required = true
        }

        handle {
            enforceDiscordType<DiscordCommandContext> {
                enforceGuild { guild ->
                    if (!checkCallerPermission(Permission.ManageGuild)) {
                        respondError("You do not have permission to manage VcNotify settings.")
                        return@enforceGuild
                    }

                    val roleId = args[roleArgument]

                    if (roleId == null) {
                        respondError("Invalid role specified. Please provide a valid role.")
                        return@enforceGuild
                    }

                    newSuspendedTransaction {
                        var settings = VcNotifySettings.getSettings(guildId!!)

                        val role = guild.getRoleOrNull(roleId.snowflake)

                        if (role != null) {
                            if (settings == null)
                                settings = VcNotifySettings.create(
                                    communityId = guild.id.platform,
                                    roleId = roleId,
                                )
                            else settings.roleId = roleId.id

                            respond("Notification role set to ${role.mention}.")
                        } else respond("Role with ID $roleId not found in this server.")
                    }
                }
            }
        }
    }

    subCommand("setformat", "Set the message format for notifications") {
        val formatArgument = argument("format", "The message format to set", ArgumentTypes.STRING) {
            required = true
        }

        handle {
            enforceDiscordType<DiscordCommandContext> {
                enforceGuild { guild ->
                    if (!checkCallerPermission(Permission.ManageGuild)) {
                        respondError("You do not have permission to manage VcNotify settings.")
                        return@enforceGuild
                    }

                    val communityId = guild.id.platform

                    newSuspendedTransaction {
                        var settings = VcNotifySettings.getSettings(communityId)

                        if (settings == null)
                            settings = VcNotifySettings.create(communityId)

                        settings.messageFormat =
                            args[formatArgument] ?: "{role} {user} is now in {channelLink}! Join them!"

                        respondPrivate("Notification message format set to: `${settings.messageFormat}`")
                    }
                }
            }
        }
    }

    subCommand("setcooldown", "Set the cooldown duration for notifications") {
        val cooldownArgument =
            argument("duration", "The cooldown duration", ArgumentTypes.DURATION) {
                required = true
            }

        handle {
            enforceDiscordType<DiscordCommandContext> {
                enforceGuild { guild ->
                    if (!checkCallerPermission(Permission.ManageGuild)) {
                        respondError("You do not have permission to manage VcNotify settings.")
                        return@enforceGuild
                    }

                    val communityId = guild.id.platform

                    newSuspendedTransaction {
                        var settings = VcNotifySettings.getSettings(communityId)

                        if (settings == null)
                            settings = VcNotifySettings.create(communityId)

                        val cooldown = args[cooldownArgument] ?: 30.minutes
                        settings.cooldown = cooldown

                        respondPrivate("Notification cooldown set to ${settings.cooldown.format()}.")
                    }
                }
            }
        }
    }
}

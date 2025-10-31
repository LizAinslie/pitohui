package dev.lizainslie.pitohui.modules.vcnotify.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.message.embed
import dev.lizainslie.pitohui.core.commands.ArgumentTypes
import dev.lizainslie.pitohui.core.commands.defineCommand
import dev.lizainslie.pitohui.core.platforms.Platforms
import dev.lizainslie.pitohui.modules.system.commands.checkManagementPermission
import dev.lizainslie.pitohui.modules.vcnotify.data.VcNotifySettings
import dev.lizainslie.pitohui.platforms.discord.commands.DiscordSlashCommandContext
import dev.lizainslie.pitohui.platforms.discord.extensions.DISCORD
import dev.lizainslie.pitohui.platforms.discord.extensions.platform
import kotlinx.coroutines.flow.firstOrNull
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlin.time.Duration.Companion.minutes

val VcNotifyAdminCommand = defineCommand("vcnotify_admin", "Admin commands for the VcNotify module") {
    platforms(Platforms.DISCORD)

    subCommand("info", "Documentation for VcNotify") {
        handle {
            if (!checkManagementPermission()) {
                respond("You do not have permission to manage VcNotify settings.")
                return@handle
            }

            if (this !is DiscordSlashCommandContext) {
                respond("This command is currently only available on Discord.")
                return@handle
            }

            interaction.respondEphemeral {
                embed {
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
        val roleArgument = argument("role", "The role to set for notifications", ArgumentTypes.ROLE, required = true)

        handle {
            if (!checkManagementPermission()) {
                respond("You do not have permission to manage VcNotify settings.")
                return@handle
            }

            if (this !is DiscordSlashCommandContext) {
                respond("This command is currently only available on Discord.")
                return@handle
            }

            if (interaction !is GuildChatInputCommandInteraction) {
                respond("This command can only be used in a server.")
                return@handle
            }

            val guildInteraction = interaction as GuildChatInputCommandInteraction

            // todo: perhaps use ulong instead of long in the argparsing logic
            val roleId = args[roleArgument]

            if (roleId == null) {
                respond("Invalid role specified. Please provide a valid role.")
                return@handle
            }

            val communityId = guildInteraction.guildId.platform()

            newSuspendedTransaction {
                var settings = VcNotifySettings.getSettings(communityId)

                val role = guildInteraction.guild.roles.firstOrNull { it.id == roleId }

                if (role != null) {
                    if (settings == null)
                        settings = VcNotifySettings.create(
                            communityId = communityId,
                            roleId = roleId,
                        )
                    else settings.roleId = roleId.id

                    respond("Notification role set to ${role.mention}.")
                } else respond("Role with ID $roleId not found in this server.")
            }
        }
    }

    subCommand("setformat", "Set the message format for notifications") {
        val formatArgument = argument("format", "The message format to set", ArgumentTypes.STRING, required = true)

        handle {
            if (!checkManagementPermission()) {
                respond("You do not have permission to manage VcNotify settings.")
                return@handle
            }

            if (this !is DiscordSlashCommandContext) {
                respond("This command is currently only available on Discord.")
                return@handle
            }

            if (interaction !is GuildChatInputCommandInteraction) {
                respond("This command can only be used in a server.")
                return@handle
            }

            newSuspendedTransaction {
                var settings = VcNotifySettings.getSettings(guildId!!)

                if (settings == null)
                    settings = VcNotifySettings.create(guildId!!)

                settings.messageFormat =
                    args[formatArgument] ?: "{role} {user} is now in {channelLink}! Join them!"

                respond("Notification message format set to: `${settings.messageFormat}`")
            }
        }
    }

    subCommand("setcooldown", "Set the cooldown duration for notifications") {
        val cooldownArgument =
            argument("duration", "The cooldown duration in minutes", ArgumentTypes.INT, required = true)

        handle {
            if (!checkManagementPermission()) {
                respond("You do not have permission to manage VcNotify settings.")
                return@handle
            }

            if (this !is DiscordSlashCommandContext) {
                respond("This command is currently only available on Discord.")
                return@handle
            }

            if (interaction !is GuildChatInputCommandInteraction) {
                respond("This command can only be used in a server.")
                return@handle
            }

            val guildInteraction = interaction as GuildChatInputCommandInteraction
            val communityId = guildInteraction.guildId.platform()

            newSuspendedTransaction {
                var settings = VcNotifySettings.getSettings(communityId)

                if (settings == null)
                    settings = VcNotifySettings.create(communityId)

                val cooldownMinutes = args[cooldownArgument] ?: 30
                settings.cooldown = cooldownMinutes.minutes

                respond("Notification cooldown set to ${settings.cooldown.inWholeMinutes} minutes.")
            }
        }
    }
}

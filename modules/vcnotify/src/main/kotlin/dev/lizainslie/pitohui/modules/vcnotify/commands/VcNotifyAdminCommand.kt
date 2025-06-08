package dev.lizainslie.pitohui.modules.vcnotify.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import dev.lizainslie.pitohui.core.commands.ArgumentType
import dev.lizainslie.pitohui.core.commands.ArgumentTypes
import dev.lizainslie.pitohui.core.commands.defineCommand
import dev.lizainslie.pitohui.core.commands.platform.DiscordSlashCommandContext
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.Platforms
import dev.lizainslie.pitohui.modules.system.commands.checkManagementPermission
import dev.lizainslie.pitohui.modules.vcnotify.data.VcNotifySettings
import kotlinx.coroutines.flow.firstOrNull
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration.Companion.minutes

val VcNotifyAdminCommand = defineCommand("vcnotify_admin", "Admin commands for the VcNotify module") {
    platforms(Platforms.DISCORD)

    subCommand("info", "Documentation for VcNotify") {
        handle { context ->
            if (!checkManagementPermission(context)) {
                context.respond("You do not have permission to manage VcNotify settings.")
                return@handle
            }

            if (context !is DiscordSlashCommandContext) {
                context.respond("This command is currently only available on Discord.")
                return@handle
            }

            context.interaction.respondEphemeral {
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
        val roleArgument =  argument("role", "The role to set for notifications", ArgumentTypes.ROLE, required = true)

        handle { context ->
            if (!checkManagementPermission(context)) {
                context.respond("You do not have permission to manage VcNotify settings.")
                return@handle
            }

            if (context !is DiscordSlashCommandContext) {
                context.respond("This command is currently only available on Discord.")
                return@handle
            }

            if (context.interaction !is GuildChatInputCommandInteraction) {
                context.respond("This command can only be used in a server.")
                return@handle
            }

            val guildInteraction = context.interaction as GuildChatInputCommandInteraction

            // todo: perhaps use ulong instead of long in the argparsing logic
            val roleId = roleArgument.resolve(context)

            if (roleId == null) {
                context.respond("Invalid role specified. Please provide a valid role.")
                return@handle
            }

            val platformId = PlatformId.fromSnowflake(guildInteraction.guildId)

            newSuspendedTransaction {
                var settings = VcNotifySettings.getSettings(platformId)

                val role = guildInteraction.guild.roles.firstOrNull { it.id.value == roleId }

                if (role != null) {
                    if (settings == null)
                        settings = VcNotifySettings.create(
                            platformId = platformId,
                            roleId = roleId,
                        )
                    else settings.roleId = roleId

                    context.respond("Notification role set to ${role.mention}.")
                } else context.respond("Role with ID $roleId not found in this server.")
            }
        }
    }

    subCommand("setformat", "Set the message format for notifications") {
        val formatArgument = argument("format", "The message format to set", ArgumentTypes.STRING, required = true)

        handle { context ->
            if (!checkManagementPermission(context)) {
                context.respond("You do not have permission to manage VcNotify settings.")
                return@handle
            }

            if (context !is DiscordSlashCommandContext) {
                context.respond("This command is currently only available on Discord.")
                return@handle
            }

            if (context.interaction !is GuildChatInputCommandInteraction) {
                context.respond("This command can only be used in a server.")
                return@handle
            }

            val guildInteraction = context.interaction as GuildChatInputCommandInteraction
            val platformId = PlatformId.fromSnowflake(guildInteraction.guildId)

            newSuspendedTransaction {
                var settings = VcNotifySettings.getSettings(platformId)

                if (settings == null) {
                    settings = VcNotifySettings.create(platformId)
                }

                settings.messageFormat = formatArgument.resolve(context) ?: "{role} {user} is now in {channelLink}! Join them!"

                context.respond("Notification message format set to: `${settings.messageFormat}`")
            }
        }
    }

    subCommand("setcooldown", "Set the cooldown duration for notifications") {
        val cooldownArgument = argument("duration", "The cooldown duration in minutes", ArgumentTypes.INT, required = true)

        handle { context ->
            if (!checkManagementPermission(context)) {
                context.respond("You do not have permission to manage VcNotify settings.")
                return@handle
            }

            if (context !is DiscordSlashCommandContext) {
                context.respond("This command is currently only available on Discord.")
                return@handle
            }

            if (context.interaction !is GuildChatInputCommandInteraction) {
                context.respond("This command can only be used in a server.")
                return@handle
            }

            val guildInteraction = context.interaction as GuildChatInputCommandInteraction
            val platformId = PlatformId.fromSnowflake(guildInteraction.guildId)

            newSuspendedTransaction {
                var settings = VcNotifySettings.getSettings(platformId)

                if (settings == null) {
                    settings = VcNotifySettings.create(platformId)
                }

                val cooldownMinutes = cooldownArgument.resolve(context) ?: 30
                settings.cooldown = cooldownMinutes.minutes

                context.respond("Notification cooldown set to ${settings.cooldown.inWholeMinutes} minutes.")
            }
        }
    }
}
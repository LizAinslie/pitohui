package dev.lizainslie.pitohui.modules.greeting.commands

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.lizainslie.pitohui.core.commands.argument.ArgumentTypes
import dev.lizainslie.pitohui.core.commands.defineCommand
import dev.lizainslie.pitohui.modules.greeting.GreetingModule
import dev.lizainslie.pitohui.modules.greeting.data.entities.CommunityGreetingSettings
import dev.lizainslie.pitohui.platforms.discord.Discord
import dev.lizainslie.pitohui.platforms.discord.commands.DiscordCommandContext
import dev.lizainslie.pitohui.platforms.discord.commands.enforceDiscordType
import dev.lizainslie.pitohui.platforms.discord.extensions.snowflake
import dev.lizainslie.pitohui.util.color.hexString
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

val GreetingModuleConfigCommand = defineCommand(
    "greeting_config",
    "Manage community greeting settings",
) {
    platform(Discord) {
        defaultMemberPermissions = Permissions(Permission.ManageGuild)
        dmPermission = false
    }

    communityOnly = true

    subCommand(
        "enable_welcome",
        "Enables the greeting welcome message",
    ) {
        val channelArg = argument("channel", "The channel that welcome messages will be sent to", ArgumentTypes.CHANNEL) {
            required = true
        }
        val messageArg = argument("message", "The content of the welcome message (you can reference {variables} which will be replaced)", ArgumentTypes.STRING) {
            required = true
        }
        val embedArg = argument("embed", "If enabled, the welcome message will be placed inside of an embed", ArgumentTypes.BOOLEAN) {
            required = true
        }
        val colorArg = argument("color", "The color of the embed (if embed is enabled)", ArgumentTypes.COLOR) {
            required = true
        }
        val imageArg = argument("image", "The URL of an image that will be attached with the welcome message", ArgumentTypes.STRING) {
            required = true
        }

        handle {
            val cfgChannel by channelArg.require()
            val cfgMessage by messageArg.require()
            val cfgEmbed by embedArg.require()
            val cfgColor by colorArg.require()
            val cfgImage by imageArg.require()

            enforceDiscordType<DiscordCommandContext> {
                enforceGuild { guild ->

                    if (!checkCallerPermission(Permission.ManageGuild)) {
                        respondError("This command can only be executed by someone who has permission to manage the server")
                        return@enforceGuild
                    }

                    newSuspendedTransaction {
                        var greetingSettings = CommunityGreetingSettings.findByCommunityId(communityId!!)
                            ?: CommunityGreetingSettings.create(communityId!!)

                        greetingSettings.welcomeChannelId = cfgChannel.id
                        greetingSettings.welcomeMessage = cfgMessage
                        greetingSettings.embedWelcome = cfgEmbed
                        greetingSettings.welcomeColor = cfgColor.hexString
                        greetingSettings.welcomeImageUrl = cfgImage

                        respondPrivate("Welcome greetings have been enabled for <#${cfgChannel.id}>.")
                    }
                }
            }
        }
    }

    subCommand(
        "disable_welcome",
        "Disables the greeting welcome message",
    ) {

        handle {
            enforceDiscordType<DiscordCommandContext> {
                enforceGuild { guild ->

                    if (!checkCallerPermission(Permission.ManageGuild)) {
                        respondError("This command can only be executed by someone who has permission to manage the server")
                        return@enforceGuild
                    }

                    newSuspendedTransaction {
                        var greetingSettings = CommunityGreetingSettings.findByCommunityId(communityId!!)
                            ?: CommunityGreetingSettings.create(communityId!!)

                        greetingSettings.welcomeChannelId = null
                        greetingSettings.welcomeMessage = null
                        greetingSettings.embedWelcome = false
                        greetingSettings.welcomeColor = null
                        greetingSettings.welcomeImageUrl = null

                        respondPrivate("Welcome greetings have been disabled.")
                    }
                }
            }
        }
    }

    subCommand(
        "test_welcome",
        "Tests the greeting welcome message by manually triggering it for a user",
    ) {
        val userArg = argument("user", "The user to test the greeting welcome message for", ArgumentTypes.USER) {
            required = true
        }

        handle {
            val testUser by userArg.require()

            enforceDiscordType<DiscordCommandContext> {
                enforceGuild { guild ->

                    if (!checkCallerPermission(Permission.ManageGuild)) {
                        respondError("This command can only be executed by someone who has permission to manage the server")
                        return@enforceGuild
                    }

                    newSuspendedTransaction {
                        var greetingSettings = CommunityGreetingSettings.findByCommunityId(communityId!!)
                            ?: CommunityGreetingSettings.create(communityId!!)

                        if (greetingSettings.welcomeChannelId == null) {
                            respondPrivate("Welcome greetings are disabled and cannot be tested.")
                        } else {
                            respondPrivate("The welcome greeting for <@${testUser.id}> will now be sent.")
                            GreetingModule.handleMemberJoin(communityId!!, testUser.snowflake)
                        }
                    }
                }
            }
        }
    }

    subCommand(
        "enable_goodbye",
        "Enables the greeting goodbye message",
    ) {
        val channelArg = argument("channel", "The channel that goodbye messages will be sent to", ArgumentTypes.CHANNEL) {
            required = true
        }
        val messageArg = argument("message", "The content of the goodbye message (you can reference {variables} which will be replaced)", ArgumentTypes.STRING) {
            required = true
        }
        val embedArg = argument("embed", "If enabled, the goodbye message will be placed inside of an embed", ArgumentTypes.BOOLEAN) {
            required = true
        }
        val colorArg = argument("color", "The color of the embed (if embed is enabled)", ArgumentTypes.COLOR) {
            required = true
        }
        val imageArg = argument("image", "The URL of an image that will be attached with the goodbye message", ArgumentTypes.STRING) {
            required = true
        }

        handle {
            val cfgChannel by channelArg.require()
            val cfgMessage by messageArg.require()
            val cfgEmbed by embedArg.require()
            val cfgColor by colorArg.require()
            val cfgImage by imageArg.require()

            enforceDiscordType<DiscordCommandContext> {
                enforceGuild { guild ->

                    if (!checkCallerPermission(Permission.ManageGuild)) {
                        respondError("This command can only be executed by someone who has permission to manage the server")
                        return@enforceGuild
                    }

                    newSuspendedTransaction {
                        var greetingSettings = CommunityGreetingSettings.findByCommunityId(communityId!!)
                            ?: CommunityGreetingSettings.create(communityId!!)

                        greetingSettings.goodbyeChannelId = cfgChannel.id
                        greetingSettings.goodbyeMessage = cfgMessage
                        greetingSettings.embedGoodbye = cfgEmbed
                        greetingSettings.goodbyeColor = cfgColor.hexString
                        greetingSettings.goodbyeImageUrl = cfgImage

                        respondPrivate("Goodbye greetings have been enabled for <#${cfgChannel.id}>.")
                    }
                }
            }
        }
    }

    subCommand(
        "disable_goodbye",
        "Disables the greeting goodbye message",
    ) {

        handle {
            enforceDiscordType<DiscordCommandContext> {
                enforceGuild { guild ->

                    if (!checkCallerPermission(Permission.ManageGuild)) {
                        respondError("This command can only be executed by someone who has permission to manage the server")
                        return@enforceGuild
                    }

                    newSuspendedTransaction {
                        var greetingSettings = CommunityGreetingSettings.findByCommunityId(communityId!!)
                            ?: CommunityGreetingSettings.create(communityId!!)

                        greetingSettings.goodbyeChannelId = null
                        greetingSettings.goodbyeMessage = null
                        greetingSettings.embedGoodbye = false
                        greetingSettings.goodbyeColor = null
                        greetingSettings.goodbyeImageUrl = null

                        respondPrivate("Goodbye greetings have been disabled.")
                    }
                }
            }
        }
    }

    subCommand(
        "test_goodbye",
        "Tests the greeting goodbye message by manually triggering it for a user",
    ) {
        val userArg = argument("user", "The user to test the greeting goodbye message for", ArgumentTypes.USER) {
            required = true
        }

        handle {
            val testUser by userArg.require()

            enforceDiscordType<DiscordCommandContext> {
                enforceGuild { guild ->

                    if (!checkCallerPermission(Permission.ManageGuild)) {
                        respondError("This command can only be executed by someone who has permission to manage the server")
                        return@enforceGuild
                    }

                    newSuspendedTransaction {
                        var greetingSettings = CommunityGreetingSettings.findByCommunityId(communityId!!)
                            ?: CommunityGreetingSettings.create(communityId!!)

                        if (greetingSettings.goodbyeChannelId == null) {
                            respondPrivate("Goodbye greetings are disabled and cannot be tested.")
                        } else {
                            respondPrivate("The goodbye greeting for <@${testUser.id}> will now be sent.")
                            GreetingModule.handleMemberLeave(communityId!!, testUser.snowflake)
                        }
                    }
                }
            }
        }
    }
}
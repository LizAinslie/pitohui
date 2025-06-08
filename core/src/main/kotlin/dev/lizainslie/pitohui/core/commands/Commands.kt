package dev.lizainslie.pitohui.core.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.SubCommand
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.commands.platform.DiscordSlashCommandContext
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.platforms.Platforms

class Commands(
    private val bot: Bot
) {
    val commands = mutableListOf<CommandRegistration>()

    fun initialize() {
        bot.kord.on<GuildChatInputCommandInteractionCreateEvent> {
            handle()
        }
    }

    suspend fun registerCommand(command: RootCommand, module: AbstractModule) {
        commands.add(CommandRegistration(command, module))
        for (guildId in bot.config.guilds) {
            println("registering command ${command.name} in guild $guildId")
            bot.kord.createGuildChatInputCommand(
                guildId,
                command.name,
                command.description,
            ) {
                arguments(command.arguments)

                subCommands(command.subCommands)
            }
        }
    }

    fun GuildChatInputCommandInteractionCreateEvent.resolveCommandRegistration(): CommandRegistration? {
        val interactionCmd = interaction.command
        val commandName = interactionCmd.rootName
        return commands.find { it.command.rootName == commandName }
    }

    suspend fun GuildChatInputCommandInteractionCreateEvent.handle() {
        val interactionCmd = interaction.command
        val registration = resolveCommandRegistration()

        if (registration == null) {
            interaction.respondEphemeral {
                content = "Command ${interactionCmd.rootName} not found"
            }
            return
        }

        if (!registration.module.isEnabledForCommunity(Platforms.DISCORD, interaction.guildId.value.toString())) {
            interaction.respondEphemeral {
                content = "Command ${interactionCmd.rootName} is not enabled for this community"
            }
            return
        }

        val command = registration.command

//        val firstLevelOptions = interactionCmd.data.options.orEmpty()
//        val subCommandPredicate =
//            firstLevelOptions.all { it.value is Optional.Missing && it.subCommands is Optional.Missing }

        var handlingCommand: BaseCommand = command

        if (interactionCmd is SubCommand /* && subCommandPredicate */) {
            val subCommandName = interactionCmd.name

            println("Resolving subcommand: $subCommandName")

            val subCommand = command.subCommands.find { it.name == subCommandName }

            if (subCommand == null) {
                interaction.respondEphemeral {
                    content = "Subcommand $subCommandName not found"
                }
                println("Subcommand $subCommandName not found")
                return
            }

            println("Using subcommand: ${subCommand.name}")

            handlingCommand = subCommand
        }

        if (Platforms.DISCORD in command.platforms) {
            println("Handling command ${command.name} on discord")
            handlingCommand.handle(DiscordSlashCommandContext(
                bot = bot,
                module = registration.module,
                platform = Platforms.DISCORD,
                interaction = interaction,
            ))
        } else {
            interaction.respondEphemeral {
                content = "Command ${command.name} not usable as a slash command"
            }
        }
    }
}
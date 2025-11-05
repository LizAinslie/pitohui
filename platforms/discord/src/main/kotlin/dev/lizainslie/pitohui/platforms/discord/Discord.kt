package dev.lizainslie.pitohui.platforms.discord

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.Role
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.interaction.SubCommand
import dev.kord.core.event.Event
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.gateway.ALL
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.commands.BaseCommand
import dev.lizainslie.pitohui.core.commands.PlatformArgumentParseFn
import dev.lizainslie.pitohui.core.commands.RootCommand
import dev.lizainslie.pitohui.platforms.discord.extensions.arguments
import dev.lizainslie.pitohui.platforms.discord.commands.DiscordSlashCommandContext
import dev.lizainslie.pitohui.platforms.discord.extensions.subCommands
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.modules.ModuleVisibility
import dev.lizainslie.pitohui.core.platforms.PlatformAdapter
import dev.lizainslie.pitohui.core.platforms.PlatformAdapterFactory
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.PlatformKey
import dev.lizainslie.pitohui.platforms.discord.config.DiscordPlatformConfig
import dev.lizainslie.pitohui.platforms.discord.extensions.platform
import dev.lizainslie.pitohui.platforms.discord.extensions.snowflake
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.collections.find

class Discord(config: DiscordPlatformConfig) : PlatformAdapter<DiscordPlatformConfig>(config, key, "Discord") {
    lateinit var kord: Kord

    override val channelArgumentParser =
        PlatformArgumentParseFn { value ->
            when (value) {
                is Channel -> value.id.platform
                is Snowflake -> value.platform
                is String -> {
                    val channelId =
                        if (value.startsWith("<#") && value.endsWith(">")) // this is a formatted mention
                            value.substring(2, value.length - 1)
                        else if (value.all { it.isDigit() }) // this is a raw ID
                            value
                        else throw IllegalArgumentException("Invalid channel ID format: $value") // ohno.jpg

                    PlatformId(key, channelId)
                }
                else -> throw IllegalArgumentException("Invalid channel type: ${value::class.simpleName}")
            }
        }

    override val roleArgumentParser =
        PlatformArgumentParseFn { value ->
            when (value) {
                is Role -> value.id.platform
                is Snowflake -> value.platform
                is String -> {
                    val roleId =
                        if (value.startsWith("<@&") && value.endsWith(">")) // this is a formatted mention
                            value.substring(2, value.length - 1)
                        else if (value.all { it.isDigit() }) // this is a raw ID
                            value
                        else throw IllegalArgumentException("Invalid role ID format: $value") // ohno.jpg

                    PlatformId(key, roleId)
                }
                else -> throw IllegalArgumentException("Invalid role type: ${value::class.simpleName}")
            }
        }

    override suspend fun initialize(bot: Bot) {
        super.initialize(bot)
        kord = Kord(config.token)

        kord.on<GuildChatInputCommandInteractionCreateEvent> {
            handle()
        }
    }

    inline fun <reified T : Event> addEventListener(
        scope: CoroutineScope = kord,
        noinline consumer: suspend T.() -> Unit
    ): Job = kord.on<T>(scope, consumer)

    @OptIn(PrivilegedIntent::class)
    override suspend fun start(bot: Bot) {
        kord.login {
            intents = Intents.ALL
        }
    }

    override suspend fun registerCommand(command: RootCommand, module: AbstractModule) {
        for (guildConf in config.guilds) {
            if (module.visibility == ModuleVisibility.DEVELOPER && !guildConf.admin) continue

            println("registering command ${command.name} in guild ${guildConf.id}")
            kord.createGuildChatInputCommand(
                guildConf.id,
                command.name,
                command.description,
            ) {
                arguments(command.arguments)

                subCommands(command.subCommands)
            }
        }
    }

    suspend fun getChannelById(id: Snowflake) = kord.getChannel(id)
    suspend fun getChannelById(id: PlatformId) = getChannelById(id.snowflake)
    suspend fun getChannelById(id: Long) = getChannelById(Snowflake(id))
    suspend fun getChannelById(id: ULong) = getChannelById(Snowflake(id))
    suspend fun getChannelById(id: String) = getChannelById(Snowflake(id))

    suspend fun getGuildById(id: Snowflake) = kord.getGuild(id)
    suspend fun getGuildById(id: PlatformId) = getGuildById(id.snowflake)
    suspend fun getGuildById(id: Long) = getGuildById(Snowflake(id))
    suspend fun getGuildById(id: ULong) = getGuildById(Snowflake(id))
    suspend fun getGuildById(id: String) = getGuildById(Snowflake(id))

    suspend fun getUserById(id: Snowflake) = kord.getUser(id)
    suspend fun getUserById(id: PlatformId) = getUserById(id.snowflake)
    suspend fun getUserById(id: Long) = getUserById(Snowflake(id))
    suspend fun getUserById(id: ULong) = getUserById(Snowflake(id))
    suspend fun getUserById(id: String) = getUserById(Snowflake(id))

    private suspend fun GuildChatInputCommandInteractionCreateEvent.handle() {
        val interactionCmd = interaction.command
        val registration = bot.commands.getRegistration(interactionCmd.rootName)

        if (registration == null) {
            interaction.respondEphemeral {
                content = "Command ${interactionCmd.rootName} not found"
            }
            return
        }

        if (!registration.module.isEnabledForCommunity(interaction.guildId.platform)) {
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

        if (interactionCmd is SubCommand) {
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

        bot.commands.dispatchCommand(handlingCommand, DiscordSlashCommandContext(
            bot = bot,
            module = registration.module,
            platform = Discord,
            interaction = interaction,
        ))
    }

    companion object : PlatformAdapterFactory<DiscordPlatformConfig, Discord>() {
        override val key = PlatformKey("discord")
        override val configSerializer = DiscordPlatformConfig.serializer()
        override val platformClass = Discord::class
        override fun initialize(config: DiscordPlatformConfig) = Discord(config)
        override fun createDefaultConfig() = DiscordPlatformConfig()
    }
}
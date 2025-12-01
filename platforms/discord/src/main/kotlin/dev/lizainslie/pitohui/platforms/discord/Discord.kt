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
import dev.lizainslie.pitohui.core.config.Configs
import dev.lizainslie.pitohui.core.logging.suspendLogModule
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.modules.ModuleVisibility
import dev.lizainslie.pitohui.core.platforms.PlatformAdapter
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.PlatformKey
import dev.lizainslie.pitohui.platforms.discord.extensions.arguments
import dev.lizainslie.pitohui.platforms.discord.commands.DiscordSlashCommandContext
import dev.lizainslie.pitohui.platforms.discord.extensions.subCommands
import dev.lizainslie.pitohui.platforms.discord.config.DiscordPlatformConfig
import dev.lizainslie.pitohui.platforms.discord.extensions.platform
import dev.lizainslie.pitohui.platforms.discord.extensions.snowflake
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.collections.find

object Discord : PlatformAdapter(
    key = PlatformKey("discord"),
    displayName = "Discord"
) {
    val config by Configs.config<DiscordPlatformConfig>()

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
        if (!command.communityOnly && module.visibility != ModuleVisibility.DEVELOPER && config.registerCommandsGlobally) {
            log.info("Registering global command ${command.name}")
            kord.createGlobalChatInputCommand(
                command.name,
                command.description,
            ) {
                arguments(command.arguments)

                subCommands(command.subCommands)
            }
        }

        for (guildConf in config.guilds) {
            if (module.visibility == ModuleVisibility.DEVELOPER && !guildConf.admin) continue

            log.info("registering command ${command.name} in guild ${guildConf.id}")
            val registeredCmd = kord.createGuildChatInputCommand(
                guildConf.id,
                command.name,
                command.description,
            ) {
                arguments(command.arguments)

                subCommands(command.subCommands)
            }

            // todo: save registeredCmd.id for later use in unregistering
        }
    }

    override suspend fun unregisterCommand(command: RootCommand, module: AbstractModule) {
        if (!command.communityOnly && config.registerCommandsGlobally) {
            log.info("Getting all registered global commands to unregister ${command.name}")

            val commands = kord.rest.interaction.getGlobalApplicationCommands(kord.selfId).toList()
            log.debug("Found ${commands.size} registered global commands")
            val commandToRemove = commands.find { it.name == command.name }

            if (commandToRemove != null) {
                kord.rest.interaction.deleteGlobalApplicationCommand(
                    kord.selfId,
                    commandToRemove.id
                )
                log.info("Deleted global command ${command.name}")
            } else {
                log.warn("Global command ${command.name} not found")
            }
        }

        for (guildConf in config.guilds) {
            if (module.visibility == ModuleVisibility.DEVELOPER && !guildConf.admin) continue

            log.debug("Getting all registered commands in guild ${guildConf.id} to unregister ${command.name}")

            val commands =
                kord.rest.interaction.getGuildApplicationCommands(kord.selfId, guildConf.id).toList()
            log.debug("Found ${commands.size} registered commands in guild ${guildConf.id}")
            val commandToRemove = commands.find { it.name == command.name }

            if (commandToRemove != null) {
                kord.rest.interaction.deleteGuildApplicationCommand(
                    kord.selfId,
                    guildConf.id,
                    commandToRemove.id
                )
                log.info("Deleted command ${command.name} from guild ${guildConf.id}")
            } else {
                log.warn("Command ${command.name} not found in guild ${guildConf.id}")
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
        this@Discord.suspendLogPlatform {

            val interactionCmd = interaction.command
            val registration = bot.commands.getRegistration(interactionCmd.rootName)

            if (registration == null) {
                interaction.respondEphemeral {
                    content = "Command ${interactionCmd.rootName} not found"
                }
                return@suspendLogPlatform
            }

            suspendLogModule(registration.module) {

                if (!registration.module.isEnabledForCommunity(interaction.guildId.platform)) {
                    interaction.respondEphemeral {
                        content = "Command ${interactionCmd.rootName} is not enabled for this community"
                    }
                    return@suspendLogModule
                }

                val command = registration.command

//        val firstLevelOptions = interactionCmd.data.options.orEmpty()
//        val subCommandPredicate =
//            firstLevelOptions.all { it.value is Optional.Missing && it.subCommands is Optional.Missing }

                var handlingCommand: BaseCommand = command

                if (interactionCmd is SubCommand) {
                    val subCommandName = interactionCmd.name

                    log.debug("Resolving subcommand: $subCommandName")

                    val subCommand = command.subCommands.find { it.name == subCommandName }

                    if (subCommand == null) {
                        interaction.respondEphemeral {
                            content = "Subcommand $subCommandName not found"
                        }
                        log.warn("Subcommand $subCommandName not found")
                        return@suspendLogModule
                    }

                    log.debug("Using subcommand: ${subCommand.name}")

                    handlingCommand = subCommand
                }

                bot.commands.dispatchCommand(
                    handlingCommand, DiscordSlashCommandContext(
                        bot = bot,
                        module = registration.module,
                        interaction = interaction,
                    )
                )
            }
        }
    }
}
package dev.lizainslie.pitohui.platforms.discord.commands

import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.commands.ArgumentDescriptor
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.platforms.discord.entities.DiscordSlashCommandResponse
import dev.lizainslie.pitohui.platforms.discord.extensions.platform
import kotlinx.datetime.Clock

class DiscordSlashCommandContext(
    bot: Bot,
    module: AbstractModule,

    val interaction: ChatInputCommandInteraction
) : DiscordCommandContext(bot, module) {
    override suspend fun respond(text: String): DiscordSlashCommandResponse {
        val kordResponse = interaction.respondPublic {
            content = text
        }

        response = DiscordSlashCommandResponse(kordResponse, this)
        return response as DiscordSlashCommandResponse
    }

    override suspend fun respondPrivate(text: String): DiscordSlashCommandResponse {
        val kordResponse = interaction.respondEphemeral {
            content = text
        }

        response = DiscordSlashCommandResponse(kordResponse, this)
        return response as DiscordSlashCommandResponse
    }

    override suspend fun respondError(text: String): DiscordSlashCommandResponse {
        val kordResponse = interaction.respondEphemeral {
            content = text
        }

        response = DiscordSlashCommandResponse(kordResponse, this)
        return response as DiscordSlashCommandResponse
    }

    override suspend fun respond(text: String, block: EmbedBuilder.() -> Unit): DiscordSlashCommandResponse {
        val kordResponse = interaction.respondPublic {
            content = text
            embed(block)
        }

        response = DiscordSlashCommandResponse(kordResponse, this)
        return response as DiscordSlashCommandResponse
    }

    override suspend fun respondPrivate(text: String, block: EmbedBuilder.() -> Unit): DiscordSlashCommandResponse {
        val kordResponse = interaction.respondEphemeral {
            content = text
            embed(block)
        }

        response = DiscordSlashCommandResponse(kordResponse, this)
        return response as DiscordSlashCommandResponse
    }

    override fun <T> resolveRawArgumentValue(arg: ArgumentDescriptor<T>): T? {
        val option = interaction.command.options[arg.name]

        return option?.value?.let {
            arg.argumentType.tryParse(it, this).getOrDefault(arg.defaultValue)
        }
    }

    override suspend fun dump() {
        val stealth = callerIsStealth()

        val guild = getGuild()
        val channel = getChannel()

        interaction.channel.createMessage {
            embed {
                title = "Context dumped!"
                description = "Read fields below for details"
                timestamp = Clock.System.now()

                field("Is caller in stealth?", true) {
                    if (stealth) "Yes" else "No"
                }

                field("Called from guild?", true) {
                    if (isInGuild) "Yes" else "No"
                }

                field("", true)

                if (isInGuild && guild != null) field("Guild", true) {
                    "${guild.name} (ID: `${guild.id}`)"
                }

                if (channel != null) field("Channel", true) {
                    "${channel.mention} (ID: `${channel.id}`)"
                }

                footer {
                    text = "Pitohui v0.0.1-alpha.1"
                }
            }
        }
    }

    override val channelId: PlatformId = interaction.channelId.platform
    override val callerId: PlatformId = interaction.user.id.platform
    override val guildId: PlatformId? = interaction.invokedCommandGuildId?.platform
    override val isInGuild: Boolean = interaction.invokedCommandGuildId != null

//    init {
//        println("community id: $communityId, in community? $isInCommunity, caller id: $callerId, guild id: $guildId, channel id: $channelId")
//    }
}

package dev.lizainslie.pitohui.platforms.discord.commands

import dev.kord.common.Color
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import dev.kord.rest.builder.message.embed
import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.commands.CommandContext
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.platforms.PlatformAdapterFactory
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.platforms.discord.Discord
import dev.lizainslie.pitohui.platforms.discord.extensions.snowflake
import kotlinx.datetime.Clock

abstract class DiscordCommandContext(
    bot: Bot,
    module: AbstractModule,
    platform: PlatformAdapterFactory<*, *>,
) : CommandContext(bot, module, platform) {
    abstract val channelId: PlatformId
    abstract val guildId: PlatformId?
    abstract val isInGuild: Boolean

    suspend fun getGuild() = guildId?.let {
        Discord.get().getGuildById(it)
    }

    suspend fun getMember() = getGuild()?.getMember(callerId.snowflake)
    suspend fun getCaller() = Discord.get().getUserById(callerId)
    suspend fun getChannel() = Discord.get().getChannelById(channelId)

    suspend fun respond(block: UserMessageCreateBuilder.() -> Unit) {
        val channel = getChannel() as MessageChannel
        channel.createMessage(block)
    }

    suspend fun respondPrivate(block: UserMessageCreateBuilder.() -> Unit) {
        val channel = getCaller()!!.getDmChannel()
        channel.createMessage(block)
    }

    suspend fun respondStealth(block: UserMessageCreateBuilder.() -> Unit) {
        if (callerIsStealth()) respondPrivate(block)
        else respond(block)
    }

    override suspend fun respondException(e: Exception) {
        respondStealth {
            embed {
                buildExceptionEmbed(e)
            }
        }
    }

    open suspend fun enforceGuild(
        message: String = "This command must be run in a server",
        block: suspend DiscordCommandContext.(guild: Guild) -> Unit
    ) {
        if (isInGuild) this.block(getGuild()!!)
        else respondError(message)
    }
}

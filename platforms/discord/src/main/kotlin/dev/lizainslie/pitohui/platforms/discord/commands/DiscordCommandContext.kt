package dev.lizainslie.pitohui.platforms.discord.commands

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.commands.CommandContext
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.platforms.PlatformAdapterFactory
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.platforms.discord.Discord
import dev.lizainslie.pitohui.platforms.discord.extensions.snowflake

abstract class DiscordCommandContext(
    bot: Bot,
    module: AbstractModule,
    platform: PlatformAdapterFactory<*, *>,
) : CommandContext(bot, module, platform) {
    abstract val channelId: PlatformId
    abstract val guildId: PlatformId?
    abstract val isInGuild: Boolean

    override val communityId = guildId

    suspend fun getGuild() = guildId?.let {
        Discord.get().getGuildById(it)
    }

    suspend fun getMember() = getGuild()?.getMember(callerId.snowflake)
    suspend fun getCaller() = Discord.get().getUserById(callerId)
    suspend fun getChannel() = Discord.get().getChannelById(channelId)

    suspend fun checkCallerPermission(permissions: Permission): Boolean {
        if (!isInGuild) return false

//        println("in guild")

        val member = getMember() ?: return false

//        println(member.toString())

        val perms = member.getPermissions()

//        println("Caller permissions: ${perms.values.joinToString(", ") {
//            perm -> perm.toString()
//        }}")

        return perms.contains(permissions)
    }

    suspend fun checkCallerPermission(permissions: Permissions): Boolean {
        if (!isInGuild) return false

//        println("in guild")

        val member = getMember() ?: return false

//        println(member.toString())

        val perms = member.getPermissions()

//        println("Caller permissions: ${perms.values.joinToString(", ") {
//            perm -> perm.toString()
//        }}")

        return perms.contains(permissions)
    }

    open suspend fun respond(text: String = "", block: EmbedBuilder.() -> Unit) {
        val channel = getChannel() as MessageChannel
        channel.createMessage {
            content = text
            embed(block)
        }
    }

    open suspend fun respondPrivate(text: String = "", block: EmbedBuilder.() -> Unit) {
        val channel = getCaller()!!.getDmChannel()
        channel.createMessage {
            content = text
            embed(block)
        }
    }

    suspend fun respondStealth(text: String = "", block: EmbedBuilder.() -> Unit) {
        if (callerIsStealth()) respondPrivate(text, block)
        else respond(text, block)
    }

    override suspend fun respondException(e: Exception) {
        respondStealth {
            buildExceptionEmbed(e)
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

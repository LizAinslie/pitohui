package dev.lizainslie.pitohui.platforms.discord.commands

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.entity.Guild
import dev.kord.rest.builder.message.EmbedBuilder
import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.commands.CommandContext
import dev.lizainslie.pitohui.core.commands.argument.ResolvedArguments
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.platforms.discord.Discord
import dev.lizainslie.pitohui.platforms.discord.entities.DiscordResponse
import dev.lizainslie.pitohui.platforms.discord.extensions.snowflake

abstract class DiscordCommandContext(
    bot: Bot,
    module: AbstractModule,
    arguments: ResolvedArguments,
) : CommandContext(bot, module, Discord, arguments) {
    abstract val channelId: PlatformId
    abstract val guildId: PlatformId?
    abstract val isInGuild: Boolean

    // we use `get()` here because we need a functional getter otherwise it
    // evaluates to `null` even if the value of `guildId` is not `null`
    final override val communityId get() = guildId

    suspend fun getGuild() = guildId?.let {
        Discord.getGuildById(it)
    }

    suspend fun getMember() = getGuild()?.getMember(callerId.snowflake)
    suspend fun getCaller() = Discord.getUserById(callerId)
    suspend fun getChannel() = Discord.getChannelById(channelId)

    suspend fun checkCallerPermission(permissions: Permission): Boolean {
        if (!isInGuild) return false
        val member = getMember() ?: return false
        val perms = member.getPermissions()
        return member.isOwner() or perms.contains(Permission.Administrator) or perms.contains(permissions)
    }

    suspend fun checkCallerPermission(permissions: Permissions): Boolean {
        if (!isInGuild) return false
        val member = getMember() ?: return false
        val perms = member.getPermissions()
        return perms.contains(permissions)
    }

    abstract suspend fun respond(text: String = "", block: EmbedBuilder.() -> Unit): DiscordResponse
//    {
//        val channel = getChannel() as MessageChannel
//        channel.createMessage {
//            content = text
//            embed(block)
//        }
//    }

    abstract suspend fun respondPrivate(text: String = "", block: EmbedBuilder.() -> Unit): DiscordResponse
//    {
//        val channel = getCaller()!!.getDmChannel()
//        channel.createMessage {
//            content = text
//            embed(block)
//        }
//    }

    suspend fun respondStealth(text: String = "", block: EmbedBuilder.() -> Unit) =
        if (callerIsStealth()) respondPrivate(text, block)
        else respond(text, block)

    override suspend fun respondException(e: Exception) =
        if (response != null)
            (response as DiscordResponse).createStealthFollowup {
                buildExceptionEmbed(e)
            }
        else
            respondStealth {
                buildExceptionEmbed(e)
            }

    open suspend fun enforceGuild(
        message: String = "This command must be run in a server",
        block: suspend DiscordCommandContext.(guild: Guild) -> Unit
    ) {
        if (isInGuild) this.block(getGuild()!!)
        else respondError(message)
    }
}

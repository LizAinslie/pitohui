package dev.lizainslie.pitohui.core.platforms

import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.entity.channel.ResolvedChannel

sealed interface PlatformChannel {
    val name: String
    val mention: String
        get() = "#$name"

    data class DiscordChannel(
        val channel: Channel,
    ) : PlatformChannel {
        override val name = when (channel) {
            is GuildChannel -> channel.name
            is ResolvedChannel -> channel.name // can't combine these two
            else -> "Unnamed Channel"
        }
        override val mention = channel.mention
        val id = channel.id
        val type = channel.type
    }
}


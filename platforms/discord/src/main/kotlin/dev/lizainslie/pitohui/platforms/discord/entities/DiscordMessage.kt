package dev.lizainslie.pitohui.platforms.discord.entities

import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.entity.Message
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.entities.PlatformMessage
import dev.lizainslie.pitohui.platforms.discord.extensions.platform

open class DiscordMessage(
    override val content: String,
    override val authorId: PlatformId,
    val channelId: PlatformId,
) : PlatformMessage {
    constructor(message: Message) : this(
        content = message.content,
        authorId = message.author!!.id.platform,
        channelId = message.channelId.platform
    )
}
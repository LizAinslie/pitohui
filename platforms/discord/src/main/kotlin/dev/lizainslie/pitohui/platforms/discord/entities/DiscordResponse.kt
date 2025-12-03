package dev.lizainslie.pitohui.platforms.discord.entities

import dev.kord.rest.builder.message.EmbedBuilder
import dev.lizainslie.pitohui.core.platforms.entities.PlatformResponse

interface DiscordResponse : PlatformResponse {
    suspend fun createFollowup(text: String = "", block: EmbedBuilder.() -> Unit): DiscordResponse
    suspend fun createPrivateFollowup(text: String = "", block: EmbedBuilder.() -> Unit): DiscordResponse

    suspend fun createStealthFollowup(content: String = "", block: EmbedBuilder.() -> Unit) =
        if (context.callerIsStealth()) createPrivateFollowup(content, block)
        else createFollowup(content, block)
}
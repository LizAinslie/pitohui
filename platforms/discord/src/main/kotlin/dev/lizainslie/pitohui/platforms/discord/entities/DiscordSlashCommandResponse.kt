package dev.lizainslie.pitohui.platforms.discord.entities

import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.kord.core.behavior.interaction.response.createPublicFollowup
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import dev.lizainslie.pitohui.platforms.discord.commands.DiscordSlashCommandContext

class DiscordSlashCommandResponse(
    private val response: MessageInteractionResponseBehavior,
    override val context: DiscordSlashCommandContext,
) :  DiscordResponse {
    override suspend fun edit(newContent: String) {
        response.edit {
            content = newContent
        }
    }

    override suspend fun createFollowup(content: String): DiscordSlashCommandResponse {
        response.createPublicFollowup {
            this.content = content
        }

        return this
    }

    override suspend fun createPrivateFollowup(content: String): DiscordSlashCommandResponse {
        response.createEphemeralFollowup {
            this.content = content
        }

        return this
    }

    override suspend fun createStealthFollowup(content: String) =
        if (context.callerIsStealth()) createPrivateFollowup(content)
        else createFollowup(content)

    override suspend fun createFollowup(text: String, block: EmbedBuilder.() -> Unit): DiscordResponse {
        response.createPublicFollowup {
            content = text
            embed(block)
        }

        return this
    }

    override suspend fun createPrivateFollowup(text: String, block: EmbedBuilder.() -> Unit): DiscordResponse {
        response.createEphemeralFollowup {
            content = text
            embed(block)
        }

        return this
    }
}
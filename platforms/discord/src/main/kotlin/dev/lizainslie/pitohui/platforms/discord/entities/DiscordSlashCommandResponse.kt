package dev.lizainslie.pitohui.platforms.discord.entities

import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.kord.core.behavior.interaction.response.createPublicFollowup
import dev.kord.core.behavior.interaction.response.edit

class DiscordSlashCommandResponse(
    private val response: MessageInteractionResponseBehavior
) :  DiscordResponse {
    override suspend fun edit(newContent: String) {
        response.edit {
            content = newContent
        }
    }

    override suspend fun createFollowup(content: String) {
        response.createPublicFollowup {
            this.content = content
        }
    }

    override suspend fun createPrivateFollowup(content: String) {
        response.createEphemeralFollowup {
            this.content = content
        }
    }
}
package dev.lizainslie.pitohui.modules.greeting

import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.guild.MemberJoinEvent
import dev.kord.core.event.guild.MemberLeaveEvent
import dev.kord.rest.builder.message.embed
import dev.lizainslie.pitohui.core.Bot
import dev.lizainslie.pitohui.core.modules.AbstractModule
import dev.lizainslie.pitohui.core.placeholder.placeholders
import dev.lizainslie.pitohui.core.platforms.PlatformId
import dev.lizainslie.pitohui.core.platforms.SupportPlatforms
import dev.lizainslie.pitohui.modules.greeting.data.entities.CommunityGreetingSettings
import dev.lizainslie.pitohui.modules.greeting.data.tables.CommunityGreetingSettingsTable
import dev.lizainslie.pitohui.platforms.discord.Discord
import dev.lizainslie.pitohui.platforms.discord.extensions.platform
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

@SupportPlatforms(Discord::class)
object GreetingModule : AbstractModule(
    name = "greeting",
    description = "A module that provides greeting commands.",
    tables = setOf(
        CommunityGreetingSettingsTable
    )
) {
    override fun onInit(bot: Bot) {
        super.onInit(bot)

        Discord.addEventListener<MemberJoinEvent> {
             handleMemberJoin(
                 communityId = guildId.platform
             )
        }

        Discord.addEventListener<MemberLeaveEvent> {
            handleMemberLeave(
                communityId = guildId.platform
            )
        }
    }

    /**
     * Handle the [MemberJoinEvent] and send a welcome message if configured.
     *
     * @param communityId The ID of the community where the member joined.
     */
    private suspend fun handleMemberJoin(communityId: PlatformId) {
        val settings = findSettings(communityId) ?: return

        val channel = settings.welcomeChannelId?.let {
            Discord.getChannelById(it)
        } ?: return

        val placeholders = placeholders {
            replace("user_mention", "")
        }

        if (channel is MessageChannel) {
            channel.createMessage {
                if (settings.embedWelcome) {
                    embed {
                        settings.welcomeMessage?.let {
                            description = placeholders.replace(it)
                        }

                        settings.welcomeColor?.let {
//                            color =
                        }

                        timestamp = Clock.System.now()
                    }
                } else {
                    settings.welcomeMessage?.let {
                        content = placeholders.replace(it)
                    }

                    settings.welcomeImageUrl?.let {
                        // add as attachment
                    }
                }
            }
        }
    }

    /**
     * Handle the [MemberLeaveEvent] and send a goodbye message if configured.
     *
     * @param communityId The ID of the community where the member left.
     */
    private suspend fun handleMemberLeave(communityId: PlatformId) {
        val settings = findSettings(communityId) ?: return

        if (settings.goodbyeChannelId == null) return
    }

    /**
     * Find the [CommunityGreetingSettings] for the given [communityId].
     *
     * @param communityId The ID of the community to find settings for.
     * @return The [CommunityGreetingSettings] for the given [communityId], or null if none exist.
     */
    suspend fun findSettings(communityId: PlatformId) =
        newSuspendedTransaction {
            CommunityGreetingSettings.findByCommunityId(communityId)
        }
}